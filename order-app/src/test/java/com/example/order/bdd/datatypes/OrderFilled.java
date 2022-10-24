package com.example.order.bdd.datatypes;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class OrderFilled {

    private Long orderId;
    private Long quantityFilled;
    private String counterparty;
}
