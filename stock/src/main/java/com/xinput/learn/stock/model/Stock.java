package com.xinput.learn.stock.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Stock {

    /**
     * 代码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    private List<String> reason;
}
