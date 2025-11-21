package top.codestyle.entity.es.vo;

import lombok.Data;
import top.codestyle.entity.es.pojo.CodeStyleTemplateDO;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP搜索结果VO实体类
 */
@Data
public class McpSearchResultVO {

    private String id;
    private String groupId;
    private String artifactId;
    private Config config;

    /**
     * 配置类
     */
    @Data
    public static class Config {
        private String version;
        private List<FileInfo> files;
    }

    /**
     * 文件信息类
     */
    @Data
    public static class FileInfo {
        private String filePath;
        private String description;
        private String filename;
        private List<InputVariable> inputVariables;
        private String sha256;
    }

    /**
     * 输入变量类
     */
    @Data
    public static class InputVariable {
        private String variableName;
        private String variableType;
        private String variableComment;
        private String example;
    }


}