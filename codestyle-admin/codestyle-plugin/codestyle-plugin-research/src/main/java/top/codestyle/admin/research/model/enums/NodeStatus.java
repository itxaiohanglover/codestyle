/*
 * Copyright (c) 2022-present CodeStyle (codestyle.top)
 *
 * This project is licensed under the Apache License 2.0.
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

