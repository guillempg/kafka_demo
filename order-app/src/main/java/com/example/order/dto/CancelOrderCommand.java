package com.example.order.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Value
@Builder
public class CancelOrderCommand {

    @NotNull(message = "Please provide the order id")
    @Positive(message = "Value must be positive")
    @Min(value = 1, message = "Value must be greater than 1")
    private Long orderId;

    @NotBlank(message = "Please provide a User value")
    private String user;
}
