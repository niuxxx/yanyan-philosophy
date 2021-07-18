package com.yanyan.philosophy.common.core.bean;

/**
 * ExcelParam
 *
 * Created by niuyan on 2018/5/16.
 */
public class ExcelParam {

    private String startTime;
    private String endTime;
    private Integer cellNum;

    public ExcelParam() {
    }

    public ExcelParam(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getCellNum() {
        return cellNum;
    }

    public void setCellNum(Integer cellNum) {
        this.cellNum = cellNum;
    }
}
