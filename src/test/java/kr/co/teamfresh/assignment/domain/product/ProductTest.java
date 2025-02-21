package kr.co.teamfresh.assignment.domain.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ExtendWith(MockitoExtension.class)
class ProductTest {

    @Test
    void 상품등록() {
        //given
        var name = "상품1";
        var stock = 10;

        //when
        var product = Product.register(name, stock);

        //then
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getStock()).isEqualTo(stock);
    }

    @Test
    void 상품_요청수량보다_재고수량이_많으면_재고수량이_차감된다() {
        //given
        var name = "상품1";
        var stock = 10;
        var product = Product.register(name, stock);

        //when
        product.decreaseStock(7);

        //then
        assertThat(product.getStock()).isEqualTo(3);
    }

    @Test
    void 상품_요청수량이_1보다_작으면_수량이_차감되지_않는다() {
        //given
        var name = "상품1";
        var stock = 10;
        var product = Product.register(name, stock);

        var requestQuantity = 0;

        //when
        //then
        assertThatThrownBy(() -> product.decreaseStock(requestQuantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 0보다 커야 합니다.");
    }

    @Test
    void 상품_요청수량보다_재고수량이_적으면_재고수량이_차감되지_않는다() {
        //given
        var name = "상품1";
        var stock = 10;
        var product = Product.register(name, stock);
        var requestQuantity = 20;

        //when
        //then
        assertThatThrownBy(() -> product.decreaseStock(requestQuantity))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("재고가 부족합니다.");
    }
}
