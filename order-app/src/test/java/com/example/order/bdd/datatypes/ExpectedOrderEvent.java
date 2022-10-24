package com.example.order.bdd.datatypes;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class ExpectedOrderEvent {

    private Long orderId;
    private EventType eventType;

    public enum EventType { PLACED, CANCELED, FILLED }
}
