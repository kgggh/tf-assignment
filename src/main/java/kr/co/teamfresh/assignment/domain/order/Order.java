package kr.co.teamfresh.assignment.domain.order;

import kr.co.teamfresh.assignment.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Orderer orderer;

    @Embedded
    private OrdererAddress ordererAddress;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    protected Order(Orderer orderer, OrdererAddress ordererAddress, List<OrderProduct> orderProducts) {
        this.orderer = orderer;
        this.status = OrderStatus.PENDING;
        this.ordererAddress = ordererAddress;
        this.orderProducts = orderProducts;
    }

    public static Order create(Orderer orderer, OrdererAddress ordererAddress, List<OrderProduct> orderProducts) {
        return new Order(orderer, ordererAddress, orderProducts);
    }

    public void addOrderProduct(OrderProduct orderProduct) {
        this.orderProducts.add(orderProduct);
        orderProduct.assignToOrder(this);
    }
}
