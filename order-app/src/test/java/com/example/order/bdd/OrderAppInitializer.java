package com.example.order.bdd;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
public class OrderAppInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final Logger LOGGER = LoggerFactory.getLogger("ORDER-APP-LOGGER");
    private final GenericContainer<?> orderApp;

    private Network network = Network.newNetwork();

    public OrderAppInitializer() {
        this(Network.newNetwork());
    }

    public OrderAppInitializer(Network network){
        this.network = network;
        orderApp = new GenericContainer<>(DockerImageName.parse("example/order-app-box:latest"))
                .withNetwork(network)
                .withNetworkAliases("order-app")
                .withEnv("KAFKA_HOST","kafka");
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        orderApp.start();
        orderApp.followOutput(new Slf4jLogConsumer(LOGGER));
    }
}
