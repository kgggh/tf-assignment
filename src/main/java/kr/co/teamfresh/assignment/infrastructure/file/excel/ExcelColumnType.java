package kr.co.teamfresh.assignment.infrastructure.file.excel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum ExcelColumnType {
    STRING(cell -> {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            throw new IllegalArgumentException("셀 값이 비어 있습니다.");
        }

        return cell.getStringCellValue();
    }, String.class),
    INTEGER(cell -> {
        if (cell == null || cell.getCellType() != CellType.NUMERIC) {
            throw new IllegalArgumentException("셀 값이 숫자가 아닙니다.");
        }

        return (int) cell.getNumericCellValue();
    }, Integer.class),
    LONG(cell -> {
        if (cell == null || cell.getCellType() != CellType.NUMERIC) {
            throw new IllegalArgumentException("셀 값이 숫자가 아닙니다.");
        }

        return (long) cell.getNumericCellValue();
    }, Long.class);

    private final Function<Cell, Object> converter;
    private final Class<?> type;

    @SuppressWarnings("unchecked")
    public <T> T convert(Cell cell) {
        return (T) converter.apply(cell);
    }
}
