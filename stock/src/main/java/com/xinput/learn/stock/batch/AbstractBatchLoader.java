package com.xinput.learn.stock.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用批处理加载器抽象类
 * 提供批处理请求合并的通用实现，子类只需实现具体的批量加载逻辑
 *
 * @param <K> 请求的Key类型
 * @param <V> 返回的Value类型
 */
@Slf4j
public abstract class AbstractBatchLoader<K, V> implements BatchLoader<K, V> {

    /**
     * 批处理任务队列
     */
    private BlockingQueue<BatchRequest<K, V>> requestQueue;

    /**
     * 批处理执行器
     */
    private ScheduledExecutorService batchExecutor;

    /**
     * 批处理配置
     */
    @Getter
    private BatchLoaderConfig config;

    /**
     * 是否已关闭
     */
    private volatile boolean shutdown = false;

    // ==================== 监控指标 ====================
    /**
     * 总请求数
     */
    private final AtomicLong totalRequestCount = new AtomicLong(0);

    /**
     * 批处理次数
     */
    private final AtomicLong batchCount = new AtomicLong(0);

    /**
     * 降级执行次数
     */
    private final AtomicLong degradeCount = new AtomicLong(0);

    /**
     * 阻塞等待次数
     */
    private final AtomicLong blockCount = new AtomicLong(0);

    /**
     * 失败次数
     */
    private final AtomicLong failCount = new AtomicLong(0);

    /**
     * 当前队列大小
     */
    private final AtomicInteger currentQueueSize = new AtomicInteger(0);

    /**
     * 构造函数
     */
    public AbstractBatchLoader(BatchLoaderConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        // 初始化请求队列
        requestQueue = new LinkedBlockingQueue<>(config.getQueueCapacity());

        // 初始化批处理执行器
        batchExecutor = Executors.newScheduledThreadPool(
                config.getThreadPoolSize(),
                r -> {
                    Thread thread = new Thread(r, config.getName() + "-batch-loader");
                    thread.setDaemon(true);
                    return thread;
                });

        // 启动定时批处理任务
        batchExecutor.scheduleAtFixedRate(
                this::processBatch,
                config.getIntervalMs(),
                config.getIntervalMs(),
                TimeUnit.MILLISECONDS);

        // 启动监控日志任务
        if (config.isEnableMonitor()) {
            batchExecutor.scheduleAtFixedRate(
                    this::printMonitor,
                    config.getMonitorIntervalSeconds(),
                    config.getMonitorIntervalSeconds(),
                    TimeUnit.SECONDS);
        }

        log.info("{} 初始化完成 - 批处理间隔: {}ms, 最大批量: {}, 队列容量: {}, 溢出策略: {}, 超时时间: {}ms",
                config.getName(),
                config.getIntervalMs(),
                config.getMaxBatchSize(),
                config.getQueueCapacity(),
                config.getOverflowStrategy(),
                config.getTimeoutMs());
    }

