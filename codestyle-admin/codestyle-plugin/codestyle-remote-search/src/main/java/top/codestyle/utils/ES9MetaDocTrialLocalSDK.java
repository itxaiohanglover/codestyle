package top.codestyle.utils;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codestyle.model.es.entity.RemoteMetaDoc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Component
public class ES9MetaDocTrialLocalSDK {

    public RemoteMetaDoc searchES9(String keyword, String host,String indexName) throws IOException, InterruptedException {
        String searchUrl = host +"/" + indexName + "/_search";
        String safeKeyword = getSafeKeyword(keyword);
        String jsonQuery = """
        {
          "size": 1,
          "_source": ["id","groupId","artifactId","description","config"],
          "retriever": {
            "rrf": {
              "retrievers": [
                {
                  "standard": {
                    "query": {
                      "match": {
                        "description.text": %s
                      }
                    }
                  }
                },
                {
                  "standard": {
                    "query": {
                      "semantic": {
                        "field": "description.semantic",
                        "query": %s
                      }
                    }
                  }
                }
              ]
            }
          }
        }
        """.formatted(safeKeyword,safeKeyword);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(searchUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonQuery))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonString = response.body();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonString);
        // ES的RRF会自动按score降序排序，所以第一个就是score最高的结果

        JsonNode hits = root.path("hits").path("hits");
        if(hits.isEmpty()) return null;
        JsonNode sourceNode = hits.get(0).path("_source");

        RemoteMetaDoc doc = mapper.treeToValue(sourceNode, RemoteMetaDoc.class);

        return doc;
    }

    private static String getSafeKeyword(String keyword) {
        if (keyword == null) {
            return "\"\"";
        }
        
        StringBuilder safeKeyword = new StringBuilder();
        safeKeyword.append('"');
        
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            
            // 基本JSON转义
            switch (c) {
                case '"':
                    safeKeyword.append("\\\"");
                    break;
                case '\\':
                    safeKeyword.append("\\\\");
                    break;
                case '/':
                    safeKeyword.append("\\/");
                    break;
                case '\b':
                    safeKeyword.append("\\b");
                    break;
                case '\f':
                    safeKeyword.append("\\f");
                    break;
                case '\n':
                    safeKeyword.append("\\n");
                    break;
                case '\r':
                    safeKeyword.append("\\r");
                    break;
                case '\t':
                    safeKeyword.append("\\t");
                    break;
                // 防止SQL注入相关字符
                case '\'':
                    safeKeyword.append("\\'");
                    break;
                case ';':
                    safeKeyword.append("\\;");
                    break;
                case '=':
                    safeKeyword.append("\\=");
                    break;
                case '(': 
                    safeKeyword.append("\\(");
                    break;
                case ')':
                    safeKeyword.append("\\)");
                    break;
                case '|':
                    safeKeyword.append("\\|");
                    break;
                case '&':
                    safeKeyword.append("\\&");
                    break;
                case '%':
                    safeKeyword.append("\\%");
                    break;
                case '*':
                    safeKeyword.append("\\*");
                    break;
                case '+':
                    safeKeyword.append("\\+");
                    break;
                case '-':
                    // 检查是否是注释开头
                    if (i + 1 < keyword.length() && keyword.charAt(i + 1) == '-') {
                        safeKeyword.append("\\--");
                        i++;
                    } else {
                        safeKeyword.append("\\-");
                    }
                    break;
                default:
                    // 允许可见ASCII字符和汉字，过滤掉控制字符
                    if ((c >= 32 && c <= 126) || (c >= 0x4e00 && c <= 0x9fa5)) {
                        safeKeyword.append(c);
                    }
                    break;
            }
        }
        
        safeKeyword.append('"');
        return safeKeyword.toString();
    }

    /**
     * 保存文档到ES
     * @param doc 要保存的文档
     * @param host ES主机地址
     * @param indexName 索引名称
     * @return 是否保存成功
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean saveDocToES(RemoteMetaDoc doc, String host, String indexName) throws IOException, InterruptedException {
        String saveUrl = host + "/" + indexName + "/_doc/" + doc.getId();
        
        ObjectMapper mapper = new ObjectMapper();
        String jsonDoc = mapper.writeValueAsString(doc);
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(saveUrl))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonDoc))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return true;
        }
        
        System.out.println("保存文档失败，状态码: " + statusCode + ", 响应: " + response.body());
        return false;
    }
}
