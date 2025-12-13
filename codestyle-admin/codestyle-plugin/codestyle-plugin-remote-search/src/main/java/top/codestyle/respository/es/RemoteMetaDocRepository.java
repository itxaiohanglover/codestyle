package top.codestyle.respository.es;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import top.codestyle.model.es.entity.RemoteMetaDoc;

/**
 * RemoteMetaDocRepository - Spring Data Elasticsearch 仓库接口
 * 用于实现ES查询
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
public interface RemoteMetaDocRepository extends ElasticsearchRepository<RemoteMetaDoc, Long> {
    // Spring Data Elasticsearch会根据方法名自动生成查询
    // 这里只需要简单查询description关键字
    RemoteMetaDoc findByDescriptionContaining(String keyword);
    
    /**
     * 获取所有文档的ID
     * 使用@Query注解自定义查询，只返回ID字段
     *
     * @return 所有文档的ID列表
     */
    @Query("{\"match_all\": {}}")
    Iterable<Long> findAllIds();
}