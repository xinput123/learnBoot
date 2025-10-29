package com.xinput.learn.stock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.xinput.learn.stock.batch.OverflowStrategy;

import lombok.Getter;
import lombok.Setter;

/**
 * 批处理配置
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "stock.batch")
public class BatchConfig {

    /**
     * 批处理间隔时间(毫秒)
     * 每隔多久触发一次批处理
     * 默认: 10ms
     */
    private long intervalMs = 10;

    /**
     * 批处理最大大小
     * 单次批处理最多处理多少个请求
     * 达到这个数量会立即触发批处理，不等待定时器
     * 默认: 100
     */
    private int maxBatchSize = 100;

    /**
     * 批处理线程池大小
     * 默认: 1 (单线程处理批量请求)
     */
    private int threadPoolSize = 1;

    /**
     * 请求队列容量
     * 待处理请求的队列最大容量
     * 默认: 10000
     */
    private int queueCapacity = 10000;

    /**
     * 队列溢出策略
     * BLOCK: 阻塞等待（推荐）
     * DEGRADE: 降级执行单个查询
     * FAIL_FAST: 快速失败
     * DROP_OLDEST: 丢弃最旧请求（不推荐）
     * 默认: BLOCK
     */
    private OverflowStrategy overflowStrategy = OverflowStrategy.BLOCK;

    /**
     * 请求超时时间(毫秒)
     * 默认: 5000ms
     */
    private long timeoutMs = 5000;

    /**
     * 是否允许返回null值
     * 默认: true
     */
    private boolean allowNull = true;

    /**
     * 批量查询结果为空时，是否重试单个查询
     * 默认: false
     */
    private boolean retryOnNull = false;

    /**
     * 是否启用监控日志
     * 默认: true
     */
    private boolean enableMonitor = true;

    /**
     * 监控日志输出间隔(秒)
     * 默认: 60秒
     */
    private long monitorIntervalSeconds = 60;
}
