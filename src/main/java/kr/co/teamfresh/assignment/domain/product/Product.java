package kr.co.teamfresh.assignment.domain.product;

import jakarta.persistence.*;
import kr.co.teamfresh.assignment.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int stock;

    protected Product(String name, int stock) {
        this.name = name;
        this.stock = stock;
    }

    public static Product register(String name, int stock) {
        return new Product(name, stock);
    }

    public boolean hasStockFor(int quantity) {
        if (quantity <= 0) {
            return false;
        }

        return stock >= quantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(this.id + "주문 수량은 1개 이상이어야 합니다.");
        }

        if (!hasStockFor(quantity)) {
            throw new IllegalStateException("재고가 부족합니다. 상품 ID: " + this.id + ", 재고: " + this.stock);
        }

        this.stock -= quantity;
    }
}
