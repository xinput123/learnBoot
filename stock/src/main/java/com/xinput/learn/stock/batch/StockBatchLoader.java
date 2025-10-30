package com.xinput.learn.stock.batch;

import com.xinput.learn.stock.config.BatchConfig;
import com.xinput.learn.stock.model.Stock;
import com.xinput.learn.stock.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 信息批量加载器
 * 继承通用批处理框架，实现查询的批量加载逻辑
 */
@Slf4j
@Component
public class StockBatchLoader extends AbstractBatchLoader<String, Stock> {

    @Resource
    private StockService stockService;

    public StockBatchLoader(BatchConfig batchConfig) {
        super(BatchLoaderConfig.builder()
                .name("StockBatchLoader")
                .intervalMs(batchConfig.getIntervalMs())
                .maxBatchSize(batchConfig.getMaxBatchSize())
                .threadPoolSize(batchConfig.getThreadPoolSize())
                .queueCapacity(batchConfig.getQueueCapacity())
                .overflowStrategy(batchConfig.getOverflowStrategy())
                .timeoutMs(batchConfig.getTimeoutMs())
                .allowNull(batchConfig.isAllowNull())
                .retryOnNull(batchConfig.isRetryOnNull())
                .enableMonitor(batchConfig.isEnableMonitor())
                .monitorIntervalSeconds(batchConfig.getMonitorIntervalSeconds())
                .build());
    }

    @Override
    public Map<String, Stock> batchLoad(List<String> keys) {
        // 调用Service层的批量查询方法
        return stockService.queryStock(keys);
    }

    @Override
    public Stock singleLoad(String key) {
        // 调用Service层的单个查询方法（降级使用）
        return stockService.getStock(key);
    }

    /**
     * 加载信息 - 异步方式
     *
     * @param code 代码
     * @return CompletableFuture<Stock>
     */
    public CompletableFuture<Stock> loadStock(String code) {
        return load(code);
    }

    /**
     * 加载信息 - 同步方式
     *
     * @param code 代码
     * @return Stock
     */
    public Stock loadStockSync(String code) {
        return loadSync(code);
    }
}
