package com.xinput.learn.stock.process;

import com.xinput.learn.stock.consts.StockCache;
import com.xinput.learn.stock.util.StockFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitProcessor implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        StockCache.stockMap = StockFactory.reslove("code.txt");
    }
}
