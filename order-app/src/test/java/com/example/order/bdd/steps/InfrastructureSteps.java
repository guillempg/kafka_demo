package com.example.order.bdd.steps;

import io.cucumber.java.en.Given;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.PartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class InfrastructureSteps {

    @Autowired
    DataSource dataSource;

    @Autowired
    KafkaProducer<Long, SpecificRecord> kafkaProducer;

    @Autowired
    KafkaConsumer<String, List<PartitionInfo>> kafkaTopicConsumer;


    @Given("components {string} are running")
    public void components_are_running(final String componentList) {

        List<Components> components = Arrays.stream(componentList.split(",")).sequential()
                .map(String::trim)
                .map(Components::valueOf)
                .collect(Collectors.toList());

        List<Components> failedHealthChecks = components.stream()
                .filter(comp ->
                {
                    try {
                        return !healthCheck(comp);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        assertThat(failedHealthChecks).isEmpty();
    }

    private boolean healthCheck(Components comp) throws SQLException {

        Boolean isHealthy = false;

        switch (comp) {
            case DATABASE: {
                isHealthy = dataSource.getConnection() != null;
                break;
            }
            case KAFKA: {
                isHealthy = !kafkaTopicConsumer.listTopics().keySet().isEmpty();
                break;
            }
            default:
                isHealthy = false;
        }

        return isHealthy;
    }

    enum Components {
        DATABASE,
        KAFKA
    }
}
