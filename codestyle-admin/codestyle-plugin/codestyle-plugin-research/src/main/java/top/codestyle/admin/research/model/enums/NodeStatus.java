/*
 * Copyright (c) 2022-present CodeStyle Authors. All Rights Reserved.
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

package top.codestyle.admin.research.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 节点状态
 *
 * @author CodeStyle
 * @since 2026-02-22
 */
@Getter
@RequiredArgsConstructor
public enum NodeStatus {

    /**
     * 待执行
     */
    PENDING("待执行"),

    /**
     * 运行中
     */
    RUNNING("运行中"),

    /**
     * 已完成
     */
    COMPLETED("已完成"),

    /**
     * 已失败
     */
    FAILED("已失败"),

    /**
     * 已跳过
     */
    SKIPPED("已跳过");

    /**
     * 状态描述
     */
    private final String description;
}
