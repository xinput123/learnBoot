package com.xinput.learn.stock.controller;

import com.xinput.learn.stock.consts.StockCache;
import com.xinput.learn.stock.model.Stock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/stock")
public class StockController {

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
}
