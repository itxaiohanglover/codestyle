
package top.codestyle.file.service;

import top.codestyle.model.dto.file.StorageReq;
import top.codestyle.model.entity.StorageDO;
import top.codestyle.model.query.StorageQuery;
import top.codestyle.model.vo.StorageResp;
import top.continew.starter.data.mp.service.IService;
import top.continew.starter.extension.crud.service.BaseService;


/**
 * 存储业务接口
 *
 * @author Charles7c
 * @since 2023/12/26 22:09
 */
public interface StorageService extends BaseService<StorageResp, StorageResp, StorageQuery, StorageReq>, IService<StorageDO> {

    /**
     * 查询默认存储
     *
     * @return 存储信息
     */
    StorageDO getDefaultStorage();

    /**
     * 根据编码查询
     *
     * @param code 编码
     * @return 存储信息
     */
    StorageDO getByCode(String code);

    /**
     * 加载存储
     *
     * @param req 存储信息
     */
    void load(StorageReq req);

    /**
     * 卸载存储
     *
     * @param req 存储信息
     */
    void unload(StorageReq req);
}