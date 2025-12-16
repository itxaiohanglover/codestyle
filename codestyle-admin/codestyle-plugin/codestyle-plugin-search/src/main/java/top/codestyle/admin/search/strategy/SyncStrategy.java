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

/**
 * 同步策略接口
 * 定义不同同步策略的通用方法
 *
 * @author ChonghaoGao
 * @date 2025/12/13
 */
public interface SyncStrategy {

    /**
     * 执行同步操作
     *
     * @param params 同步参数，可以是不同类型的参数
     * @return 同步成功的数量或状态
     */
    Object executeSync(Object... params);
}
