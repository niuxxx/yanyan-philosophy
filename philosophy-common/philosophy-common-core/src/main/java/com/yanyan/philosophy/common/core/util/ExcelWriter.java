package com.yanyan.philosophy.common.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 将ExcelData中的数据写入到文件中
 *
 * Created by niuyan on 2018/5/16.
 */
public class ExcelWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelWriter.class);

    /**
     * 文本中的换行标志符
     */
    public static final char NEWLINE = 0x000A;

    /**
     * ExcelWriter对应的Excel文件在java中的对象模型
     */
    private HSSFWorkbook workbook = new HSSFWorkbook();

    /**
     * 由于Excel有对于单个sheet最大条目的限制65535，故单个sheet的数据操作此做大值时会自动增加一个同名sheet,后缀加上1,2,3...
     */
    public static final int COUNT_PER_SHEET = 50000;

    /**
     * 单元格的默认格式
     */
    private HSSFCellStyle defaultStyle;

    private FontFactory fontFactory;

    public ExcelWriter() {
        fontFactory = new FontFactory();
        defaultStyle = initStyle(null);
    }

    /**
     * 按excel标题头数目，将excel前columnNum列初始化为特殊样式
     *
     * @param sheetName sheet的名称
     * @param excelData sheet中的数据
     * @param columnNum 单元格样式
     */
    public void createSheetWithTextCell(String sheetName, ExcelData excelData, int columnNum) {
        HSSFCellStyle style = initStyle(null);
        int sheetNum = createSheet(sheetName, excelData);
        for (int i = 0; i < sheetNum; i++) {
            HSSFSheet sheet = workbook.getSheetAt(0);
            for (int j = 0; j < columnNum; j++) {
                sheet.setDefaultColumnStyle((short) j, style);
            }
        }
    }

    /**
     * 根据sheetName和excel数据,创建一个sheet
     *
     * @param sheetName sheet的名称
     * @param excelData sheet中的数据
     */
    public int createSheet(String sheetName, ExcelData excelData) {
        HSSFRow row;
        HSSFCell cell;
        List<List<CellData>> sheetData = excelData.getSheetData();
        List<CellData> rowData;
        int sheetNum = 0;

        // 如果sheetData的记录条数超过countPerSheet,则分sheet展示
        for (int sheetNo = 0; sheetNo * COUNT_PER_SHEET < sheetData.size(); sheetNo++) {
            // 如果有同名sheet,则先干掉后再添加sheet.否则直接添加.
            String sheetNameWithNo = sheetName + (sheetNo == 0 ? "" : sheetNo);
            HSSFSheet sheet = workbook.getSheet(sheetNameWithNo);
            if (sheet != null) {
                workbook.removeSheetAt(workbook.getSheetIndex(sheetNameWithNo));
            }
            sheet = workbook.createSheet(sheetNameWithNo);
            sheetNum++;

            // 生成表格主体部分
            int startNo = sheetNo * COUNT_PER_SHEET;
            for (int i = startNo; i < sheetData.size(); i++) {
                // 当前sheet中的行号
                int rowNo = i % COUNT_PER_SHEET;
                // 每页只显示一定数量条
                if (i - sheetNo * COUNT_PER_SHEET >= COUNT_PER_SHEET) {
                    break;
                }

                // 初始化行
                rowData = excelData.getRowData(i + 1);
                row = sheet.createRow(rowNo);

                // 添加行中的单个单元格
                for (int j = 0; j < rowData.size(); j++) {
                    // 初始化单元格
                    CellData cellData = rowData.get(j);
                    cell = row.createCell(j);
                    setCellContent(cell, cellData);
                }
            }

            // 合并单元格
            // TODO 分sheet显示的合并,未经测试,不知道会出啥幺蛾子
            List<int[]> regionArea = excelData.getRegionArea();
            for (int[] region : regionArea) {
                CellRangeAddress address = null;
                if (region[0] / COUNT_PER_SHEET == sheetNo) {
                    address =
                            new CellRangeAddress(region[0] % COUNT_PER_SHEET, Math.min(COUNT_PER_SHEET, region[2]
                                    - COUNT_PER_SHEET * sheetNo), (short) region[1], (short) region[3]);
                } else if (region[2] / COUNT_PER_SHEET == sheetNo) {
                    address =
                            new CellRangeAddress(1, (short) region[1], Math.min(COUNT_PER_SHEET, region[2] % sheetNo),
                                    (short) region[3]);
                }
                if (null != address) {
                    sheet.addMergedRegion(address);
                }
            }
        }
        return sheetNum;
        /*
         * 自动设置列宽(测试失败) int columnCount=0; for(ArrayList<CellData> rowTemp:sheetData) if (rowTemp.size()>columnCount)
         * columnCount=rowTemp.size(); for (short i=0;i<columnCount;i++) sheet.autoSizeColumn(i);
         */
    }

    /**
     * 根据cellData设定一个poi的cell对象
     *
     * @param cell 需要设置数据的HSSFCell对象
     * @param cellData 需要设置的数据
     */
    private void setCellContent(HSSFCell cell, CellData cellData) {
        HSSFRow row = cell.getRow();
        HSSFSheet sheet = cell.getSheet();
        Assert.notNull(row);
        Assert.notNull(sheet);

        // style是否被变化过.如未发生变化则不进行设置.
        HSSFCellStyle specialStyle = null;

        // 写入单元格内容
        cell.setCellValue(new HSSFRichTextString(cellData.getCellcon()));
        // 如果有url，添加url和蓝色字体
        if (StringUtils.isNotBlank(cellData.getUrl())) {
            HSSFHyperlink link = new HSSFHyperlink(HSSFHyperlink.LINK_URL);
            link.setAddress(cellData.getUrl());
            cell.setHyperlink(link);

            specialStyle = initStyle(specialStyle);
            HSSFFont font = fontFactory.getUrlFont();
            specialStyle.setFont(font);
        }
        // 写入单元格注释
        if (cellData.getRemark() != null && cellData.getRemark().length() > 0) {
            int countn = 0;
            String tempString = cellData.getRemark();
            while (tempString.indexOf("\n") > -1) {
                tempString = tempString.substring(tempString.indexOf("\n") + 1);
                countn++;
            }
            // 用于生成注释
            HSSFComment comment =
                    sheet.createDrawingPatriarch().createComment(
                            new HSSFClientAnchor(0, 0, 0, 0, (short) 10, countn < 3 ? 9 : countn + 6, (short) 6, 5));
            comment.setString(new HSSFRichTextString(cellData.getRemark())); // 设置注释
            cell.setCellComment(comment); // 设置注释
        }

        // 支持多行显示
        if (cellData.getCellcon() != null && cellData.getCellcon().indexOf("\n") != -1) {
            int rowsCount = 1;
            String temp = cellData.getCellcon();
            while (temp.indexOf("\n") != -1) {
                temp = temp.substring(temp.indexOf("\n") + 1);
                rowsCount++;
            }
            specialStyle = initStyle(specialStyle);
            specialStyle.setWrapText(true);
            row.setHeightInPoints(row.getHeightInPoints() > (sheet.getDefaultRowHeightInPoints() + 1) * rowsCount ? row
                    .getHeightInPoints() : (sheet.getDefaultRowHeightInPoints() + 1) * rowsCount);
        }
        if (cellData.getBGcolor() != 0) {
            specialStyle = initStyle(specialStyle);
            specialStyle.setFillForegroundColor(cellData.getBGcolor());
            specialStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        }

        // 将单元格格式应用到单元格中
        cell.setCellStyle(null == specialStyle ? defaultStyle : specialStyle);
    }

    /**
     * 初始化一个HSSFCellStyle
     *
     * @param style 需要初始化的style.如果style为null,则设定默认属性.如果style不为空,则不进行任何操作
     * @return 初始化后的style
     */
    private HSSFCellStyle initStyle(HSSFCellStyle style) {
        if (null == style) {
            // 设定边框等默认样式
            style = addBorderStyle(workbook.createCellStyle());
            // 设定为文本格式
            style.setDataFormat((short) 49);
        }
        return style;
    }

    /**
     * 设置一个HSSFCellStyle的边框属性
     *
     * @param style 需要设置的HSSFCellStyle对象
     * @return 设置完成后的HSSFCellStyle对象
     */
    private HSSFCellStyle addBorderStyle(HSSFCellStyle style) {
        Assert.notNull(style);

        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        style.setFont(fontFactory.getContentFont());
        return style;
    }

    /**
     * 根据文件名保存文件
     *
     * @param file 欲保存的完整文件名(路径+文件名)
     * @throws IOException
     */
    public void saveFile(String file) throws IOException {
        saveFile(new File(file));
    }

    /**
     * 根据文件对象保存文件
     *
     * @param file 欲将当前Excel数据保存到的文件对象
     * @throws IOException
     */
    public void saveFile(File file) throws IOException {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if(null != fileOut) {
                    fileOut.close();
                }
            } catch (IOException e) {
                LOGGER.error("file close failed: " + e.getMessage());
            }
        }
    }

    /**
     * 在web应用中,输出Excel文件到response中.
     *
     * @param oriFileName 欲输出的文件名
     * @param request
     * @param response
     * @throws IOException
     */
    public void outPutToResponse(String oriFileName, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String fileName;
        if (oriFileName != null) {
            fileName = oriFileName.replace("/", "_");
        } else {
            fileName = "";
        }

        try {
            response.setContentType("application/msexcel;charset=UTF-8");
            boolean ieVersion = (request.getHeader("User-Agent").indexOf("MSIE") > 0);
            if (ieVersion) {
                fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
                response.setHeader("Content-Disposition", "filename=" + fileName);
            } else {
                fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage());
        }
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xls");
        workbook.write(response.getOutputStream());
    }

    private class FontFactory {
        private HSSFFont contentFont;
        private HSSFFont urlFont;

        public FontFactory() {
            contentFont = workbook.createFont();
//            contentFont.setFontName("宋体");

            urlFont = workbook.createFont();
            urlFont.setUnderline(HSSFFont.U_SINGLE);
            urlFont.setColor(HSSFColor.BLUE.index);

        }

        public HSSFFont getContentFont() {
            return contentFont;
        }

        public HSSFFont getUrlFont() {
            return urlFont;
        }

    }

}
