package kr.co.teamfresh.assignment.domain.order;

import jakarta.persistence.*;
import kr.co.teamfresh.assignment.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class OrderProduct extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    protected OrderProduct(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public static OrderProduct of(Long productId, int quantity) {
        return new OrderProduct(productId, quantity);
    }

    public void assignToOrder(Order order) {
        this.order = order;
    }
}
