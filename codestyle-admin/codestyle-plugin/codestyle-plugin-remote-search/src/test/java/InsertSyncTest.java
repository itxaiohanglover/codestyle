/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import top.codestyle.mapper.RemoteMetaInfoMapper;
import top.codestyle.model.mysql.entity.RemoteMetaInfo;
import top.codestyle.respository.es.RemoteSearchESRepository;
import top.codestyle.services.SyncService;
import top.codestyle.vo.SearchMetaVO;

import java.util.Optional;

/**
 * 测试新插入数据的同步
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = top.codestyle.RemoteSearchApplication.class)
public class InsertSyncTest {

    @Autowired
    private RemoteMetaInfoMapper remoteMetaInfoMapper;

    @Autowired
    private SyncService syncService;

    @Autowired
    private RemoteSearchESRepository remoteSearchESRepository;

    /**
     * 测试新插入数据的同步
     */
    @Test
    public void testInsertSync() throws InterruptedException {
        System.out.println("开始测试新插入数据的同步...");

        // 1. 创建一个新的RemoteMetaInfo对象
        RemoteMetaInfo metaInfo = new RemoteMetaInfo();
        metaInfo.setId(200L);
        metaInfo
            .setMetaJson("{\"groupId\": \"TestGroup\", \"artifactId\": \"test-insert-sync\", \"description\": \"测试插入数据同步\", \"config\": {}}");

        // 2. 插入数据
        int insertResult = remoteMetaInfoMapper.insert(metaInfo);
        System.out.println("插入结果: " + insertResult);

        // 3. 查询插入的数据
        RemoteMetaInfo insertedMetaInfo = remoteMetaInfoMapper.selectById(200L);
        System.out.println("插入后的数据: " + insertedMetaInfo);

        // 4. 手动执行增量同步
        System.out.println("执行增量同步...");
        int syncResult = syncService.incrementalSync(java.time.LocalDateTime.now().minusMinutes(1));
        System.out.println("增量同步结果: " + syncResult);

        // 5. 等待ES同步完成
        System.out.println("等待2秒，让ES同步完成...");
        Thread.sleep(2000);

        // 6. 搜索新插入的数据
        System.out.println("搜索'test-insert-sync'...");
        Optional<SearchMetaVO> searchResult = remoteSearchESRepository.searchInES("test-insert-sync");

        // 7. 验证是否检索到了结果
        if (searchResult.isPresent()) {
            System.out.println("✅ 成功检索到新插入的数据！");
            System.out.println("检索到的结果: " + searchResult.get());
            // 验证artifactId是否正确
            assert searchResult.get().getArtifactId().equals("test-insert-sync") : "检索到的artifactId不正确";
        } else {
            System.out.println("❌ 未检索到新插入的数据！");
        }
    }
}
