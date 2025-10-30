package com.xinput.learn.stock.controller;

import com.google.common.collect.Maps;
import com.xinput.learn.stock.batch.StockBatchLoader;
import com.xinput.learn.stock.model.Stock;
import com.xinput.learn.stock.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 批处理性能测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/stock/test")
public class StockBatchTestController {

    @Resource
    private StockService stockService;

    @Resource
    private StockBatchLoader stockBatchLoader;

    /**
     * 测试不使用批处理的并发查询性能
     *
     * @param codes 代码列表，逗号分隔，例如: 000001,000002,000003
     * @return 性能测试结果
     */
    @GetMapping("/noBatch")
    public Map<String, Object> testNoBatch(@RequestParam String codes) {
        String[] codeArray = codes.split(",");
        long startTime = System.currentTimeMillis();

        List<Stock> stocks = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            List<CompletableFuture<Stock>> futures = new ArrayList<>();

            // 并发查询每个
            for (String code : codeArray) {
                CompletableFuture<Stock> future = CompletableFuture.supplyAsync(
                        () -> stockService.getStock(code.trim()),
                        executor);
                futures.add(future);
            }

            // 等待所有查询完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 收集结果
            stocks = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long costTime = System.currentTimeMillis() - startTime;

        Map<String, Object> result = Maps.newHashMap();
        result.put("mode", "不使用批处理");
        result.put("requestCount", codeArray.length);
        result.put("costTime", costTime + "ms");
        result.put("avgTime", (costTime * 1.0 / codeArray.length) + "ms");
        result.put("stocks", stocks);

        log.info("不使用批处理 - 查询数量: {}, 总耗时: {}ms, 平均: {}ms",
                codeArray.length, costTime, costTime * 1.0 / codeArray.length);

        return result;
    }

    /**
     * 测试使用批处理的并发查询性能
     *
     * @param codes 代码列表，逗号分隔，例如: 000001,000002,000003
     * @return 性能测试结果
     */
    @GetMapping("/withBatch")
    public Map<String, Object> testWithBatch(@RequestParam String codes) {
        String[] codeArray = codes.split(",");
        long startTime = System.currentTimeMillis();

        List<Stock> stocks = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        try {
            List<CompletableFuture<Stock>> futures = new ArrayList<>();

            // 并发提交查询请求（会被批处理器合并）
            for (String code : codeArray) {
                CompletableFuture<Stock> future = CompletableFuture.supplyAsync(
                        () -> stockBatchLoader.loadStock(code.trim()).join(),
                        executor);
                futures.add(future);
            }

            // 等待所有查询完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 收集结果
            stocks = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long costTime = System.currentTimeMillis() - startTime;

        Map<String, Object> result = Maps.newHashMap();
        result.put("mode", "使用批处理");
        result.put("requestCount", codeArray.length);
        result.put("costTime", costTime + "ms");
        result.put("avgTime", (costTime * 1.0 / codeArray.length) + "ms");
        result.put("stocks", stocks);

        log.info("使用批处理 - 查询数量: {}, 总耗时: {}ms, 平均: {}ms",
                codeArray.length, costTime, costTime * 1.0 / codeArray.length);

        return result;
    }

    /**
     * 对比测试：同时测试批处理和非批处理的性能
     *
     * @param codes 代码列表，逗号分隔
     * @return 对比结果
     */
    @GetMapping("/compare")
    public Map<String, Object> compare(@RequestParam String codes) {
        Map<String, Object> result = Maps.newHashMap();

        // 测试不使用批处理
        Map<String, Object> noBatchResult = testNoBatch(codes);

        // 等待一下，避免相互影响
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 测试使用批处理
        Map<String, Object> withBatchResult = testWithBatch(codes);

        result.put("noBatch", noBatchResult);
        result.put("withBatch", withBatchResult);

        long noBatchTime = Long.parseLong(noBatchResult.get("costTime").toString().replace("ms", ""));
        long withBatchTime = Long.parseLong(withBatchResult.get("costTime").toString().replace("ms", ""));
        double improvement = ((noBatchTime - withBatchTime) * 100.0 / noBatchTime);

        result.put("performance", String.format("批处理性能提升: %.2f%%", improvement));
        result.put("timeReduced", (noBatchTime - withBatchTime) + "ms");
        return result;
    }
}
