package com.xinput.learn.stock.controller;

import com.google.common.collect.Maps;
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

    @GetMapping("/status")
    public String status() {
        return "OK";
    }

    @GetMapping("/all")
    public Map<String, Stock> all() {
        return StockCache.stockMap;
    }

    @GetMapping("/get/{code}")
    public Stock get(@PathVariable(name = "code") String code) {
        return StockCache.stockMap.get(code);
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
}
