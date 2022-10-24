package com.example.order.bdd.datatypes;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ConsumerAndMessage {

    private String consumerName;
    private List<OrderPlaced> messages;
}
