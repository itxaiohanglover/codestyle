import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.codestyle.model.mysql.dao.RemoteMetaInfo;
import top.codestyle.mapper.RemoteMetaInfoMapper;
import top.codestyle.services.SyncService;

/**
 * 增量同步测试类
 * 用于测试MySQL数据更新后，ES是否能正确同步
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = top.codestyle.RemoteSearchApplication.class)
public class IncrementalSyncTest {

    @Autowired
    private RemoteMetaInfoMapper remoteMetaInfoMapper;
    
    @Autowired
    private SyncService syncService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 测试增量同步
     * 1. 更新MySQL中的记录
     * 2. 执行增量同步
     * 3. 验证ES中是否更新了对应的数据
     */
    @Test
    public void testIncrementalSync() throws JsonProcessingException, InterruptedException {
        // 1. 查询ID为6的记录
        RemoteMetaInfo metaInfo = remoteMetaInfoMapper.selectById(6L);
        System.out.println("更新前的记录: " + metaInfo);
        
        // 2. 修改metaJson字段
        // 创建新的JSON内容
        String newMetaJson = "{\"groupId\": \"MachineLearning\", \"artifactId\": \"test-updated\", \"description\": \"测试更新后的描述\", \"config\": {}}";
        metaInfo.setMetaJson(newMetaJson);
        
        // 3. 更新记录
        int updateResult = remoteMetaInfoMapper.updateById(metaInfo);
        System.out.println("更新结果: " + updateResult);
        
        // 4. 手动执行增量同步（模拟定时任务）
        System.out.println("执行增量同步...");
        int syncResult = syncService.incrementalSync(java.time.LocalDateTime.now().minusMinutes(1));
        System.out.println("增量同步结果: " + syncResult);
        
        // 5. 验证更新后的记录
        RemoteMetaInfo updatedMetaInfo = remoteMetaInfoMapper.selectById(6L);
        System.out.println("更新后的记录: " + updatedMetaInfo);
        
        // 6. 等待几秒钟，让ES同步完成
        System.out.println("等待3秒，让ES同步完成...");
        Thread.sleep(3000);
        
        System.out.println("增量同步测试完成。请手动检查ES中是否能搜索到更新后的数据。");
    }
}
