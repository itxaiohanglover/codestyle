package top.codestyle.model.es.entity;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class RemoteMetaDoc {
    private String groupId;
    private String artifactId;
    private String description;
    private Long id;
    private Config config;

    @Data
    public static class Config {
        private String version;
        private List<File> files;

        @Data
        public static class File {
            private String filename;
            private String sha256;
            private String filePath;
            private String description;
            private List<InputVariable> inputVariables;

            @Data
            public static class InputVariable {
                private String variableType;
                private String variableName;
                private String variableComment;
                private String example;

                // getters & setters
            }

            // getters & setters
        }

        // getters & setters
    }

    // getters & setters
}