package kr.co.teamfresh.assignment.presentation.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OrderCreateRequest(
    @NotNull(message = "주문자명은 필수입니다.")
    @NotBlank(message = "주문자명은 빈 문자열이 될 수 없습니다.")
    String ordererName,

    @NotNull(message = "주소는 필수입니다.")
    @NotBlank(message = "주소는 빈 문자열이 될 수 없습니다.")
    String address,

    @NotNull(message = "주문 상품 정보는 필수입니다.")
    @NotEmpty(message = "주문 상품 정보는 최소 한 개 이상이어야 합니다.")
    List<@Valid OrderProductInfo> orderProducts
) {
    public record OrderProductInfo(
        @NotNull(message = "상품 ID는 필수입니다.")
        @Positive(message = "상품 ID는 0이 될수 없습니다.")
        Long productId,

        @NotNull(message = "상품명은 필수입니다.")
        @NotBlank(message = "상품명은 빈 문자열이 될 수 없습니다.")
        String productName,

        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1 이상이어야 합니다.")
        Integer quantity
    ) { }
}
