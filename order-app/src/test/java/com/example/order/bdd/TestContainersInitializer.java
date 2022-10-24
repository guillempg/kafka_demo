package com.example.order.bdd;

import com.example.order.bdd.testcontainers.KafkaContainer;
import com.example.order.bdd.testcontainers.PostgresContainer;
import com.example.order.bdd.testcontainers.SchemaRegistryContainer;
import com.example.order.bdd.testcontainers.ZookeeperContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final String CONFLUENT_PLATFORM_VERSION = "5.5.0";
    private static final String GROUP_ID = "OrderGroup";

    private static Network network;
    private static PostgresContainer postgres;
    private static KafkaContainer kafka;
    private static ZookeeperContainer zookeeper;
    private static SchemaRegistryContainer schemaRegistry;


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        network = Network.newNetwork();
        zookeeper = new ZookeeperContainer()
                .withNetwork(network);
        kafka = new KafkaContainer(zookeeper.getInternalUrl())
                .withNetwork(network);
        schemaRegistry = new SchemaRegistryContainer(zookeeper.getInternalUrl())
                .withNetwork(network);
        postgres = new PostgresContainer()
                .withNetwork(network);

        Startables
                .deepStart(postgres, zookeeper, kafka, schemaRegistry)
                .orTimeout(2, TimeUnit.MINUTES)
                .join();


//        final OrderAppInitializer orderApp = new OrderAppInitializer(network);
//        orderApp.initialize(applicationContext);

        TestPropertyValues.of(
                        "spring.datasource.driverClassName=org.postgresql.Driver",
                        "spring.datasource.url=" + postgres.getJdbcUrl(),
                        "spring.datasource.username=user",
                        "spring.datasource.password=secret",
                        "spring.kafka.bootstrap-servers=" + kafka.getBootstrapServers(),
                        "spring.kafka.consumer.group-id=" + GROUP_ID,
                        "schema.registry.url=" + schemaRegistry.getUrl()
                )
                .applyTo(applicationContext.getEnvironment());
    }

}
