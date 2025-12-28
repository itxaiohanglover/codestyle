import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.codestyle.mapper.RemoteMetaInfoMapper;
import top.codestyle.model.mysql.dao.RemoteMetaInfo;
import top.codestyle.respository.es.RemoteSearchESRepository;
import top.codestyle.services.SyncService;

/**
 * 测试删除数据的同步
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = top.codestyle.RemoteSearchApplication.class)
public class DeleteSyncTest {

    @Autowired
    private RemoteMetaInfoMapper remoteMetaInfoMapper;
    
    @Autowired
    private SyncService syncService;
    
    @Autowired
    private RemoteSearchESRepository remoteSearchESRepository;
    
    /**
     * 测试删除数据的同步
     */
    @Test
    public void testDeleteSync() throws InterruptedException {
        System.out.println("开始测试删除数据的同步...");
        
        // 1. 首先确保测试数据存在
        RemoteMetaInfo existingMetaInfo = remoteMetaInfoMapper.selectById(200L);
        if (existingMetaInfo == null) {
            // 如果数据不存在，先插入
            System.out.println("测试数据不存在，先插入...");
            RemoteMetaInfo metaInfo = new RemoteMetaInfo();
            metaInfo.setId(200L);
            metaInfo.setMetaJson("{\"groupId\": \"TestGroup\", \"artifactId\": \"test-delete-sync\", \"description\": \"测试删除数据同步\", \"config\": {}}");
            remoteMetaInfoMapper.insert(metaInfo);
            Thread.sleep(1000);
        }
        
        // 2. 先搜索数据，确认ES中存在
        System.out.println("搜索'test-delete-sync'，确认ES中存在...");
        boolean beforeDelete = remoteSearchESRepository.searchInES("test-delete-sync").isPresent();
        System.out.println("删除前ES中是否存在: " + beforeDelete);
        
        // 3. 删除数据库中的数据
        System.out.println("删除数据库中的数据...");
        int deleteResult = remoteMetaInfoMapper.deleteById(200L);
        System.out.println("删除结果: " + deleteResult);
        
        // 4. 执行删除同步
        System.out.println("执行删除同步...");
        // 调用syncDeletedData方法，需要转换为SyncServiceImpl
        top.codestyle.services.impl.SyncServiceImpl syncServiceImpl = (top.codestyle.services.impl.SyncServiceImpl)syncService;
        int syncResult = syncServiceImpl.syncDeletedData();
        System.out.println("删除同步结果: " + syncResult);
        
        // 5. 等待ES同步完成
        System.out.println("等待2秒，让ES同步完成...");
        Thread.sleep(2000);
        
        // 6. 再次搜索数据，确认ES中已删除
        System.out.println("搜索'test-delete-sync'，确认ES中已删除...");
        boolean afterDelete = remoteSearchESRepository.searchInES("test-delete-sync").isPresent();
        System.out.println("删除后ES中是否存在: " + afterDelete);
        
        // 7. 验证结果
        if (!afterDelete) {
            System.out.println("✅ 成功删除ES中的数据！");
        } else {
            System.out.println("❌ ES中的数据未被删除！");
        }
    }
}