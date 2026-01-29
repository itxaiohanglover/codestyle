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

package top.codestyle.admin.search.service;

import java.util.Set;

/**
 * 热点数据识别服务接口
 * 使用滑动窗口算法统计访问频率，自动识别热点关键词
 *
 * @author chonghaoGao
 * @date 2025/12/23
 */
public interface HotKeyDetectionService {

    /**
     * 记录关键词访问
     * 
     * @param query 搜索关键词
     */
    void recordAccess(String query);

    /**
     * 判断是否为热点关键词
     * 
     * @param query 搜索关键词
     * @return true表示是热点关键词
     */
    boolean isHotKey(String query);

    /**
     * 获取热点关键词列表
     * 
     * @param limit 返回数量限制
     * @return 热点关键词集合
     */
    Set<String> getHotKeys(int limit);

    /**
     * 获取关键词访问次数
     * 
     * @param query 搜索关键词
     * @return 访问次数
     */
    long getAccessCount(String query);
}
