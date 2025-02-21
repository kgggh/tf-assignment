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

    public void decreaseStock(int quantity) {
        if(quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }

        if(quantity > stock) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        this.stock -= quantity;
    }
}
