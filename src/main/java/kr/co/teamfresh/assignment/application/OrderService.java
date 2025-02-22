package kr.co.teamfresh.assignment.application;

import kr.co.teamfresh.assignment.presentation.order.request.OrderCreateRequest;
import kr.co.teamfresh.assignment.presentation.order.request.OrderFileCreateRequest;

public interface OrderService {
    Long processOrder(OrderCreateRequest orderCreateRequest);
    Long importOrderFromFile(OrderFileCreateRequest orderFileRequest);
}
