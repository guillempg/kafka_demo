package com.example.order.configuration;

import io.confluent.kafka.serializers.*;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka")
@EnableAutoConfiguration
public class KafkaConfiguration {

    public static final String ORDERS_TOPIC = "orders";
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value(value = "${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value(value = "${spring.kafka.producer.acks}")
    private String acks;

    @Value("${schema.registry.url}")
    private String schemaRegistryUrl;

    @Autowired
    private ApplicationContext context;


    @Bean
    public KafkaAdmin adminClient() {
        return new KafkaAdmin(adminClientConfigProperties("admin"));
    }

    @Bean
    public KafkaProducer<Long, SpecificRecord> kafkaProducer() {

        return new KafkaProducer<>(producerConfigProperties("producer"));
    }

    @Bean
    public KafkaConsumer<Long, SpecificRecord> kafkaConsumer() {
        return new KafkaConsumer<>(consumerConfigProperties("consumer"));
    }

    @Bean
    public KafkaConsumer<String, List<PartitionInfo>> kafkaHealthCheckConsumer() {
        return new KafkaConsumer<>(consumerConfigProperties("healthcheck-consumer"));
    }

    @Bean
    public NewTopic ordersTopic() {
        return new NewTopic(ORDERS_TOPIC, 2, (short) 1);
    }


    private Map<String, Object> producerConfigProperties(String clientId) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        configProps.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        configProps.put(KafkaAvroDeserializerConfig.AUTO_REGISTER_SCHEMAS, true);
        configProps.put(KafkaAvroDeserializerConfig.VALUE_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class.getName());
        return configProps;
    }

    private Map<String, Object> consumerConfigProperties(String clientId) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        configProps.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        configProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        return configProps;
    }

    private Map<String, Object> adminClientConfigProperties(String clientId) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(AdminClientConfig.CLIENT_ID_CONFIG, clientId);
        configProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return configProps;
    }

    public void createTopic(final String topicName, Integer partitionNumber) {
        NewTopic topic = new NewTopic(topicName, partitionNumber, (short) 1);
        adminClient().createOrModifyTopics(topic);
    }
}
