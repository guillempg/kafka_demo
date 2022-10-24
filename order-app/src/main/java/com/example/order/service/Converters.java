package com.example.order.service;

import com.example.order.domain.Order;
import com.example.order.domain.OrderType;
import com.example.order.domain.Side;
import com.example.order.dto.CancelOrderCommand;
import com.example.order.kafka.OrderCanceled;
import com.example.order.kafka.OrderPlaced;

public class Converters {

    public static OrderPlaced createOrderPlacedEvent(final Order order) {
        return OrderPlaced.newBuilder()
                .setOrderId(String.valueOf(order.getId()))
                .setSymbol(order.getSymbol())
                .setSide(toKafka(order.getSide()))
                .setQuantity(order.getQuantity())
                .setOrderType(toKafka(order.getOrderType()))
                .build();
    }

    public static OrderCanceled createOrderCanceledEvent(final Long orderId, final String user) {
        return OrderCanceled.newBuilder()
                .setOrderId(String.valueOf(orderId))
                .setUser(user)
                .build();
    }

    public static OrderCanceled toKafka(final CancelOrderCommand cancelOrderCommand) {
        return OrderCanceled.newBuilder()
                .setOrderId(String.valueOf(cancelOrderCommand.getOrderId()))
                .setUser(cancelOrderCommand.getUser())
                .build();
    }

    public static com.example.order.kafka.Side toKafka(final Side side) {
        switch (side) {
            case BUY:
                return com.example.order.kafka.Side.BUY;
            case SELL:
                return com.example.order.kafka.Side.SELL;
            default:
                throw new IllegalArgumentException("Cannot convert Side to avro");
        }
    }

    public static com.example.order.kafka.OrderType toKafka(OrderType orderType) {
        if (orderType == null) {
            return com.example.order.kafka.OrderType.MARKET;
        }

        switch (orderType) {
            case LIMIT:
                return com.example.order.kafka.OrderType.LIMIT;
            case MARKET: default:
                return com.example.order.kafka.OrderType.MARKET;
//            default:
//                throw new IllegalArgumentException("Cannot convert OrderType to avro");
        }
    }
}
