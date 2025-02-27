package kr.co.teamfresh.assignment.infrastructure.file.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class OrderExcelOrderProcessorTest {
    private ExcelOrderProcessor orderExcelFileProcessor;

    @BeforeEach
    void init() {
        orderExcelFileProcessor = new ExcelOrderProcessor();
    }

    @Test
    void 엑셀파일이_주문양식에_옳바르게_작성되면_주문_데이터를_처리해_파싱할_수_있다() throws Exception {
        //given
        var resource = new ClassPathResource("/excel/테스트_정상_주문서.xlsx");
        var inputStream = resource.getInputStream();

        var ordererName = "김아무개";
        var ordererAddress = "경기도 용인시 처인구 백옥대로 xxx-xx길";

        //when
        var orderImportResult = orderExcelFileProcessor.process(inputStream);


        //then
        assertThat(orderImportResult.ordererName()).isEqualTo(ordererName);
        assertThat(orderImportResult.address()).isEqualTo(ordererAddress);

        List<OrderImportResult.OrderProductInfo> orderProductInfos = orderImportResult.orderProducts();
        assertThat(orderProductInfos)
            .extracting(OrderImportResult.OrderProductInfo::productId)
            .containsExactly(1L, 2L, 3L);

        assertThat(orderImportResult.orderProducts())
            .extracting(OrderImportResult.OrderProductInfo::productName)
            .containsExactly("사과", "바나나", "파인애플");

        assertThat(orderImportResult.orderProducts())
            .extracting(OrderImportResult.OrderProductInfo::quantity)
            .containsExactly(2, 4, 6);
    }

    @Test
    void 엑셀_이외의_파일인_경우_처리에_실패한다() throws Exception {
        //given
        var resource = new ClassPathResource("/excel/미확인_주문서.txt");
        var inputStream = resource.getInputStream();

        //when
        //then
        assertThatThrownBy(() -> orderExcelFileProcessor.process(inputStream))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 제공과_다른_양식의_엑셀파일인_경우_파싱에_실패한다() throws Exception {
        //given
        var resource = new ClassPathResource("/excel/다른_양식의_주문서.xlsx");
        var inputStream = resource.getInputStream();

        //when
        //then
        assertThatThrownBy(() -> orderExcelFileProcessor.process(inputStream))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("엑셀 파일의 데이터가 올바르지 않습니다.");
    }
}
