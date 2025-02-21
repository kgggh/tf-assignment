package kr.co.teamfresh.assignment.domain.order;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("주문 접수");

    private final String description;
}
