/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this fileInfo except in compliance with the License.
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

package top.codestyle.admin.search.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.codestyle.admin.search.strategy.impl.DeletedSyncStrategy;
import top.codestyle.admin.search.strategy.impl.FullSyncStrategy;
import top.codestyle.admin.search.strategy.impl.IncrementalSyncStrategy;
import top.codestyle.admin.search.strategy.impl.SingleSyncStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * 同步策略工厂
 * 管理和提供不同的同步策略实例
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
@Component
public class SyncStrategyFactory {

    private final Map<String, SyncStrategy> strategyMap;

    @Autowired
    public SyncStrategyFactory(FullSyncStrategy fullSyncStrategy,
                               IncrementalSyncStrategy incrementalSyncStrategy,
                               SingleSyncStrategy singleSyncStrategy,
                               DeletedSyncStrategy deletedSyncStrategy) {

        this.strategyMap = new HashMap<>();
        // 注册所有同步策略
        strategyMap.put("fullSync", fullSyncStrategy);
        strategyMap.put("incrementalSync", incrementalSyncStrategy);
        strategyMap.put("singleSync", singleSyncStrategy);
        strategyMap.put("deletedSync", deletedSyncStrategy);
    }

    /**
     * 获取指定的同步策略
     *
     * @param strategyName 策略名称
     * @return 同步策略实例
     * @throws IllegalArgumentException 如果策略名称无效
     */
    public SyncStrategy getSyncStrategy(String strategyName) {
        SyncStrategy strategy = strategyMap.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("无效的同步策略名称: " + strategyName);
        }
        return strategy;
    }
}
