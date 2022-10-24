package com.example.order.service;

import com.example.order.configuration.KafkaConfiguration;
import com.example.order.domain.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.commons.codec.digest.MurmurHash2;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import static com.example.order.configuration.KafkaConfiguration.ORDERS_TOPIC;
import static com.example.order.service.Converters.createOrderCanceledEvent;
import static com.example.order.service.Converters.createOrderPlacedEvent;

@Service
@Slf4j
public class KafkaService {

    private final KafkaConfiguration kafkaConfiguration;

    public KafkaService(KafkaConfiguration kafkaConfiguration) {
        this.kafkaConfiguration = kafkaConfiguration;
    }

    public void sendOrderPlaced(final Order order) {
        KafkaProducer<Long, SpecificRecord> kafkaProducer = kafkaConfiguration.kafkaProducer();
        int partition = partitionNum(order.getId());
        log.info("Sending place event:" + order + " to partition:" + partition);

        ProducerRecord<Long, SpecificRecord> record = new ProducerRecord<>(
                ORDERS_TOPIC,
                partition,
                order.getId(),
                createOrderPlacedEvent(order));

        kafkaProducer.send(record);

    }

    public void sendOrderCanceled(final Long orderId, final String user) {
        KafkaProducer<Long, SpecificRecord> kafkaProducer = kafkaConfiguration.kafkaProducer();
        int partition = partitionNum(orderId);
        log.info("Sending cancel event: id=" + orderId + " to partition:" + partition);
        ProducerRecord<Long, SpecificRecord> record = new ProducerRecord<>(
                ORDERS_TOPIC,
                orderId,
                createOrderCanceledEvent(orderId, user)); // todo better use CancelCommand?

        kafkaProducer.send(record);
    }

    // for demo purposes, this is kafka's default algo
    private int partitionNum(final long orderId) {
        return Math.abs(MurmurHash2.hash32(String.valueOf(orderId))) % 2;
    }
}
