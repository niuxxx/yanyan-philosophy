package com.yanyan.philosophy.common.core.util;

import com.yanyan.common.bean.ExcelParam;
import com.yanyan.common.constant.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel service
 *
 * Created by niuyan on 2018/5/16.
 */
public abstract class ExcelExportExecutor {

    public ExcelWriter generateExcelWrite(ExcelParam param) {
        String sheetName = generateSheetName(param.getStartTime(), param.getEndTime());
        List<String> titles = getTitles();
        List<List<String>> bodies = getBodies(param);

        return generate(titles, bodies, sheetName);
    }

    protected String generateSheetName(String st, String et) {

        return String.format("%s---%s", st, et);
    }

    protected ExcelWriter generate(List<String> titles, List<List<String>> bodys, String sheetName) {
        ExcelWriter excelWriter = new ExcelWriter();
        ExcelData excelData = new ExcelData();
        // set title
        excelData.addRowString(new ArrayList<String>(titles), HSSFColor.YELLOW.index);
        // set body
        for (List<String> row : bodys) {
            if (StringUtils.equals(row.get(0), Constants.EXECEL_TITLE_SIGNAL)) {
                excelData.addRowString(new ArrayList<String>(row.subList(1, row.size())), HSSFColor.YELLOW.index);
            } else {
                excelData.addRowString(new ArrayList<String>(row));
            }
        }
        excelWriter.createSheet(sheetName, excelData);

        return excelWriter;
    }

    protected abstract List<List<String>> getBodies(ExcelParam param);

    protected abstract List<String> getTitles();

    public abstract String getFileName(ExcelParam param);

}
