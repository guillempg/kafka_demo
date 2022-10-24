package com.example.order.controller;

import com.example.order.domain.Order;
import com.example.order.dto.CancelOrderCommand;
import com.example.order.dto.PlaceOrderCommand;
import com.example.order.exception.OrderNotFoundException;
import com.example.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.net.URI;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(final OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(value = "/place")
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<Order> placeOrder(@RequestBody PlaceOrderCommand placeOrder) {
        final Order order = orderService.placeOrder(placeOrder);
        return ResponseEntity
                .created(URI.create("/" + order.getId()))
                .body(order);
    }

    @PostMapping(value = "/cancel/{id}")
    @ResponseStatus(HttpStatus.OK)
    void cancelOrder(@PathVariable("id")
                     @Valid
                     @Positive
                     @Min(value = 1, message = "Value must be positive nonzero value")
                     Long id) {
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderId(id)
                .user("TestUser")
                .build();
        orderService.cancelOrder(command);
    }

    @GetMapping("/{id}")
    Order findOrder(@PathVariable("id")
                    @Valid
                    @Positive
                    @Min(value = 1, message = "Value must be positive nonzero value")
                    Long id) {
        return orderService.findOrder(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @ControllerAdvice
    static class OrderNotFoundAdvice {
        @ResponseBody
        @ExceptionHandler(OrderNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        String orderNotFoundHandler(OrderNotFoundException ex) {
            return ex.getMessage();
        }
    }

}
