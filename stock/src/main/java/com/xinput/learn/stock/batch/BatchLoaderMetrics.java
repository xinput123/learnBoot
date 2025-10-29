package com.xinput.learn.stock.batch;

import lombok.Builder;
import lombok.Getter;

/**
 * 批处理加载器监控指标
 */
@Getter
@Builder
public class BatchLoaderMetrics {

    /**
     * 总请求数
     */
    private long totalRequestCount;

    /**
     * 批处理次数
     */
    private long batchCount;

    /**
     * 降级执行次数
     */
    private long degradeCount;

    /**
     * 阻塞等待次数
     */
    private long blockCount;

    /**
     * 失败次数
     */
    private long failCount;

    /**
     * 当前队列大小
     */
    private int currentQueueSize;

    /**
     * 平均批量大小
     */
    public double getAvgBatchSize() {
        return batchCount > 0 ? totalRequestCount * 1.0 / batchCount : 0;
    }

    /**
     * 降级率
     */
    public double getDegradeRate() {
        return totalRequestCount > 0 ? degradeCount * 100.0 / totalRequestCount : 0;
    }

    /**
     * 失败率
     */
    public double getFailRate() {
        return totalRequestCount > 0 ? failCount * 100.0 / totalRequestCount : 0;
    }

    @Override
    public String toString() {
        return String.format(
                "BatchLoaderMetrics{总请求=%d, 批处理次数=%d, 平均批量=%.2f, " +
                        "降级=%d(%.2f%%), 阻塞=%d, 失败=%d(%.2f%%), 队列=%d}",
                totalRequestCount, batchCount, getAvgBatchSize(),
                degradeCount, getDegradeRate(),
                blockCount,
                failCount, getFailRate(),
                currentQueueSize);
    }
}
