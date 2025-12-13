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

/**
 * 测试MyMetaObjectHandler的自动填充功能
 *
 * @author ChonghaoGao
 * @date 2025/12/4
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = top.codestyle.RemoteSearchApplication.class)
public class MetaObjectHandlerTest {

    @Autowired
    private RemoteMetaInfoMapper remoteMetaInfoMapper;

    /**
     * 测试插入数据时自动填充create_time和update_time
     */
    @Test
    public void testInsertFill() {
        System.out.println("开始测试插入数据时自动填充create_time和update_time...");

        // 创建一个新的RemoteMetaInfo对象
        RemoteMetaInfo metaInfo = new RemoteMetaInfo();
        metaInfo.setId(100L);
        metaInfo
            .setMetaJson("{\"groupId\": \"TestGroup\", \"artifactId\": \"test-insert\", \"description\": \"测试插入数据\", \"config\": {}}");

        // 插入数据
        int insertResult = remoteMetaInfoMapper.insert(metaInfo);
        System.out.println("插入结果: " + insertResult);

        // 查询插入的数据
        RemoteMetaInfo insertedMetaInfo = remoteMetaInfoMapper.selectById(100L);
        System.out.println("插入后的数据: " + insertedMetaInfo);
        System.out.println("create_time: " + insertedMetaInfo.getCreateTime());
        System.out.println("update_time: " + insertedMetaInfo.getUpdateTime());

        // 验证create_time和update_time是否被自动填充
        assert insertedMetaInfo.getCreateTime() != null : "create_time没有被自动填充";
        assert insertedMetaInfo.getUpdateTime() != null : "update_time没有被自动填充";

        System.out.println("✅ 自动填充测试通过！");
    }
}
