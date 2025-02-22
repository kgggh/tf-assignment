package kr.co.teamfresh.assignment.presentation.order;

import jakarta.validation.Valid;
import kr.co.teamfresh.assignment.FileUtil;
import kr.co.teamfresh.assignment.application.OrderService;
import kr.co.teamfresh.assignment.presentation.order.request.OrderCreateRequest;
import kr.co.teamfresh.assignment.presentation.order.request.OrderFileCreateRequest;
import kr.co.teamfresh.assignment.presentation.order.response.OrderCreateResponse;
import kr.co.teamfresh.assignment.presentation.order.response.OrderFileCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@RequiredArgsConstructor
@RequestMapping("/api/orders")
@RestController
public class OrderController {
    private final OrderService orderService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public OrderCreateResponse createOrder(@Valid @RequestBody OrderCreateRequest orderCreateRequest) {
        return new OrderCreateResponse(orderService.processOrder(orderCreateRequest));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/import")
    public OrderFileCreateResponse createOrderFromFile(@RequestPart(name = "file") MultipartFile file) {
        try {
            OrderFileCreateRequest orderFileRequest = new OrderFileCreateRequest(
                file.getOriginalFilename(),
                FileUtil.extractFileExtension(file.getOriginalFilename()),
                file.getInputStream()
            );

            return new OrderFileCreateResponse(orderService.importOrderFromFile(orderFileRequest));
        } catch (IOException e) {
            throw new UncheckedIOException("파일을 처리하는 중 오류가 발생했습니다.", e);
        }
    }
}
