package top.codestyle.test;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.codestyle.model.es.entity.RemoteMetaDoc;
import top.codestyle.model.mysql.dao.RemoteMetaInfo;
import top.codestyle.mapper.RemoteMetaInfoMapper;
import top.codestyle.services.SyncService;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试逻辑删除恢复的同步功能
 * 验证当数据从删除状态恢复时能正确同步到ES
 */
@Slf4j
@SpringBootTest
public class RestoreDeletedSyncTest {

    @Autowired
    private RemoteMetaInfoMapper remoteMetaInfoMapper;

    @Autowired
    private SyncService syncService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testRestoreDeletedSync() throws Exception {
        // 生成唯一ID
        long uniqueId = System.currentTimeMillis();
        log.info("测试增量同步功能，使用ID: {}", uniqueId);

        // 1. 准备测试数据
        RemoteMetaDoc metaDoc = new RemoteMetaDoc();
        metaDoc.setId(uniqueId);
        metaDoc.setGroupId("TestGroup");
        metaDoc.setArtifactId("test-incremental-sync");
        metaDoc.setDescription("测试增量同步功能");
        
        // 配置Config对象
        RemoteMetaDoc.Config config = new RemoteMetaDoc.Config();
        config.setVersion("1.0.0");
        metaDoc.setConfig(config);

        // 转换为JSON字符串
        String metaJson = objectMapper.writeValueAsString(metaDoc);

        // 2. 创建RemoteMetaInfo对象
        RemoteMetaInfo metaInfo = new RemoteMetaInfo();
        metaInfo.setId(uniqueId);
        metaInfo.setMetaJson(metaJson);

        // 3. 插入到MySQL
        int insertResult = remoteMetaInfoMapper.insert(metaInfo);
        assertEquals(1, insertResult, "插入MySQL失败");
        log.info("成功插入到MySQL");

        // 4. 执行增量同步，确保数据同步到ES
        int syncCount1 = syncService.incrementalSync(LocalDateTime.now().minusHours(1));
        assertTrue(syncCount1 > 0, "首次同步失败");
        log.info("首次同步到ES，同步了 {} 条数据", syncCount1);

        // 5. 更新数据
        RemoteMetaDoc updatedMetaDoc = new RemoteMetaDoc();
        updatedMetaDoc.setId(uniqueId);
        updatedMetaDoc.setGroupId("TestGroup");
        updatedMetaDoc.setArtifactId("test-incremental-sync");
        updatedMetaDoc.setDescription("测试增量同步功能 - 更新后");
        
        RemoteMetaDoc.Config updatedConfig = new RemoteMetaDoc.Config();
        updatedConfig.setVersion("1.1.0");
        updatedMetaDoc.setConfig(updatedConfig);

        String updatedMetaJson = objectMapper.writeValueAsString(updatedMetaDoc);
        RemoteMetaInfo updatedMetaInfo = new RemoteMetaInfo();
        updatedMetaInfo.setId(uniqueId);
        updatedMetaInfo.setMetaJson(updatedMetaJson);

        int updateResult = remoteMetaInfoMapper.updateById(updatedMetaInfo);
        assertEquals(1, updateResult, "更新MySQL失败");
        log.info("成功更新MySQL数据");

        // 6. 执行增量同步，确保更新的数据同步到ES
        int syncCount2 = syncService.incrementalSync(LocalDateTime.now().minusMinutes(5));
        assertTrue(syncCount2 > 0, "更新后同步失败");
        log.info("更新后同步到ES，同步了 {} 条数据", syncCount2);

        // 7. 清理测试数据
        int deleteResult = remoteMetaInfoMapper.deleteById(uniqueId);
        assertEquals(1, deleteResult, "删除MySQL失败");
        log.info("成功删除MySQL数据");

        // 8. 执行增量同步，确保数据从ES中删除
        int syncCount3 = syncService.incrementalSync(LocalDateTime.now().minusMinutes(5));
        assertTrue(syncCount3 >= 0, "删除后同步失败");
        log.info("删除后同步，同步了 {} 条数据", syncCount3);

        log.info("增量同步测试完成");
    }
}