    @PreDestroy
    public void destroy() {
        shutdown = true;
        if (batchExecutor != null) {
            batchExecutor.shutdown();
            try {
                if (!batchExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    batchExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                batchExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("{} 已关闭", config.getName());
    }

    @Override
    public CompletableFuture<V> load(K key) {
        if (shutdown) {
            CompletableFuture<V> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("BatchLoader 已关闭"));
            return future;
        }

        totalRequestCount.incrementAndGet();
        CompletableFuture<V> future = new CompletableFuture<>();
        BatchRequest<K, V> request = new BatchRequest<>(key, future, System.currentTimeMillis());

        // 尝试将请求加入队列
        boolean offered = requestQueue.offer(request);

        if (!offered) {
            // 队列已满，根据溢出策略处理
            handleOverflow(request);
        } else {
            currentQueueSize.incrementAndGet();
            // 如果队列已达到最大批量，立即触发批处理
            if (requestQueue.size() >= config.getMaxBatchSize()) {
                batchExecutor.execute(this::processBatch);
            }
        }

        return future;
    }

    @Override
    public V loadSync(K key) {
        try {
            CompletableFuture<V> future = load(key);
            // 带超时的等待
            return future.get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.error("请求超时: key={}, timeout={}ms", key, config.getTimeoutMs());
            throw new RuntimeException("请求超时: " + key, e);
        } catch (Exception e) {
            log.error("请求异常: key={}", key, e);
            throw new RuntimeException("请求异常: " + key, e);
        }
    }

    /**
     * 处理队列溢出
     */
    private void handleOverflow(BatchRequest<K, V> request) {
        switch (config.getOverflowStrategy()) {
            case BLOCK:
                // 阻塞等待队列有空位
                handleBlockStrategy(request);
                break;

            case DEGRADE:
                // 降级执行单个查询
                handleDegradeStrategy(request);
                break;

            case FAIL_FAST:
                // 快速失败
                handleFailFastStrategy(request);
                break;

            case DROP_OLDEST:
                // 丢弃最旧的请求
                handleDropOldestStrategy(request);
                break;

            default:
                handleFailFastStrategy(request);
        }
    }

    /**
     * 阻塞等待策略
     */
    private void handleBlockStrategy(BatchRequest<K, V> request) {
        blockCount.incrementAndGet();
        log.warn("队列已满，阻塞等待 - key: {}, 队列大小: {}", request.getKey(), requestQueue.size());
        try {
            // 阻塞等待，直到队列有空位或超时
            boolean offered = requestQueue.offer(
                    request,
                    config.getTimeoutMs(),
                    TimeUnit.MILLISECONDS);

            if (!offered) {
                // 等待超时，降级执行
                log.error("阻塞等待超时，降级执行 - key: {}", request.getKey());
                handleDegradeStrategy(request);
            } else {
                currentQueueSize.incrementAndGet();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("阻塞等待被中断，降级执行 - key: {}", request.getKey());
            handleDegradeStrategy(request);
        }
    }

    /**
     * 降级策略 - 直接执行单个查询
     */
    private void handleDegradeStrategy(BatchRequest<K, V> request) {
        degradeCount.incrementAndGet();
        log.warn("队列已满，降级执行单个查询 - key: {}, 队列大小: {}", request.getKey(), requestQueue.size());

        // 异步执行单个查询，避免阻塞调用线程
        CompletableFuture.runAsync(() -> {
            try {
                V result = singleLoad(request.getKey());
                request.getFuture().complete(result);
            } catch (Exception e) {
                log.error("降级执行异常 - key: {}", request.getKey(), e);
                request.getFuture().completeExceptionally(e);
            }
        }, batchExecutor);
    }

    /**
     * 快速失败策略
     */
    private void handleFailFastStrategy(BatchRequest<K, V> request) {
        failCount.incrementAndGet();
        log.error("队列已满，快速失败 - key: {}, 队列大小: {}", request.getKey(), requestQueue.size());
        request.getFuture().completeExceptionally(
                new RuntimeException("批处理队列已满，请稍后重试。队列容量: " + config.getQueueCapacity()));
    }

    /**
     * 丢弃最旧策略（不推荐）
     */
    private void handleDropOldestStrategy(BatchRequest<K, V> request) {
        log.warn("队列已满，丢弃最旧请求 - key: {}", request.getKey());
        BatchRequest<K, V> oldest = requestQueue.poll();
        if (oldest != null) {
            currentQueueSize.decrementAndGet();
            oldest.getFuture().completeExceptionally(
                    new RuntimeException("请求被丢弃（队列已满，采用丢弃最旧策略）"));
        }
        // 加入新请求
        if (requestQueue.offer(request)) {
            currentQueueSize.incrementAndGet();
        } else {
            // 还是失败，降级执行
            handleDegradeStrategy(request);
        }
    }

    /**
     * 处理批量请求
     */
    private void processBatch() {
        if (requestQueue.isEmpty()) {
            return;
        }

        // 从队列中取出待处理的请求
        List<BatchRequest<K, V>> batch = new ArrayList<>();
        requestQueue.drainTo(batch, config.getMaxBatchSize());

        if (batch.isEmpty()) {
            return;
        }

        currentQueueSize.addAndGet(-batch.size());
        batchCount.incrementAndGet();

        long startTime = System.currentTimeMillis();
        List<K> keys = new ArrayList<>(batch.size());

        // 收集所有Key
        for (BatchRequest<K, V> request : batch) {
            keys.add(request.getKey());
        }

        try {
            log.debug("开始批量查询 - 请求数量: {}, keys: {}", batch.size(), keys);

            // 批量查询数据
            Map<K, V> resultMap = batchLoad(keys);

            // 将结果分发给各个等待的请求
            for (BatchRequest<K, V> request : batch) {
                try {
                    // 检查是否超时
                    long waitTime = System.currentTimeMillis() - request.getStartTime();
                    if (waitTime > config.getTimeoutMs()) {
                        log.warn("请求已超时 - key: {}, 等待时间: {}ms", request.getKey(), waitTime);
                        request.getFuture().completeExceptionally(
                                new TimeoutException("请求超时: " + waitTime + "ms"));
                        continue;
                    }

                    V value = resultMap.get(request.getKey());
                    if (value != null || config.isAllowNull()) {
                        request.getFuture().complete(value);
                    } else {
                        // 结果为空且不允许空值，尝试单独查询
                        if (config.isRetryOnNull()) {
                            log.warn("批量查询结果为空，尝试单独查询 - key: {}", request.getKey());
                            V singleResult = singleLoad(request.getKey());
                            request.getFuture().complete(singleResult);
                        } else {
                            request.getFuture().complete(null);
                        }
                    }
                } catch (Exception e) {
                    log.error("分发结果异常 - key: {}", request.getKey(), e);
                    request.getFuture().completeExceptionally(e);
                }
            }

            long costTime = System.currentTimeMillis() - startTime;
            log.debug("批量查询完成 - 耗时: {}ms, 请求数量: {}, 平均: {}ms",
                    costTime, batch.size(), costTime * 1.0 / batch.size());

        } catch (Exception e) {
            log.error("批量查询异常 - keys: {}", keys, e);
            // 异常情况下，将异常传递给所有等待的请求
            for (BatchRequest<K, V> request : batch) {
                if (!request.getFuture().isDone()) {
                    request.getFuture().completeExceptionally(e);
                }
            }
        }
    }

    /**
     * 打印监控指标
     */
    private void printMonitor() {
        long total = totalRequestCount.get();
        long batch = batchCount.get();
        long degrade = degradeCount.get();
        long block = blockCount.get();
        long fail = failCount.get();
        int queueSize = currentQueueSize.get();

        double avgBatchSize = batch > 0 ? total * 1.0 / batch : 0;
        double degradeRate = total > 0 ? degrade * 100.0 / total : 0;

        log.info("{} 监控指标 - 总请求: {}, 批处理次数: {}, 平均批量: {:.2f}, " +
                "降级: {}({:.2f}%), 阻塞等待: {}, 失败: {}, 当前队列: {}",
                config.getName(), total, batch, avgBatchSize,
                degrade, degradeRate, block, fail, queueSize);
    }

    /**
     * 获取监控指标
     */
    public BatchLoaderMetrics getMetrics() {
        return BatchLoaderMetrics.builder()
                .totalRequestCount(totalRequestCount.get())
                .batchCount(batchCount.get())
                .degradeCount(degradeCount.get())
                .blockCount(blockCount.get())
                .failCount(failCount.get())
                .currentQueueSize(currentQueueSize.get())
                .build();
    }

    /**
     * 批处理请求包装类
     */
    @Getter
    private static class BatchRequest<K, V> {
        private final K key;
        private final CompletableFuture<V> future;
        private final long startTime;

        public BatchRequest(K key, CompletableFuture<V> future, long startTime) {
            this.key = key;
            this.future = future;
            this.startTime = startTime;
        }
    }
}
