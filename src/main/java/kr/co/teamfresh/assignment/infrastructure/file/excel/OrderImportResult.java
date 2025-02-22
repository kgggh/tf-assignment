package kr.co.teamfresh.assignment.infrastructure.file.excel;

import java.util.List;

public record OrderImportResult(
    String ordererName,
    String address,
    List<OrderProductInfo> orderProducts
) {
    public record OrderProductInfo(
        Long productId,
        String productName,
        Integer quantity
    ) { }
}
