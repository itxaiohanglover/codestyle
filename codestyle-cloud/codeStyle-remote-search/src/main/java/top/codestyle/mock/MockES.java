package top.codestyle.init;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class MockES {

    private ElasticsearchClient esClient;


    /**
     * 清空 meta_info 全部文档
     */
    public void clearIndex() throws Exception {
        DeleteByQueryRequest req = DeleteByQueryRequest.of(b -> b
                .index("meta_info")
                .query(q -> q.matchAll(m -> m))
        );

        esClient.deleteByQuery(req);
    }

    /**
     * 插入 Mock 数据（符合数组结构）
     */
    public void insertMockData() throws Exception {

        // === 构造 Mock 数据 ===

        RemoteMetaConfig.InputVariable var1 = new RemoteMetaConfig.InputVariable(
                "String",
                "inputFilePath",
                "输入文件路径",
                "data.csv"
        );

        RemoteMetaConfig.InputVariable var2 = new RemoteMetaConfig.InputVariable(
                "String",
                "outputFilePath",
                "输出文件路径",
                "cleaned_data.csv"
        );

        RemoteMetaConfig.FileInfo file = new RemoteMetaConfig.FileInfo(
                "data_cleaning.py.ftl",
                "a1b2c3d4e5...",
                "src/data_cleaning",
                "数据清洗脚本模板",
                List.of(var1, var2)
        );

        RemoteMetaConfig.Config config = new RemoteMetaConfig.Config(
                "1.0.0",
                List.of(file) // ★ 重点：files 是数组！
        );

        MetaInfoDocument doc = new MetaInfoDocument(
                3L,
                "DataScience",
                "PyData-Cleaning",
                "Python template for cleaning and transforming data with Pandas and NumPy. 支持 CSV 和 JSON 数据的快速清洗与转换。",
                config
        );

        // === 写入 ES ===

        IndexRequest<MetaInfoDocument> req = IndexRequest.of(b -> b
                .index("meta_info")
                .id(doc.getId().toString())
                .document(doc)
        );

        esClient.index(req);
    }


    // ============================================================
    // VO 类，与你的结构完全一致
    // ============================================================

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MetaInfoDocument {
        private Long id;
        private String groupId;
        private String artifactId;
        private String description;
        private RemoteMetaConfig.Config config;
    }

    public static class RemoteMetaConfig {

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Config {
            private String version;
            private List<FileInfo> files;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class FileInfo {
            private String filename;
            private String sha256;
            private String filePath;
            private String description;
            private List<InputVariable> inputVariables;
        }

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class InputVariable {
            private String variableType;
            private String variableName;
            private String variableComment;
            private String example;
        }
    }
}
