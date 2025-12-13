

package top.codestyle.respository.es;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;
import top.codestyle.model.es.entity.RemoteMetaDoc;
import top.codestyle.vo.SearchMetaVO;

import java.util.Optional;

/**
 * RemoteSearchESRepository - 使用 Spring Data Elasticsearch 查询
 */
@Slf4j
@AllArgsConstructor
@Component
public class RemoteSearchESRepository {

    private final RemoteMetaDocRepository remoteMetaDocRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 执行搜索
     */
    public Optional<SearchMetaVO> searchInES(String keyword) {
        try {
            // 使用Spring Data Elasticsearch执行简单的match查询
            Criteria criteria = new Criteria("description").matches(keyword);
            CriteriaQuery query = new CriteriaQuery(criteria);
            query.setMaxResults(1);
            
            SearchHits<RemoteMetaDoc> searchHits = elasticsearchOperations.search(query, RemoteMetaDoc.class);
            
            if (searchHits.isEmpty()) {
                log.info("未找到匹配关键词: {} 的模板", keyword);
                return Optional.empty();
            }
            
            RemoteMetaDoc remoteMetaDoc = searchHits.getSearchHit(0).getContent();
            SearchMetaVO resultVO = converToResultVO(remoteMetaDoc);
            log.debug("检索出的本模板结果: {}", resultVO);
            return Optional.of(resultVO);

        } catch (Exception e) {
            log.error("检索异常: {}，尝试返回兜底数据", e.getMessage(), e);
            // TODO: 实现兜底数据逻辑
            return Optional.empty();
        }
    }

    /**
     * 转换为 VO
     */
    private SearchMetaVO converToResultVO(RemoteMetaDoc remoteMetaDoc) {
        SearchMetaVO searchMetaVO = new SearchMetaVO();
        searchMetaVO.setGroupId(remoteMetaDoc.getGroupId());
        searchMetaVO.setArtifactId(remoteMetaDoc.getArtifactId());
        searchMetaVO.setDescription(remoteMetaDoc.getDescription());
        searchMetaVO.setConfig(remoteMetaDoc.getConfig());
        return searchMetaVO;
    }

    /**
     * 保存文档到ES
     * 
     * @param metaDoc 要保存的ES文档
     * @return 是否保存成功
     */
    public boolean saveMetaDoc(RemoteMetaDoc metaDoc) {
        try {
            remoteMetaDocRepository.save(metaDoc);
            log.info("保存文档到ES成功: {}", metaDoc.getId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("保存文档到ES失败: {}", e.getMessage());
            throw new RuntimeException("保存文档到ES失败", e);
        }
    }

    /**
     * 从ES中删除指定ID的文档
     * 
     * @param id 要删除的文档ID
     * @return 是否删除成功
     */
    public boolean deleteMetaDoc(Long id) {
        try {
            remoteMetaDocRepository.deleteById(id);
            log.info("从ES中删除ID为 {} 的文档成功", id);
            return true;
        } catch (Exception e) {
            log.error("从ES中删除ID为 {} 的文档失败: {}", id, e.getMessage());
            return false;
        }
    }

    /**
     * 删除ES中的所有数据
     * 注意：此方法只用于全量同步，不应频繁调用
     */
    public void deleteAllMetaDocs() {
        try {
            remoteMetaDocRepository.deleteAll();
            log.info("成功删除ES中所有数据");
        } catch (Exception e) {
            log.error("删除ES中所有数据失败: {}", e.getMessage());
            // 不抛出异常，允许继续执行后续同步步骤
            log.info("删除操作异常，继续执行后续同步步骤");
        }
    }

    /**
     * 获取ES中所有文档的ID
     * 用于对比MySQL和ES数据，处理物理删除
     * 
     * @return ES中所有文档的ID列表
     */
    public Iterable<Long> getAllMetaDocIds() {
        try {
            return remoteMetaDocRepository.findAllIds();
        } catch (Exception e) {
            log.error("获取ES中所有文档ID失败: {}", e.getMessage());
            throw new RuntimeException("获取ES中所有文档ID失败", e);
        }
    }
}
