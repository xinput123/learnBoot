package com.xinput.learn.stock.service;

import com.google.common.collect.Lists;
import com.xinput.learn.stock.consts.StockCache;
import com.xinput.learn.stock.model.Stock;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
