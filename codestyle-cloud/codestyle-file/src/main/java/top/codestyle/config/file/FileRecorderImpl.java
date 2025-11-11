package top.codestyle.config.file;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Component;
import top.codestyle.context.UserContextHolder;
import top.codestyle.mapper.FileMapper;
import top.codestyle.mapper.StorageMapper;
import top.codestyle.model.entity.FileDO;
import top.codestyle.model.entity.StorageDO;
import top.codestyle.model.enums.FileTypeEnum;
import top.continew.starter.core.constant.StringConstants;

import java.util.Optional;

/**
 * 文件记录实现类
 *
 * @author Charles7c
 * @since 2023/12/24 22:31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileRecorderImpl implements FileRecorder {

    private final FileMapper fileMapper;
    private final StorageMapper storageMapper;

    @Override
    public boolean save(FileInfo fileInfo) {
        FileDO file = new FileDO();
        String originalFilename = EscapeUtil.unescape(fileInfo.getOriginalFilename());
        file.setName(StrUtil.contains(originalFilename, StringConstants.DOT)
            ? StrUtil.subBefore(originalFilename, StringConstants.DOT, true)
            : originalFilename);
        file.setUrl(fileInfo.getUrl());
        file.setSize(fileInfo.getSize());
        file.setExtension(fileInfo.getExt());
        file.setType(FileTypeEnum.getByExtension(file.getExtension()).getValue());
        file.setThumbnailUrl(fileInfo.getThUrl());
        file.setThumbnailSize(fileInfo.getThSize());
        StorageDO storage = (StorageDO)fileInfo.getAttr().get(ClassUtil.getClassName(StorageDO.class, false));
        file.setStorageId(storage.getId());
        file.setCreateTime(DateUtil.toLocalDateTime(fileInfo.getCreateTime()));
        file.setUpdateUser(UserContextHolder.getUserId());
        file.setUpdateTime(file.getCreateTime());
        fileMapper.insert(file);
        return true;
    }

    @Override
    public FileInfo getByUrl(String url) {
        FileDO file = this.getFileByUrl(url);
        if (null == file) {
            return null;
        }
        StorageDO storageDO = storageMapper.lambdaQuery().eq(StorageDO::getId, file.getStorageId()).one();
        return file.toFileInfo(storageDO);
    }

    @Override
    public boolean delete(String url) {
        FileDO file = this.getFileByUrl(url);
        return fileMapper.lambdaUpdate().eq(FileDO::getUrl, file.getUrl()).remove();
    }

    @Override
    public void update(FileInfo fileInfo) {
        /* 不使用分片功能则无需重写 */
    }

    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {
        /* 不使用分片功能则无需重写 */
    }

    @Override
    public void deleteFilePartByUploadId(String s) {
        /* 不使用分片功能则无需重写 */
    }

    /**
     * 根据 URL 查询文件
     *
     * @param url URL
     * @return 文件信息
     */
    private FileDO getFileByUrl(String url) {
        Optional<FileDO> fileOptional = fileMapper.lambdaQuery().eq(FileDO::getUrl, url).oneOpt();
        return fileOptional.orElseGet(() -> fileMapper.lambdaQuery()
            .likeLeft(FileDO::getUrl, StrUtil.subAfter(url, StringConstants.SLASH, true))
            .oneOpt()
            .orElse(null));
    }
}