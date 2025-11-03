package top.codestyle.file.service;

import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.web.multipart.MultipartFile;
import top.codestyle.model.dto.file.FileReq;
import top.codestyle.model.entity.FileDO;
import top.codestyle.model.query.FileQuery;
import top.codestyle.model.vo.FileResp;
import top.codestyle.model.vo.FileStatisticsResp;
import top.continew.starter.data.mp.service.IService;
import top.continew.starter.extension.crud.service.BaseService;

import java.util.List;

/**
 * 文件业务接口
 *
 * @author Charles7c
 * @since 2023/12/23 10:38
 */
public interface FileService extends BaseService<FileResp, FileResp, FileQuery, FileReq>, IService<FileDO> {

    /**
     * 上传到默认存储
     *
     * @param file 文件信息
     * @return 文件信息
     */
    default FileInfo upload(MultipartFile file) {
        return upload(file, null);
    }

    /**
     * 上传到指定存储
     *
     * @param file        文件信息
     * @param storageCode 存储编码
     * @return 文件信息
     */
    FileInfo upload(MultipartFile file, String storageCode);

    /**
     * 根据存储 ID 列表查询
     *
     * @param storageIds 存储 ID 列表
     * @return 文件数量
     */
    Long countByStorageIds(List<Long> storageIds);

    /**
     * 查询文件资源统计信息
     *
     * @return 资源统计信息
     */
    FileStatisticsResp statistics();
}