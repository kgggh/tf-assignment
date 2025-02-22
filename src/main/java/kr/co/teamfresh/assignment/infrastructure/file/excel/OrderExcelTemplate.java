package kr.co.teamfresh.assignment.infrastructure.file.excel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

@Getter
@AllArgsConstructor
public enum OrderExcelTemplate {
    ORDERER_NAME("주문자명", 1, 0, ExcelColumnType.STRING),
    ORDERER_ADDRESS("주문자 주소", 1, 1, ExcelColumnType.STRING),
    PRODUCT_ID("상품 고유번호", 0, 3, ExcelColumnType.LONG),
    PRODUCT_NAME("상품명", 1, 3, ExcelColumnType.STRING),
    QUANTITY("수량", 2, 3, ExcelColumnType.INTEGER);

    public static final int SHEET_NUMBER = 0;
    public static final int START_ROW_INDEX = 3;

    private final String headerName;
    private final int cellIndex;
    private final int rowIndex;
    private final ExcelColumnType columnType;

    public <T> T getCellValue(Row row) {
        Cell cell = row.getCell(cellIndex);

        return columnType.convert(cell);
    }
}
