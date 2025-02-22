package kr.co.teamfresh.assignment.infrastructure.file.excel;

import kr.co.teamfresh.assignment.infrastructure.file.FileExtension;
import kr.co.teamfresh.assignment.infrastructure.file.OrderFileProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ExcelOrderProcessor implements OrderFileProcessor {

    @Override
    public OrderImportResult process(InputStream inputStream) {
        try(Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet currentSheet = workbook.getSheetAt(ExcelOrderTemplate.SHEET_NUMBER);
            log.info("현재 액셀 작업 시트명: {}", currentSheet.getSheetName());

            String ordererName = parseOrdererInfo(currentSheet, ExcelOrderTemplate.ORDERER_NAME);
            String address = parseOrdererInfo(currentSheet, ExcelOrderTemplate.ORDERER_ADDRESS);
            List<OrderImportResult.OrderProductInfo> orderProductInfos = parseProductInfos(currentSheet);

            return new OrderImportResult(ordererName, address, orderProductInfos);
        } catch (IOException e) {
            throw new UncheckedIOException("엑셀 파일을 처리 도중 오류가 발생했습니다.", e);
        } catch (NullPointerException | IllegalStateException | NotOfficeXmlFileException e) {
            throw new IllegalStateException("엑셀 파일의 데이터가 올바르지 않습니다.", e);
        }
    }

    private String parseOrdererInfo(Sheet sheet, ExcelOrderTemplate template) {
        Row row = sheet.getRow(template.getRowIndex());

        return template.getCellValue(row);
    }

    private List<OrderImportResult.OrderProductInfo> parseProductInfos(Sheet sheet) {
        List<OrderImportResult.OrderProductInfo> productInfos = new ArrayList<>();

        for (int i = ExcelOrderTemplate.START_ROW_INDEX; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (isRowEmpty(row)) {
                break;
            }

            OrderImportResult.OrderProductInfo productInfo = new OrderImportResult.OrderProductInfo(
                ExcelOrderTemplate.PRODUCT_ID.getCellValue(row),
                ExcelOrderTemplate.PRODUCT_NAME.getCellValue(row),
                ExcelOrderTemplate.QUANTITY.getCellValue(row)
            );

            productInfos.add(productInfo);
        }

        return productInfos;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null || row.getPhysicalNumberOfCells() == 0) {
            return true;
        }

        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }

        return true;
    }

    @Override
    public FileExtension getExtension() {
        return FileExtension.XLSX;
    }
}
