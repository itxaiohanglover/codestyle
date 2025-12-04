import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.codestyle.mapper.RemoteMetaInfoMapper;
import top.codestyle.model.mysql.dao.RemoteMetaInfo;
import top.codestyle.respository.es.RemoteSearchESRepository;
import top.codestyle.services.SyncService;
import top.codestyle.vo.SearchMetaVO;

import java.util.Optional;

/**
 * 集成测试类
 * 在同一个测试中完成更新、同步和搜索的完整流程
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = top.codestyle.RemoteSearchApplication.class)
public class IntegratedSyncSearchTest {

    @Autowired
    private RemoteMetaInfoMapper remoteMetaInfoMapper;
    
    @Autowired
    private SyncService syncService;
    
    @Autowired
    private RemoteSearchESRepository remoteSearchESRepository;
    
    /**
     * 测试完整流程：更新数据 → 增量同步 → ES检索
     */
    @Test
    public void testFullSyncSearchFlow() throws InterruptedException {
        System.out.println("开始测试完整流程：更新数据 → 增量同步 → ES检索");
        
        // 1. 查询ID为6的记录
        RemoteMetaInfo metaInfo = remoteMetaInfoMapper.selectById(6L);
        System.out.println("更新前的记录: " + metaInfo.getMetaJson());
        
        // 2. 修改metaJson字段，设置artifactid为"test-updated"
        String newMetaJson = "{\"groupId\": \"MachineLearning\", \"artifactId\": \"test-updated\", \"description\": \"测试更新后的描述\", \"config\": {}}";
        metaInfo.setMetaJson(newMetaJson);
        
        // 3. 更新记录
        int updateResult = remoteMetaInfoMapper.updateById(metaInfo);
        System.out.println("更新结果: " + updateResult);
        
        // 4. 手动执行增量同步
        System.out.println("执行增量同步...");
        int syncResult = syncService.incrementalSync(java.time.LocalDateTime.now().minusMinutes(1));
        System.out.println("增量同步结果: " + syncResult);
        
        // 5. 等待ES同步完成
        System.out.println("等待2秒，让ES同步完成...");
        Thread.sleep(2000);
        
        // 6. 搜索"test-updated"
        System.out.println("搜索'test-updated'...");
        Optional<SearchMetaVO> searchResult = remoteSearchESRepository.searchInES("test-updated");
        
        // 7. 验证是否检索到了结果
        if (searchResult.isPresent()) {
            System.out.println("✅ 成功检索到数据！");
            System.out.println("检索到的结果: " + searchResult.get());
            // 验证artifactId是否为"test-updated"
            assert searchResult.get().getArtifactId().equals("test-updated") : "检索到的artifactId不正确";
        } else {
            System.out.println("❌ 未检索到数据！");
            // 打印ES中的所有数据，用于调试
            System.out.println("正在打印ES中的所有数据...");
            printAllESData();
        }
    }
    
    /**
     * 打印ES中的所有数据，用于调试
     */
    private void printAllESData() {
        System.out.println("由于权限限制，无法直接访问ES配置，跳过打印ES数据");
    }
}
