package com.example.order.bdd.datatypes;

public class Mappers {

    public static com.example.order.kafka.OrderPlaced map(final OrderPlaced orderPlaced) {
        return com.example.order.kafka.OrderPlaced.newBuilder()
                .setOrderId(String.valueOf(orderPlaced.getOrderId()))
                .setSymbol(orderPlaced.getSymbol())
                .setSide(com.example.order.service.Converters.toKafka(orderPlaced.getSide()))
                .setQuantity(orderPlaced.getQuantity())
                .setOrderType(com.example.order.service.Converters.toKafka(orderPlaced.getOrderType()))
                .build();
    }

    public static com.example.order.kafka.OrderCanceled map(final OrderCancelled orderCancelled) {
        return com.example.order.kafka.OrderCanceled.newBuilder()
                .setOrderId(String.valueOf(orderCancelled.getOrderId()))
                .setUser(orderCancelled.getUser())
                .build();
    }

    public static com.example.order.kafka.OrderFilled map(final OrderFilled orderFilled) {
        return com.example.order.kafka.OrderFilled.newBuilder()
                .setOrderId(String.valueOf(orderFilled.getOrderId()))
                .build();
    }
}
