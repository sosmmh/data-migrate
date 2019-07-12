package com.sosmmh.migtate.bean;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: lixiahan
 * @create: 2019/07/04 22:38
 */
@Data
public class ReadDataWrapper<T> {

    private int size;

    private List<T> list;
}
