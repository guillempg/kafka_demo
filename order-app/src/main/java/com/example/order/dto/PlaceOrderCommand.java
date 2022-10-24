package com.example.order.dto;

import com.example.order.domain.OrderType;
import com.example.order.domain.Side;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Value
@Builder
public class PlaceOrderCommand {

    @NotBlank(message = "Please provide the owner")
    private String owner;

    @NotBlank(message = "Please provide a Symbol value")
    private String symbol;

    @NotNull
    @Positive(message = "Value must be positive")
    @Min(value = 1, message = "Value must be greater than 1")
    private Integer quantity;

    @NotNull(message = "specify whether the order is a BUY/SELL")
    private Side side;

    @NotNull(message = "Specify whether the order type is MARKET/LIMIT order")
    private OrderType orderType;
}
