package com.xinput.learn.stock.consts;

import com.google.common.collect.Maps;
import com.xinput.learn.stock.model.Stock;

import java.util.Map;

public class StockCache {

    public static volatile Map<String, Stock> stockMap = Maps.newHashMap();

}
