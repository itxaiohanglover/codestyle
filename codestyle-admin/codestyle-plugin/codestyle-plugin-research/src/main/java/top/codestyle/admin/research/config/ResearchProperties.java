/*
 * Copyright (c) 2022-present CodeStyle (codestyle.top)
 *
 * This project is licensed under the Apache License 2.0.
 */

package top.codestyle.admin.research.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 深度研究配置属性
 *
 * @author CodeStyle
 * @since 2026-02-22
 */
@Data
@Component
@ConfigurationProperties(prefix = "research")
public class ResearchProperties {

    /**
     * 是否启用深度研究功能
     */
    private boolean enabled = true;

    /**
     * 研究任务超时时间（秒）
     */
    private int taskTimeoutSeconds = 600;

    /**
     * 最大并行研究任务数
     */
    private int maxConcurrentTasks = 5;

    /**
     * SSE 心跳间隔（秒）
     */
    private int sseHeartbeatSeconds = 30;

    /**
     * 模型配置
     */
    private ModelConfig model = new ModelConfig();

    /**
     * GitHub 配置
     */
    private GitHubConfig github = new GitHubConfig();

    /**
     * 模板生成配置
     */
    private TemplateConfig template = new TemplateConfig();

    @Data
    public static class ModelConfig {

        /**
         * 默认模型名称
         */
        private String defaultModel = "qwen-plus";

        /**
         * 强模型（用于关键节点）
         */
        private String strongModel = "qwen-max";

        /**
         * 小模型（用于简单任务）
         */
        private String smallModel = "qwen-turbo";

        /**
         * 最大 Token 数
         */
        private int maxTokens = 4096;

        /**
         * 温度参数
         */
        private double temperature = 0.7;
    }

    @Data
    public static class GitHubConfig {

        /**
         * GitHub Token（用于提高 API 限流）
         */
        private String token;

        /**
         * 克隆超时时间（秒）
         */
        private int cloneTimeoutSeconds = 300;

        /**
         * 最大文件大小（MB）
         */
        private int maxFileSizeMb = 10;
    }

    @Data
    public static class TemplateConfig {

        /**
         * 模板存储路径
         */
        private String storagePath = "./templates";

        /**
         * 是否自动索引到 ES
         */
        private boolean autoIndex = true;

        /**
         * 是否自动生成向量
         */
        private boolean autoVectorize = true;
    }
}

