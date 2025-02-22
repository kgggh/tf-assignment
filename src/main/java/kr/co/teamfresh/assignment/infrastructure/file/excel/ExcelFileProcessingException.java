package kr.co.teamfresh.assignment.infrastructure.file.excel;

public class ExcelFileProcessingException extends RuntimeException {

    public ExcelFileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelFileProcessingException(String message) {
        super(message);
    }
}
