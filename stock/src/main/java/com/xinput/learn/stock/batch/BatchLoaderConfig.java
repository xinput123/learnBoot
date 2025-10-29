package com.xinput.learn.stock.batch;

import lombok.Builder;
import lombok.Getter;

/**
 * 批处理加载器配置
 */
@Getter
@Builder
public class BatchLoaderConfig {

    /**
     * 名称（用于日志和监控）
     */
    @Builder.Default
    private String name = "BatchLoader";

    /**
     * 批处理间隔时间(毫秒)
     * 每隔多久触发一次批处理
     */
    @Builder.Default
    private long intervalMs = 10;

    /**
     * 批处理最大大小
     * 单次批处理最多处理多少个请求
     * 达到这个数量会立即触发批处理，不等待定时器
     */
    @Builder.Default
    private int maxBatchSize = 100;

    /**
     * 批处理线程池大小
     */
    @Builder.Default
    private int threadPoolSize = 1;

    /**
     * 请求队列容量
     * 待处理请求的队列最大容量
     */
    @Builder.Default
    private int queueCapacity = 10000;

    /**
     * 队列溢出策略
     */
    @Builder.Default
    private OverflowStrategy overflowStrategy = OverflowStrategy.BLOCK;

    /**
     * 请求超时时间(毫秒)
     */
    @Builder.Default
    private long timeoutMs = 5000;

    /**
     * 是否允许返回null值
     */
    @Builder.Default
    private boolean allowNull = true;

    /**
     * 批量查询结果为空时，是否重试单个查询
     */
    @Builder.Default
    private boolean retryOnNull = false;

    /**
     * 是否启用监控日志
     */
    @Builder.Default
    private boolean enableMonitor = true;

    /**
     * 监控日志输出间隔(秒)
     */
    @Builder.Default
    private long monitorIntervalSeconds = 60;
}
