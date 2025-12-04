package top.codestyle.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.codestyle.model.es.entity.RemoteMetaDoc;

/**
 * @author ChonghaoGao
 * @date 2025/12/1 19:07)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchMetaVO {


    /**
     * 组织名(用户名)
     */
    private String groupId;

    /**
     * 模板组名
     */
    private String artifactId;

    /**
     * 模板组总体描述
     */
    private String description;

    /**
     * 单个版本配置对象
     */
    private RemoteMetaDoc.Config config;
}
