package kr.co.teamfresh.assignment.infrastructure.file.excel;

import kr.co.teamfresh.assignment.infrastructure.file.FileExtension;
import kr.co.teamfresh.assignment.infrastructure.file.FileProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OrderExcelFileProcessor implements FileProcessor<OrderImportResult> {

    @Override
    public List<OrderImportResult> process(InputStream inputStream) {
        List<OrderImportResult> orderImportResults = new ArrayList<>();

        try(Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet currentSheet = workbook.getSheetAt(OrderExcelTemplate.SHEET_NUMBER);
            log.info("현재 액셀 작업 시트명: {}", currentSheet.getSheetName());

            String ordererName = parseOrdererInfo(currentSheet, OrderExcelTemplate.ORDERER_NAME);
            String ordererAddress = parseOrdererInfo(currentSheet, OrderExcelTemplate.ORDERER_ADDRESS);
            List<OrderImportResult.OrderProductInfoResult> orderProductInfoResults = parseProductInfos(currentSheet);

            orderImportResults.add(new OrderImportResult(ordererName, ordererAddress, orderProductInfoResults));
        } catch (IOException e) {
            throw new ExcelFileProcessingException("엑셀 파일을 처리 도중 오류가 발생했습니다.", e);
        } catch (NullPointerException | IllegalStateException | NotOfficeXmlFileException e) {
            throw new ExcelFileProcessingException("엑셀 파일의 데이터가 올바르지 않습니다.", e);
        }

        return orderImportResults;
    }

    private String parseOrdererInfo(Sheet sheet, OrderExcelTemplate template) {
        Row row = sheet.getRow(template.getRowIndex());

        return template.getCellValue(row);
    }

    private List<OrderImportResult.OrderProductInfoResult> parseProductInfos(Sheet sheet) {
        List<OrderImportResult.OrderProductInfoResult> productInfos = new ArrayList<>();

        for (int i = OrderExcelTemplate.START_ROW_INDEX; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null || isRowEmpty(row)) {
                break;
            }

            OrderImportResult.OrderProductInfoResult productInfo = new OrderImportResult.OrderProductInfoResult(
                OrderExcelTemplate.PRODUCT_ID.getCellValue(row),
                OrderExcelTemplate.PRODUCT_NAME.getCellValue(row),
                OrderExcelTemplate.QUANTITY.getCellValue(row)
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
