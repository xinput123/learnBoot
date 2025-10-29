package com.xinput.learn.stock.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xinput.learn.stock.model.Stock;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class StockFactory {

    public static void sleep(long mills) {
        try {
            TimeUnit.MILLISECONDS.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Stock> reslove(String fileName) {
        List<String> contents = ResourceFileUtils.readFileFromClasspath(fileName);
        if (CollectionUtils.isEmpty(contents)) {
            return Maps.newHashMap();
        }

        Map<String, Stock> stockMap = Maps.newHashMapWithExpectedSize(contents.size());
        for (String content : contents) {
            Optional.ofNullable(create(content)).ifPresent(stock -> stockMap.put(stock.getCode(), stock));
        }
        return stockMap;
    }

    private static Stock create(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }

        String[] split = StringUtils.split(content, ",");
        if (Objects.isNull(split)) {
            return null;
        }

        int length = split.length;

        if (length == 2) {
            Stock stock = new Stock();
            stock.setCode(split[0]);
            stock.setName(split[1]);
            return stock;
        } else if (length == 3) {
            Stock stock = new Stock();
            stock.setCode(split[0]);
            stock.setName(split[1]);
            String reason = split[2];
            stock.setReason(Lists.newArrayList(StringUtils.split(reason, "+")));
            return stock;
        }
        return null;
    }
}
