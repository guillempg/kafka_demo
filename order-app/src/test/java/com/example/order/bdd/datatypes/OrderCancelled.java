package com.example.order.bdd.datatypes;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class OrderCancelled {

    private Long orderId;
    private String user;
}
