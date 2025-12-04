import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import top.codestyle.model.es.entity.RemoteMetaDoc;
import top.codestyle.model.mysql.dao.RemoteMetaInfo;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MySQL模型类RemoteMetaInfo的单元测试
 * 主要测试JSON解析功能
 */
@Slf4j
public class RemoteMetaInfoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试从meta_json解析为RemoteMetaDoc
     */
    @Test
    public void testToRemoteMetaDoc() throws JsonProcessingException {
        // 1. 准备测试数据
        String testJson = "{\n" +
                "  \"id\": 123,\n" +
                "  \"groupId\": \"top.codestyle\",\n" +
                "  \"artifactId\": \"test-template\",\n" +
                "  \"description\": \"测试模板\",\n" +
                "  \"config\": {\n" +
                "    \"version\": \"1.0.0\",\n" +
                "    \"files\": [\n" +
                "      {\n" +
                "        \"filename\": \"test.txt\",\n" +
                "        \"sha256\": \"abc123\",\n" +
                "        \"filePath\": \"src/test.txt\",\n" +
                "        \"description\": \"测试文件\",\n" +
                "        \"inputVariables\": [\n" +
                "          {\n" +
                "            \"variableType\": \"String\",\n" +
                "            \"variableName\": \"testVar\",\n" +
                "            \"variableComment\": \"测试变量\",\n" +
                "            \"example\": \"testValue\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        // 2. 创建RemoteMetaInfo对象
        RemoteMetaInfo remoteMetaInfo = new RemoteMetaInfo();
        remoteMetaInfo.setId(123L);
        remoteMetaInfo.setMetaJson(testJson);
        remoteMetaInfo.setGroupId("top.codestyle");

        // 3. 测试JSON解析
        RemoteMetaDoc remoteMetaDoc = remoteMetaInfo.toRemoteMetaDoc();

        // 4. 验证解析结果
        assertNotNull(remoteMetaDoc, "解析结果不应为null");
        assertEquals(123L, remoteMetaDoc.getId(), "ID应匹配");
        assertEquals("top.codestyle", remoteMetaDoc.getGroupId(), "groupId应匹配");
        assertEquals("test-template", remoteMetaDoc.getArtifactId(), "artifactId应匹配");
        assertEquals("测试模板", remoteMetaDoc.getDescription(), "description应匹配");

        // 验证config字段
        assertNotNull(remoteMetaDoc.getConfig(), "config不应为null");
        assertEquals("1.0.0", remoteMetaDoc.getConfig().getVersion(), "version应匹配");

        // 验证files字段
        assertNotNull(remoteMetaDoc.getConfig().getFiles(), "files不应为null");
        assertEquals(1, remoteMetaDoc.getConfig().getFiles().size(), "files数量应匹配");

        // 验证file详情
        RemoteMetaDoc.Config.File file = remoteMetaDoc.getConfig().getFiles().get(0);
        assertEquals("test.txt", file.getFilename(), "filename应匹配");
        assertEquals("abc123", file.getSha256(), "sha256应匹配");
        assertEquals("src/test.txt", file.getFilePath(), "filePath应匹配");

        // 验证inputVariables字段
        assertNotNull(file.getInputVariables(), "inputVariables不应为null");
        assertEquals(1, file.getInputVariables().size(), "inputVariables数量应匹配");

        // 验证inputVariable详情
        RemoteMetaDoc.Config.File.InputVariable variable = file.getInputVariables().get(0);
        assertEquals("String", variable.getVariableType(), "variableType应匹配");
        assertEquals("testVar", variable.getVariableName(), "variableName应匹配");
        assertEquals("测试变量", variable.getVariableComment(), "variableComment应匹配");
        assertEquals("testValue", variable.getExample(), "example应匹配");

        log.info("JSON解析测试通过，解析结果: {}", remoteMetaDoc);
    }

    /**
     * 测试JSON格式错误的情况
     */
    @Test
    public void testToRemoteMetaDocWithInvalidJson() {
        // 准备格式错误的JSON
        String invalidJson = "{\"id\": 123, \"groupId\": \"top.codestyle\", \"artifactId\": \"test-template\"";

        RemoteMetaInfo remoteMetaInfo = new RemoteMetaInfo();
        remoteMetaInfo.setId(123L);
        remoteMetaInfo.setMetaJson(invalidJson);

        // 验证解析异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            remoteMetaInfo.toRemoteMetaDoc();
        });

        assertTrue(exception.getMessage().contains("解析metaJson失败"), "异常信息应包含解析失败");
        log.info("JSON格式错误测试通过，异常信息: {}", exception.getMessage());
    }

    /**
     * 测试空JSON的情况
     */
    @Test
    public void testToRemoteMetaDocWithNullJson() {
        RemoteMetaInfo remoteMetaInfo = new RemoteMetaInfo();
        remoteMetaInfo.setId(123L);
        remoteMetaInfo.setMetaJson(null);

        // 验证解析异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            remoteMetaInfo.toRemoteMetaDoc();
        });

        log.info("空JSON测试通过，异常信息: {}", exception.getMessage());
    }
}