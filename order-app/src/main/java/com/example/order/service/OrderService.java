package com.example.order.service;

import com.example.order.domain.Order;
import com.example.order.domain.OrderStatus;
import com.example.order.dto.CancelOrderCommand;
import com.example.order.dto.PlaceOrderCommand;
import com.example.order.exception.OrderNotFoundException;
import com.example.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepo;
    private final KafkaService kafkaService;

    @Autowired
    public OrderService(final OrderRepository orderRepository, KafkaService kafkaService) {
        this.orderRepo = orderRepository;
        this.kafkaService = kafkaService;
    }

    public Order placeOrder(final PlaceOrderCommand placeOrderCommand) {
        final Order order = saveOrder(placeOrderCommand);

        kafkaService.sendOrderPlaced(order);

        return order;

    }

    public void cancelOrder(CancelOrderCommand cancelOrderCommand) {
        Order order = orderRepo
                .findById(cancelOrderCommand.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(cancelOrderCommand.getOrderId()));
        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);

        kafkaService.sendOrderCanceled(order.getId(), cancelOrderCommand.getUser());
    }

    public Optional<Order> findOrder(final Long orderId) {
        return orderRepo.findById(orderId);
    }

    private Order saveOrder(PlaceOrderCommand command) {
        return orderRepo.save(Order.builder()
                .status(OrderStatus.PLACED)
                .owner(command.getOwner())
                .symbol(command.getSymbol())
                .quantity(command.getQuantity())
                .side(command.getSide())
                .orderType(command.getOrderType())
                .build());
    }
}
