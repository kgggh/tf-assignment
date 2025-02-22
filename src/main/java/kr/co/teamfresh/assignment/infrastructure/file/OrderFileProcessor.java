package kr.co.teamfresh.assignment.infrastructure.file;

import kr.co.teamfresh.assignment.infrastructure.file.excel.OrderImportResult;

import java.io.InputStream;

public interface OrderFileProcessor {
    OrderImportResult process(InputStream inputStream);
    FileExtension getExtension();
}
