/*
 * Copyright (c) 2022-present CodeStyle (codestyle.top)
 *
 * This project is licensed under the Apache License 2.0.
 */

package top.codestyle.admin.research.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户反馈请求
 *
 * @author CodeStyle
 * @since 2026-02-22
 */
@Data
@Schema(description = "用户反馈请求")
public class ResearchFeedbackRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 反馈内容
     */
    @Schema(description = "反馈内容", example = "请调整模板变量名称")
    @NotBlank(message = "反馈内容不能为空")
    private String feedback;

    /**
     * 是否继续执行
     */
    @Schema(description = "是否继续执行", example = "true")
    private Boolean continueExecution = true;
}

