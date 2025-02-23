package kr.co.teamfresh.assignment.application;

import kr.co.teamfresh.assignment.domain.order.Order;
import kr.co.teamfresh.assignment.domain.order.OrderProduct;
import kr.co.teamfresh.assignment.domain.order.OrderRepository;
import kr.co.teamfresh.assignment.domain.order.Orderer;
import kr.co.teamfresh.assignment.domain.product.Product;
import kr.co.teamfresh.assignment.domain.product.ProductRepository;
import kr.co.teamfresh.assignment.infrastructure.file.OrderFileProcessorProvider;
import kr.co.teamfresh.assignment.infrastructure.file.excel.ExcelOrderProcessor;
import kr.co.teamfresh.assignment.infrastructure.file.excel.OrderImportResult;
import kr.co.teamfresh.assignment.infrastructure.lock.LockCoordinator;
import kr.co.teamfresh.assignment.presentation.order.request.OrderCreateRequest;
import kr.co.teamfresh.assignment.presentation.order.request.OrderFileCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderFileProcessorProvider orderFileProcessorProvider;

    @Mock
    private LockCoordinator lockCoordinator;

    @Mock
    private ExcelOrderProcessor excelOrderProcessor;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Product createProduct(Long id, String name, int stock) {
        var product = Product.register(name, stock);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    @Test
    void 일반주문시_주문자정보_및_주문상품의_재고가_있을시_정상적으로_주문이_된다() {
        //given
        var product1 = createProduct(1L, "테스트 상품1", 100);
        var product2 = createProduct(2L, "테스트 상품2", 100);

        when(productRepository.findByIdIn(any()))
            .thenReturn(List.of(product1, product2));

        var createdOrder = Order.create(new Orderer("김아무개", "경기도 용인시 처인구"),
            List.of(OrderProduct.of(product1.getId(), 1), OrderProduct.of(product2.getId(), 1)));
        ReflectionTestUtils.setField(createdOrder, "id", 1L);

        when(orderRepository.save(any(Order.class)))
            .thenReturn(createdOrder);

        var orderCreateRequest = new OrderCreateRequest(
            "김아무개",
            "경기도 용인시 처인구",
            List.of(new OrderCreateRequest.OrderProductInfo(1L, "테스트 상품1", 1))
        );

        //when
        Long orderId = orderService.processOrder(orderCreateRequest);

        //then
        assertThat(orderId).isEqualTo(1);
        verify(orderRepository, times(1)).save(any());
        verify(lockCoordinator, times(1)).multipleLock(any());
        verify(lockCoordinator, times(1)).multipleUnlock(any());
    }

    @Test
    void 파일_주문시_지원하지_않는_파일_확장자일시_주문에_실패한다() {
        //given
        var orderCreateRequest = new OrderFileCreateRequest("테스트파일.txt", "txt", null);

        when(orderFileProcessorProvider.getProcessor("txt"))
            .thenThrow(new RuntimeException("지원되지 않는 파일 타입: txt"));

        //when
        //then
        assertThatThrownBy(() -> orderService.importOrderFromFile(orderCreateRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("지원되지 않는 파일 타입: txt");

        verify(orderFileProcessorProvider, times(1)).getProcessor("txt");
    }

    @Test
    void 주문상품이_존재하지_않을때_주문_실패_한다() {
        //given
        var product1 = createProduct(1L, "테스트 상품1", 100);
        var product2 = createProduct(2L, "테스트 상품2", 100);

        when(productRepository.findByIdIn(any()))
            .thenReturn(List.of(product1, product2));

        var orderCreateRequest = new OrderCreateRequest(
            "김아무개",
            "경기도 용인시 처인구",
            List.of(new OrderCreateRequest.OrderProductInfo(999L, "확인불가 상품", 1))
        );

        //when
        //then
        assertThatThrownBy(() -> orderService.processOrder(orderCreateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("해당 상품이 존재하지 않습니다");
        verify(orderRepository, times(0)).save(any());
    }

    @Test
    void 주문상품의_재고가_부족하면_주문_실패한다() {
        //given
        var product1 = createProduct(1L, "재고 없는 상품", 0);

        when(productRepository.findByIdIn(any()))
            .thenReturn(List.of(product1));

        var orderCreateRequest = new OrderCreateRequest(
            "김아무개",
            "경기도 용인시 처인구",
            List.of(new OrderCreateRequest.OrderProductInfo(1L, "재고가 부족한 상품", 1))
        );

        //when
        //then
        assertThatThrownBy(() -> orderService.processOrder(orderCreateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("재고가 부족합니다");
        verify(orderRepository, times(0)).save(any());
    }

    @Test
    void 파일_주문시_주문상품이_존재하지_않으면_주문에_실패한다() {
        // given
        when(productRepository.findByIdIn(any()))
            .thenReturn(Collections.emptyList());

        when(orderFileProcessorProvider.getProcessor("xlsx"))
            .thenReturn(excelOrderProcessor);

        when(excelOrderProcessor.process(any()))
            .thenReturn(new OrderImportResult("김아무개", "경기도 용인시 처인구", List.of(
                new OrderImportResult.OrderProductInfo(999L, "확인불가 상품", 1) // 존재하지 않는 상품 ID
            )));

        var orderCreateRequest = new OrderFileCreateRequest("테스트파일.xlsx", "xlsx", null);

        // when & then
        assertThatThrownBy(() -> orderService.importOrderFromFile(orderCreateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("해당 상품이 존재하지 않습니다");

        verify(orderRepository, times(0)).save(any());
    }

    @Test
    void 파일_주문시_주문상품의_재고가_부족하면_주문에_실패한다() {
        // given
        var product = createProduct(1L, "재고 없는 상품", 0);

        when(productRepository.findByIdIn(any()))
            .thenReturn(List.of(product));

        when(orderFileProcessorProvider.getProcessor("xlsx"))
            .thenReturn(excelOrderProcessor);

        when(excelOrderProcessor.process(any()))
            .thenReturn(new OrderImportResult("김아무개", "경기도 용인시 처인구", List.of(
                new OrderImportResult.OrderProductInfo(1L, "재고 없는 상품", 1) // 재고 부족한 상품 요청
            )));

        var orderCreateRequest = new OrderFileCreateRequest("테스트파일.xlsx", "xlsx", null);

        // when & then
        assertThatThrownBy(() -> orderService.importOrderFromFile(orderCreateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("재고가 부족합니다");

        verify(orderRepository, times(0)).save(any()); // 주문이 저장되지 않아야 함
    }
}

