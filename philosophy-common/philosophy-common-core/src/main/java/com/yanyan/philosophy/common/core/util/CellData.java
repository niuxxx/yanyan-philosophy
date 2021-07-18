package com.yanyan.philosophy.common.core.util;

/**
 * 单元格数据及属性的bean
 * Created by niuyan on 2018/5/16.
 */
public class CellData {
    /**
     * 单元格文字内容
     */
    private String cellcon;
    /**
     * 背景颜色
     */
    private short bgColor;
    /**
     * 单元格注释
     */
    private String remark;
    /**
     * 新增Url
     */
    private String url;

    public CellData(String cellcon, int bgColor, String remark, String url) {
        this.cellcon = cellcon;
        this.bgColor = (short) bgColor;
        this.remark = remark;
        this.url = url;
    }

    public CellData(String cellcon, int bgColor, String remark) {
        this.cellcon = cellcon;
        this.bgColor = (short) bgColor;
        this.remark = remark;
    }

    public CellData(String cellcon, int bgColor) {
        this.cellcon = cellcon;
        this.bgColor = (short) bgColor;
        this.url = null;
    }

    public CellData(String cellcon) {
        this.cellcon = cellcon;
        this.bgColor = 0;
        this.url = null;
    }

    public short getBGcolor() {
        return bgColor;
    }

    public String getCellcon() {
        return cellcon;
    }

    public String getRemark() {
        return remark == null ? "" : remark;
    }

    public String getUrl() {
        return url;
    }

    public void setBgColor(short bgColor) {
        this.bgColor = bgColor;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
