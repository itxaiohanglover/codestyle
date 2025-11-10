
package top.codestyle.file.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.ProgressListener;
import org.dromara.x.file.storage.core.platform.FileStorage;
import org.dromara.x.file.storage.core.upload.UploadPretreatment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.codestyle.file.mapper.FileMapper;
import top.codestyle.file.service.FileService;
import top.codestyle.file.service.StorageService;
import top.codestyle.model.dto.file.FileReq;
import top.codestyle.model.entity.FileDO;
import top.codestyle.model.entity.StorageDO;
import top.codestyle.model.enums.FileTypeEnum;
import top.codestyle.model.query.FileQuery;
import top.codestyle.model.vo.FileResp;
import top.codestyle.model.vo.FileStatisticsResp;
import top.continew.starter.core.constant.StringConstants;
import top.continew.starter.core.util.StrUtils;
import top.continew.starter.core.util.URLUtils;
import top.continew.starter.core.validation.CheckUtils;
import top.continew.starter.extension.crud.service.BaseServiceImpl;


import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件业务实现
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends BaseServiceImpl<FileMapper, FileDO, FileResp, FileResp, FileQuery, FileReq> implements FileService {

    private final FileStorageService fileStorageService;
    @Resource
    StorageService storageService;

    @Override
    protected void beforeDelete(List<Long> ids) {
        List<FileDO> fileList = baseMapper.lambdaQuery().in(FileDO::getId, ids).list();
        Map<Long, List<FileDO>> fileListGroup = fileList.stream().collect(Collectors.groupingBy(FileDO::getStorageId));
        for (Map.Entry<Long, List<FileDO>> entry : fileListGroup.entrySet()) {
            StorageDO storage = storageService.getById(entry.getKey());
            for (FileDO file : entry.getValue()) {
                FileInfo fileInfo = file.toFileInfo(storage);
                fileStorageService.delete(fileInfo);
            }
        }
    }

    @Override
    public FileInfo upload(MultipartFile file, String storageCode) {
        StorageDO storage;
        if (StrUtil.isBlank(storageCode)) {
            storage = storageService.getDefaultStorage();
            CheckUtils.throwIfNull(storage, "请先指定默认存储");
        } else {
            storage = storageService.getByCode(storageCode);
            CheckUtils.throwIfNotExists(storage, "StorageDO", "Code", storageCode);
        }
        LocalDate today = LocalDate.now();
        String path = today.getYear() + StringConstants.SLASH + today.getMonthValue() + StringConstants.SLASH + today
                .getDayOfMonth() + StringConstants.SLASH;
        System.out.println(file.getOriginalFilename());
        UploadPretreatment uploadPretreatment = fileStorageService.of(file)
                .setPlatform(storage.getCode())
                .putAttr(ClassUtil.getClassName(StorageDO.class, false), storage)
                .setPath(path)
                .setSaveFilename(file.getOriginalFilename())
                .setSaveThFilename(file.getOriginalFilename());

        // 图片文件生成缩略图
        if (FileTypeEnum.IMAGE.getExtensions().contains(FileNameUtil.extName(file.getOriginalFilename()))) {
            uploadPretreatment.thumbnail(img -> img.size(100, 100));
        }
        log.info("Storage code: {}", storage.getCode());
        uploadPretreatment.setProgressMonitor(new ProgressListener() {
            @Override
            public void start() {
                log.info("开始上传");
            }

            @Override
            public void progress(long progressSize, Long allSize) {
                log.info("已上传 [{}]，总大小 [{}]", progressSize, allSize);
            }

            @Override
            public void finish() {
                log.info("上传结束");
            }
        });
        // 处理本地存储文件 URL
        FileInfo fileInfo = uploadPretreatment.upload();
        String domain = StrUtil.appendIfMissing(storage.getDomain(), StringConstants.SLASH);
        fileInfo.setUrl(URLUtil.normalize(domain + fileInfo.getPath() + fileInfo.getFilename()));
        return fileInfo;
    }

    @Override
    public Long countByStorageIds(List<Long> storageIds) {
        return baseMapper.lambdaQuery().in(FileDO::getStorageId, storageIds).count();
    }

    @Override
    public FileStatisticsResp statistics() {
        FileStatisticsResp resp = new FileStatisticsResp();
        List<FileStatisticsResp> statisticsList = baseMapper.statistics();
        if (CollUtil.isEmpty(statisticsList)) {
            return resp;
        }
        resp.setData(statisticsList);
        resp.setSize(statisticsList.stream().mapToLong(FileStatisticsResp::getSize).sum());
        resp.setNumber(statisticsList.stream().mapToLong(FileStatisticsResp::getNumber).sum());
        return resp;
    }

    @Override
    public byte[] load(FileQuery query) {
        List<String> paths = query.getPaths();
        // 创建内存输出流用于存储zip数据
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String filePath : paths) {
                Path path = Paths.get(filePath);
                // 检查文件是否存在
                if (!Files.exists(path)) {
                    throw new FileNotFoundException("文件不存在: " + filePath);
                }
                // 创建zip条目
                String fileName = path.getFileName().toString();
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);

                // 读取文件内容并写入zip
                try (InputStream fis = Files.newInputStream(path)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                } catch (IOException e) {
                    log.error("文件读取失败", e);
                    throw new RuntimeException(e);
                }
                zos.closeEntry();
            }
        } catch (IOException e) {
            log.error("zip文件创建失败", e);
        }

        return baos.toByteArray();
    }

    @Override
    protected void fill(Object obj) {
        super.fill(obj);
        if (obj instanceof FileResp fileResp && !URLUtils.isHttpUrl(fileResp.getUrl())) {
            StorageDO storage = storageService.getById(fileResp.getStorageId());
            String prefix = StrUtil.appendIfMissing(storage.getDomain(), StringConstants.SLASH);
            String url = URLUtil.normalize(prefix + fileResp.getUrl());
            fileResp.setUrl(url);
            String thumbnailUrl = StrUtils.blankToDefault(fileResp.getThumbnailUrl(), url, thUrl -> URLUtil
                    .normalize(prefix + thUrl));
            fileResp.setThumbnailUrl(thumbnailUrl);
            fileResp.setStorageName("%s (%s)".formatted(storage.getName(), storage.getCode()));
        }
    }
}