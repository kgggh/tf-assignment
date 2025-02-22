package kr.co.teamfresh.assignment.infrastructure.file.excel;

import java.util.List;

public record OrderImportResult(
    String ordererName,
    String ordererAddress,
    List<OrderProductInfoResult> productInfoResults
) {
    public record OrderProductInfoResult(
        Long productId,
        String productName,
        Integer quantity
    ) { }
}
