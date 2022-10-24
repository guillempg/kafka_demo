package com.example.order.bdd.datatypes;

import com.example.order.domain.OrderType;
import com.example.order.domain.Side;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class OrderPlaced {

    private OrderType orderType;
    private Long orderId;
    private String symbol;
    private Integer quantity;
    private Side side;
}
