package top.codestyle.respository.es;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codestyle.model.es.entity.RemoteMetaDoc;
import top.codestyle.properties.ElasticSearchUrlProperties;
import top.codestyle.utils.ES9MetaDocTrialLocalSDK;
import top.codestyle.vo.SearchMetaVO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 19:09)
 */


/**
 * RemoteSearchESRepository - 使用 ES9 官方客户端直接查询
 */
@Slf4j
@AllArgsConstructor
@Component
public class RemoteSearchESRepository {

    private final ES9MetaDocTrialLocalSDK es9MetaDocTrialLocalSDK;

    private final ElasticSearchUrlProperties elasticSearchUrlProperties;
    /**
     * 执行搜索
     */
    public Optional<SearchMetaVO> searchInES(String keyword) {
        try {
            RemoteMetaDoc remoteMetaDoc = es9MetaDocTrialLocalSDK.searchES9(
                    keyword,
                    elasticSearchUrlProperties.getHost(),
                    elasticSearchUrlProperties.getIndexName()
            );
            
            if (remoteMetaDoc == null) {
                log.info("未找到匹配关键词: {} 的模板", keyword);
                return Optional.empty();
            }

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
     * @param metaDoc 要保存的ES文档
     * @return 是否保存成功
     */
    public boolean saveMetaDoc(RemoteMetaDoc metaDoc) {
        try {
            return es9MetaDocTrialLocalSDK.saveDocToES(
                    metaDoc,
                    elasticSearchUrlProperties.getHost(),
                    elasticSearchUrlProperties.getIndexName()
            );
        } catch (Exception e) {
            log.error("保存文档到ES失败: {}", e.getMessage());
            throw new RuntimeException("保存文档到ES失败", e);
        }
    }
    
    /**
     * 从ES中删除指定ID的文档
     * @param id 要删除的文档ID
     * @return 是否删除成功
     */
    public boolean deleteMetaDoc(Long id) {
        try {
            // 构建删除URL
            String deleteUrl = elasticSearchUrlProperties.getHost() + "/" + 
                              elasticSearchUrlProperties.getIndexName() + "/_doc/" + id;
            
            // 执行删除请求
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(deleteUrl))
                    .DELETE()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // 检查响应状态码
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("从ES中删除ID为 {} 的文档成功", id);
                return true;
            } else {
                log.warn("从ES中删除ID为 {} 的文档失败，状态码: {}", id, response.statusCode());
                return false;
            }
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
            // 添加conflicts=proceed参数，允许跳过版本冲突的文档
            String deleteUrl = elasticSearchUrlProperties.getHost() + "/" +
                    elasticSearchUrlProperties.getIndexName() + "/_delete_by_query?conflicts=proceed";
            String deleteQuery = "{\"query\": {\"match_all\": {}}}";
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(deleteUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(deleteQuery))
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("成功删除ES中所有数据");
            } else {
                log.error("删除ES中所有数据失败，状态码: {}, 响应: {}", response.statusCode(), response.body());
                // 版本冲突是预期中的，只要请求成功发送，就继续执行后续操作
                log.info("删除操作已发送，继续执行后续同步步骤");
            }
        } catch (Exception e) {
            log.error("删除ES中所有数据失败: {}", e.getMessage());
            // 不抛出异常，允许继续执行后续同步步骤
            log.info("删除操作异常，继续执行后续同步步骤");
        }
    }
}

