package kr.co.teamfresh.assignment.application;

import kr.co.teamfresh.assignment.domain.order.Order;
import kr.co.teamfresh.assignment.domain.order.OrderProduct;
import kr.co.teamfresh.assignment.domain.order.OrderRepository;
import kr.co.teamfresh.assignment.domain.order.Orderer;
import kr.co.teamfresh.assignment.domain.product.Product;
import kr.co.teamfresh.assignment.domain.product.ProductRepository;
import kr.co.teamfresh.assignment.infrastructure.file.OrderFileProcessor;
import kr.co.teamfresh.assignment.infrastructure.file.OrderFileProcessorProvider;
import kr.co.teamfresh.assignment.infrastructure.file.excel.OrderImportResult;
import kr.co.teamfresh.assignment.infrastructure.lock.LockCoordinator;
import kr.co.teamfresh.assignment.presentation.order.request.OrderCreateRequest;
import kr.co.teamfresh.assignment.presentation.order.request.OrderFileCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderFileProcessorProvider orderFileProcessorProvider;
    private final LockCoordinator lockCoordinator;
    private static final String PRODUCT_LOCK_KEY_PREFIX = "product:";

    @Override
    public Long processOrder(OrderCreateRequest orderCreateRequest) {
        List<Long> productIds = extractProductIdsFromRequest(orderCreateRequest);

        return processOrderFromRequest(productIds, orderCreateRequest);
    }

    private List<Long> extractProductIdsFromRequest(OrderCreateRequest request) {
        return request.orderProducts().stream()
            .map(OrderCreateRequest.OrderProductInfo::productId)
            .toList();
    }

    private Long processOrderFromRequest(List<Long> productIds, OrderCreateRequest request) {
        List<String> productLockKeys = productIds.stream().map(id -> PRODUCT_LOCK_KEY_PREFIX + id).toList();

        List<OrderProduct> orderProducts = new ArrayList<>();

        try {
            lockCoordinator.multipleLock(productLockKeys);

            Map<Long, Product> productMap = getProductMapByIds(productIds);

            for (OrderCreateRequest.OrderProductInfo orderProductInfo : request.orderProducts()) {
                Long requestProductId = orderProductInfo.productId();

                Product product = productMap.get(requestProductId);
                validateAndDecreaseStock(product, orderProductInfo.quantity(), requestProductId);

                orderProducts.add(OrderProduct.of(product.getId(), orderProductInfo.quantity()));
            }

            return saveOrderTransaction(orderProducts, request.ordererName(), request.address());
        } finally {
            lockCoordinator.multipleUnlock(productLockKeys);
        }
    }

    private Map<Long, Product> getProductMapByIds(List<Long> productIds) {
        return productRepository.findByIdIn(productIds).stream()
            .collect(Collectors.toMap(Product::getId, product -> product));
    }

    @Override
    public Long importOrderFromFile(OrderFileCreateRequest orderFileRequest) {
        OrderFileProcessor processor = orderFileProcessorProvider.getProcessor(orderFileRequest.extension());
        OrderImportResult orderImportResult = processor.process(orderFileRequest.content());

        List<Long> productIds = extractProductIdsFromFile(orderImportResult);

        return processOrderFromFile(productIds, orderImportResult);
    }

    @Transactional
    public Long saveOrderTransaction(List<OrderProduct> orderProducts, String ordererName, String address) {
        Order createdOrder = Order.create(new Orderer(ordererName, address), orderProducts);

        return orderRepository.save(createdOrder).getId();
    }

    private void validateAndDecreaseStock(Product product, int quantity, Long requestProductId) {
        if (product == null) {
            throw new IllegalArgumentException("해당 상품이 존재하지 않습니다, 상품 ID: " + requestProductId);
        }

        if (!product.hasStockFor(quantity)) {
            throw new IllegalArgumentException("재고가 부족합니다. 상품 ID: " + product.getId() + ", 현재 재고: " + product.getStock());
        }

        product.decreaseStock(quantity);
    }

    private List<Long> extractProductIdsFromFile(OrderImportResult importResult) {
        return importResult.orderProducts().stream()
            .map(OrderImportResult.OrderProductInfo::productId)
            .toList();
    }

    private Long processOrderFromFile(List<Long> productIds, OrderImportResult importResult) {
        List<String> productLockKeys = productIds.stream().map(id -> PRODUCT_LOCK_KEY_PREFIX + id).toList();

        List<OrderProduct> orderProducts = new ArrayList<>();

        try {
            lockCoordinator.multipleLock(productLockKeys);

            Map<Long, Product> productMap = getProductMapByIds(productIds);

            for (OrderImportResult.OrderProductInfo orderProductInfo : importResult.orderProducts()) {
                Long requestProductId = orderProductInfo.productId();
                Product product = productMap.get(requestProductId);

                validateAndDecreaseStock(product, orderProductInfo.quantity(), requestProductId);
                orderProducts.add(OrderProduct.of(product.getId(), orderProductInfo.quantity()));
            }

            return saveOrderTransaction(orderProducts, importResult.ordererName(), importResult.address());
        } finally {
            lockCoordinator.multipleUnlock(productLockKeys);
        }
    }
}
