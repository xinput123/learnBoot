package com.xinput.learn.stock.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xinput.learn.stock.consts.StockCache;
import com.xinput.learn.stock.model.Stock;
import com.xinput.learn.stock.util.StockFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class StockService {

    public List<Stock> reason() {
        List<Stock> stocks = Lists.newArrayListWithCapacity(1024 * 6);
        for (Stock stock : StockCache.stockMap.values()) {
            if (CollectionUtils.isNotEmpty(stock.getReason())) {
                stocks.add(stock);
            }
        }
        return stocks;
    }

    public List<Stock> noreason() {
        List<Stock> stocks = Lists.newArrayListWithCapacity(1024 * 6);
        for (Stock stock : StockCache.stockMap.values()) {
            if (CollectionUtils.isEmpty(stock.getReason())) {
                stocks.add(stock);
            }
        }
        return stocks;
    }

    public Stock getStock(String code) {
        // 模拟查询数据库耗时
        StockFactory.sleep(ThreadLocalRandom.current().nextInt(100, 1000));
        return StockCache.stockMap.get(code);
    }

    public Map<String, Stock> queryStock(List<String> codes) {
        // 模拟查询数据库耗时
        StockFactory.sleep(ThreadLocalRandom.current().nextInt(200, 1000));
        Map<String, Stock> stockMap = Maps.newHashMap();
        for (String code : codes) {
            stockMap.put(code, StockCache.stockMap.get(code));
        }
        return stockMap;
    }
}
