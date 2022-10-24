package com.example.order.bdd.datatypes;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

@Builder
@EqualsAndHashCode
@Getter
public class ConsumerOfGroup {

    @EqualsAndHashCode.Include
    private String name;

    @EqualsAndHashCode.Include
    private Integer partitionId;

    @EqualsAndHashCode.Include
    private String consumerGroup;

    @EqualsAndHashCode.Exclude
    @Setter
    private KafkaConsumer<Long, SpecificRecord> consumer;
}
