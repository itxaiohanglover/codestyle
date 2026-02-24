/*
 * Copyright (c) 2022-present CodeStyle (codestyle.top)
 *
 * This project is licensed under the Apache License 2.0.
 */

package top.codestyle.admin.research.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 研究任务状态
 *
 * @author CodeStyle
 * @since 2026-02-22
 */
@Getter
@RequiredArgsConstructor
public enum ResearchStatus {

    /**
     * 待处理
     */
    PENDING("待处理"),

    /**
     * 运行中
     */
    RUNNING("运行中"),

    /**
     * 等待用户确认
     */
    WAITING_CONFIRM("等待用户确认"),

    /**
     * 已完成
     */
    COMPLETED("已完成"),

    /**
     * 已失败
     */
    FAILED("已失败"),

    /**
     * 已取消
     */
    CANCELLED("已取消");

    /**
     * 状态描述
     */
    private final String description;
}

