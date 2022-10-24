package com.example.order.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "orders")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner")
    private String owner;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "side")
    private Side side;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "orderType")
    @Enumerated(EnumType.STRING)
    private OrderType orderType;
}
