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
public class BillingAppInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final Logger BILLING_LOGGER = LoggerFactory.getLogger("BILLING-LOGGER");
    private final GenericContainer<?> billingApp;

    private Network network = Network.newNetwork();

    public BillingAppInitializer(){
        billingApp = new GenericContainer<>(DockerImageName.parse("localhost:5000/billing-app-container"))
                .withNetwork(network)
                .withNetworkAliases("billing")
                .withEnv("KAFKA_HOST","kafka");
    }

    public BillingAppInitializer(Network network){
        this();
        this.network = network;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        billingApp.start();
        billingApp.followOutput(new Slf4jLogConsumer(BILLING_LOGGER));
    }
}
