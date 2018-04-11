package uk.gov.register.views.representations.spreadsheet;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.util.Map;

public class WorkbookGenerator {

    private static final int DEFAULT_COLUMN_SIZE = 6000;

    public static Workbook toSpreadSheet(final String registerId, final List<String> fieldNames,
                                         final List<Map<String, String>> elements) {
        final Workbook workbook = new XSSFWorkbook();
        final Sheet sheet = workbook.createSheet(registerId);

        customiseColumnSizes(fieldNames, sheet);
        writeHeader(fieldNames, workbook, sheet);
        writeElements(fieldNames, elements, workbook, sheet);

        return workbook;
    }

    private static void customiseColumnSizes(final List<String> fieldNames, final Sheet sheet) {
        for (int i = 0; i < fieldNames.size(); i++) {
            sheet.setColumnWidth(i, DEFAULT_COLUMN_SIZE);
        }
    }

    private static void writeHeader(final List<String> fieldNames, final Workbook workbook, final Sheet sheet) {
        final Row header = sheet.createRow(0);
        final CellStyle headerStyle = workbook.createCellStyle();
        final XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        int counter = 0;
        Cell headerCell;

        font.setBold(true);

        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFont(font);

        for (final String fieldName : fieldNames) {
            headerCell = header.createCell(counter++);
            headerCell.setCellValue(fieldName);
            headerCell.setCellStyle(headerStyle);
        }
    }

    private static void writeElements(final List<String> fieldNames, final List<Map<String, String>> elements,
                                      final Workbook workbook, final Sheet sheet) {
        final CellStyle style = workbook.createCellStyle();
        Row row;
        Cell cell;
        int rowCounter = 1;
        int cellCounter = 0;

        style.setWrapText(true);

        for (final Map<String, String> element : elements) {
            row = sheet.createRow(rowCounter++);

            for (final String fieldName : fieldNames) {
                cell = row.createCell(cellCounter++);
                cell.setCellValue(element.get(fieldName));
                cell.setCellStyle(style);
            }

            cellCounter = 0;
        }
    }
}
