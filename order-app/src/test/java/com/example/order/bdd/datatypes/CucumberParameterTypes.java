package com.example.order.bdd.datatypes;

import com.example.order.domain.Order;
import com.example.order.domain.OrderType;
import com.example.order.domain.Side;
import com.example.order.dto.PlaceOrderCommand;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CucumberParameterTypes {

    private static final String ID_PLACEHOLDER = "#ID_PLACEHOLDER";
    private static final String ORDER_ID = "orderId";
    private static final String QUANTITY = "quantity";
    private static final String SYMBOL = "symbol";
    private static final String ORDER_TYPE = "orderType";
    private static final String OWNER = "owner";
    private static final String SIDE = "side";

    @ParameterType(name = "components", value = "(.+?)(?:,|$)") //, value = "
    public List<String> components(final String componentCsvList) {

        return Arrays.stream(componentCsvList.split(",")).sequential()
                .map(String::trim)
                .collect(Collectors.toList());
    }

    @DataTableType
    public ConsumerOfGroup consumerofGroup(final Map<String, String> map) {
        return ConsumerOfGroup.builder()
                .name(map.get("name"))
                .partitionId(Integer.valueOf(map.get("partitionId")))
                .build();
    }

    private List<OrderPlaced> convert(List<String> expectedMessages) {
        return expectedMessages.stream()
                .map(msg -> {

                    msg = msg.replaceAll("\\)", "");
                    //OrderType type = OrderType.valueOf(msg.split("orderType=")[1].split(",")[0]);
                    Long orderId = Long.valueOf(msg.split("orderId")[1].split(",")[0]);
                    String product = msg.split("symbol")[1].split(",")[0];
                    Integer quantity = Integer.valueOf(msg.split("quantity")[1].split(",")[0]);

                    return OrderPlaced.builder()
                            //.orderType(type)
                            .quantity(quantity)
                            .symbol(product)
                            .orderId(orderId)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @DataTableType
    public String expectedMessages(final Map<String, String> map) {
        return map.get("payload");
    }

    @DataTableType
    public PlaceOrderCommand placeOrderCommand(final Map<String, String> map) {

        return PlaceOrderCommand.builder()
                .orderType(OrderType.valueOf(map.get(ORDER_TYPE).toUpperCase()))
                .owner(map.get(OWNER))
                .symbol(map.get(SYMBOL))
                .quantity(Integer.valueOf(map.get(QUANTITY)))
                .side(Side.valueOf(map.get(SIDE).toUpperCase()))
                .build();
    }

    @DataTableType
    public Order order(final Map<String, String> map) {

        return Order.builder()
                .quantity(Integer.valueOf(map.get(QUANTITY)))
                .symbol(map.get(SYMBOL))
                .owner(map.get(OWNER))
                .side(Side.valueOf(map.get(SIDE).toUpperCase()))
                .orderType(OrderType.valueOf(map.get(ORDER_TYPE).toUpperCase()))
                .build();
    }

    @DataTableType
    public OrderPlaced orderPlaced(final Map<String, String> map) {

        Long id = null;
        if (!map.get(ORDER_ID).equals(ID_PLACEHOLDER)) {
            id = Long.valueOf(map.getOrDefault(ORDER_ID, null));
        }

        return OrderPlaced.builder()
                .orderId(id)
                .orderType(OrderType.valueOf(map.get(ORDER_TYPE).toUpperCase()))
                .quantity(Integer.valueOf(map.get(QUANTITY)))
                .symbol(map.get(SYMBOL))
                .side(Side.valueOf(map.get(SIDE).toUpperCase()))
                .build();
    }

    @DataTableType
    public OrderCancelled orderCancelled(final Map<String, String> map) {
        return OrderCancelled.builder()
                .orderId(Long.valueOf(map.get("orderId")))
                .user(map.get("user"))
                .build();
    }

    @DataTableType
    public OrderFilled orderFilled(final Map<String, String> map) {
        return OrderFilled.builder()
                .orderId(Long.valueOf(map.get("orderId")))
                .quantityFilled(Long.valueOf(map.get("quantity")))
                .counterparty(map.get("counterparty"))
                .build();
    }

    @DataTableType
    public ExpectedOrderEvent expectedOrderEvent(final Map<String, String> map) {
        return ExpectedOrderEvent.builder()
                .orderId(Long.valueOf(map.get("orderId")))
                .eventType(Enum.valueOf(ExpectedOrderEvent.EventType.class, map.get("msgType")))
                .build();
    }

}
