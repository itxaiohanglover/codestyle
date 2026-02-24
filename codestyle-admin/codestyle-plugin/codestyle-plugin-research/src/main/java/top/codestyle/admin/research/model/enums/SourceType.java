/*
 * Copyright (c) 2022-present CodeStyle (codestyle.top)
 *
 * This project is licensed under the Apache License 2.0.
 */

package top.codestyle.admin.research.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 输入源类型
 *
 * @author CodeStyle
 * @since 2026-02-22
 */
@Getter
@RequiredArgsConstructor
public enum SourceType {

    /**
     * 文本代码片段
     */
    TEXT("文本代码片段"),

    /**
     * 本地文件
     */
    FILE("本地文件"),

    /**
     * GitHub 仓库
     */
    GITHUB("GitHub 仓库"),

    /**
     * URL 链接
     */
    URL("URL 链接");

    /**
     * 类型描述
     */
    private final String description;
}

