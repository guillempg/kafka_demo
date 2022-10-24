package com.example.billing.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumer {

    @KafkaListener(topics = "orders")
    public void kafkaMessageListener(ConsumerRecord<Long, SpecificRecord> record) {
        System.out.println("Kafka message listener got a new record: " + record);
    }

}
