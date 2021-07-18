package com.yanyan.philosophy.common.core.bean;

import java.util.List;

/**
 * Created by niuyan on 2018/5/16.
 */
public class ExcelDataParam<T> extends ExcelParam {
    private List<T> data;


    public ExcelDataParam(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }
}
