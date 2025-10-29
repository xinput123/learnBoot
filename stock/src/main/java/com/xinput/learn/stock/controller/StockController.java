package com.xinput.learn.stock.controller;

import com.google.common.collect.Maps;
import com.xinput.learn.stock.batch.StockBatchLoader;
import com.xinput.learn.stock.consts.StockCache;
import com.xinput.learn.stock.model.Stock;
import com.xinput.learn.stock.service.StockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock")
public class StockController {

    @Resource
    private StockService stockService;

    @Resource
    private StockBatchLoader stockBatchLoader;

    @GetMapping("/status")
    public String status() {
        return "OK";
    }

    @GetMapping("/all")
    public Map<String, Stock> all() {
        return StockCache.stockMap;
    }

    /**
     * 单个查询(不使用批处理) - 每次请求都会调用一次数据库查询
     */
    @GetMapping("/get/{code}")
    public Stock get(@PathVariable(name = "code") String code) {
        return stockService.getStock(code);
    }

    /**
     * 单个查询(使用批处理) - 多个并发请求会被合并成一个批量查询
     * 提高并发吞吐量，减少数据库查询次数
     */
    @GetMapping("/getBatch/{code}")
    public Stock getBatch(@PathVariable(name = "code") String code) {
        // 调用批处理加载器，等待异步结果完成后返回
        return stockBatchLoader.loadStock(code).join();
    }

    @GetMapping("/reason")
    public Map<String, Object> reason() {
        List<Stock> stocks = stockService.reason();
        Map<String, Object> map = Maps.newHashMap();
        map.put("size", stocks.size());
        map.put("stocks", stocks);
        return map;
    }

    @GetMapping("/noreason")
    public Map<String, Object> noreason() {
        List<Stock> stocks = stockService.noreason();
        Map<String, Object> map = Maps.newHashMap();
        map.put("size", stocks.size());
        map.put("stocks", stocks);
        return map;
    }

    /**
     * 获取批处理器监控指标
     */
    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        Map<String, Object> result = Maps.newHashMap();
        result.put("metrics", stockBatchLoader.getMetrics());
        result.put("config", stockBatchLoader.getConfig());
        return result;
    }
}
