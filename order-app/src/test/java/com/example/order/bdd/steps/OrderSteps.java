package com.example.order.bdd.steps;

import com.example.order.bdd.datatypes.*;
import com.example.order.configuration.KafkaConfiguration;
import com.example.order.domain.Order;
import com.example.order.dto.PlaceOrderCommand;
import com.example.order.repository.OrderRepository;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.StreamSupport;

import static com.example.order.bdd.datatypes.Mappers.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@Slf4j
public class OrderSteps {

    @Autowired
    KafkaConsumer<String, List<PartitionInfo>> kafkaTopicReader;

    @Autowired
    private KafkaConfiguration kafkaConfiguration;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    private WebTestClient webClient;


    private final Map<String, Order> placeOrderResponsesByTrackingId = new HashMap<>();
    private final Map<Long, Order> lastBatchIdToPlaceOrderResponse = new HashMap<>();

    private final List<ConsumerOfGroup> kafkaConsumers = new ArrayList<>();

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value(value = "${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;
    @Value(value = "${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;
    @Value(value = "${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value(value = "${spring.kafka.producer.acks}")
    private String acks;
    @Value("${schema.registry.url:mock://test}")
    private String schemaRegistryUrl;


//    private int NUMBER_OF_BROKERS = 1;
//    private boolean CONTROLLER_SHUTDOWN = true;
//    private int NUMBER_OF_PARTITIONS = 2;
//    @Autowired
//    private EmbeddedKafkaBroker embeddedKafkaBroker = new EmbeddedKafkaBroker(NUMBER_OF_BROKERS, CONTROLLER_SHUTDOWN, NUMBER_OF_PARTITIONS);


    private KafkaConsumer<Long, SpecificRecord> kafkaConsumer(String clientId) {
//        Map<String, Object> configProps = KafkaTestUtils.consumerProps("OrderGroup", "false", embeddedKafkaBroker);
        Map<String, Object> configProps = new HashMap<>();

        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        configProps.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        configProps.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        configProps.put(AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        configProps.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        return new KafkaConsumer<>(configProps);
    }

    @Given("kafka topic {string} with {int} partition(s) exists")
    public void kafka_topic_exists(String topicName, Integer partitionNumber) {

        if (!kafkaTopicReader.listTopics().keySet().contains(topicName)) {
            kafkaConfiguration.createTopic(topicName, partitionNumber);
        }
        assertThat(kafkaTopicReader.listTopics().keySet()).contains(topicName);
    }

    @Then("the response of request with tracking id {string} is:")
    public void responseMatches(final String trackingId, final Order expected) {
        Order actual = placeOrderResponsesByTrackingId.get(trackingId);
        assertThat(actual).isNotNull();
        assertThat(actual.getSymbol()).isEqualTo(expected.getSymbol());
        assertThat(actual.getQuantity()).isEqualTo(expected.getQuantity());
        assertThat(actual.getOwner()).isEqualTo(expected.getOwner());
        assertThat(actual.getOrderType()).isEqualTo(expected.getOrderType());
    }

    @When("the following order is placed through REST with tracking id {string}:")
    public void the_following_order_is_placed_through_rest(final String trackingId, final PlaceOrderCommand command) throws InterruptedException {

        FluxExchangeResult<Order> result = webClient.post()
                .uri("/orders/place")
                .body(Mono.just(command), PlaceOrderCommand.class)
                .exchange()
                .returnResult(Order.class);

        Order placedOrder = result.getResponseBody().blockFirst();
        placeOrderResponsesByTrackingId.put(trackingId, placedOrder);
    }

    @When("the following orders are successfully placed through REST:")
    public void the_following_order_is_placed_through_rest(final List<PlaceOrderCommand> commands) {

        commands.forEach(command -> {
            FluxExchangeResult<Order> placeOrderResponseFluxExchangeResult = webClient.post()
                    .uri("/orders/place")
                    .body(Mono.just(command), PlaceOrderCommand.class)
                    .exchange()
                    .returnResult(Order.class);

            Order placedOrder = placeOrderResponseFluxExchangeResult.getResponseBody().blockFirst();
            lastBatchIdToPlaceOrderResponse.put(placedOrder.getId(), placedOrder);
        });
    }

    @Then("the order corresponding to tracking id {string} is in the database:")
    public void the_order_corresponding_to_tacking_id_is_in_the_database(String trackingId, final Order expectedOrder) {

        // set the Order @Id from the response
        expectedOrder.setId(placeOrderResponsesByTrackingId.get(trackingId).getId());

        Optional<Order> orderInDatabase = orderRepository.findById(expectedOrder.getId());
        assertThat(orderInDatabase).isPresent();
        assertThat(orderInDatabase.get().getQuantity()).isEqualTo(expectedOrder.getQuantity());
        assertThat(orderInDatabase.get().getSymbol()).isEqualTo(expectedOrder.getSymbol());
        assertThat(orderInDatabase.get().getOwner()).isEqualTo(expectedOrder.getOwner());
        assertThat(orderInDatabase.get().getSide()).isEqualTo(expectedOrder.getSide());
    }

    @Then("all previous orders are in the database")
    public void these_orders_are_in_the_database() {

        lastBatchIdToPlaceOrderResponse.keySet().forEach(returnedOrderId -> {
            Order order = orderRepository.findById(returnedOrderId)
                    .orElseThrow();

            Order expectedOrder = lastBatchIdToPlaceOrderResponse.get(returnedOrderId);

            assertThat(order.getSymbol()).isEqualTo(expectedOrder.getSymbol());
            assertThat(order.getQuantity()).isEqualTo(expectedOrder.getQuantity());
        });
    }

    @Then("the following event corresponding to tracking id {string} is published in kafka topic {string}:")
    public void the_following_event_is_published_in_kafka_topic(final String trackingId,
                                                                final String topic,
                                                                final OrderPlaced orderPlaced) {
        // set the Order @Id from the response
        orderPlaced.setOrderId(placeOrderResponsesByTrackingId.get(trackingId).getId());

        ProducerRecord<Long, SpecificRecord> record = new ProducerRecord<>(topic, orderPlaced.getOrderId(), map(orderPlaced));
        Future<RecordMetadata> recordMetadataFuture = kafkaConfiguration.kafkaProducer().send(record);
        try {
            RecordMetadata recordMetadata = recordMetadataFuture.get();
            log.info("record timestamp:" + recordMetadata.timestamp());
        } catch (Exception e) {
            fail("Exception thrown:" + e.getMessage());
        }
    }

    @Then("the following order placed events are published in kafka topic {string}:")
    public void the_following_events_are_published_in_kafka_topic(final String topic,
                                                                  final List<OrderPlaced> ordersPlaced) {
        ordersPlaced.forEach(orderPlaced -> {

            int partitionId;
            if (orderPlaced.getOrderId() == null) {
                partitionId = orderRepository.findAll().stream()
                        .filter(order -> order.getSymbol().equals(orderPlaced.getSymbol()))
                        .filter(order -> order.getQuantity().equals(orderPlaced.getQuantity()))
                        .findFirst()
                        .orElseThrow()
                        .getId().intValue() & 0x1;
            } else {
                partitionId = orderPlaced.getOrderId().intValue() & 0x1;
            }

            ProducerRecord<Long, SpecificRecord> record = new ProducerRecord<>(
                    topic,
                    partitionId,
                    orderPlaced.getOrderId(),
                    map(orderPlaced));
            record.headers().add(new RecordHeader("type", "OrderPlaced".getBytes()));

            Future<RecordMetadata> recordMetadataFuture = kafkaConfiguration.kafkaProducer().send(record);
            try {
                RecordMetadata recordMetadata = recordMetadataFuture.get();
                log.info("record timestamp:" + recordMetadata.timestamp());
            } catch (Exception e) {
                fail("Exception thrown:" + e.getMessage());
            }
        });
    }

    @When("the following order cancelled events are published in kafka topic {string}:")
    public void the_following_order_cancelled_events_are_published_in_kafka_topic(String topic, List<OrderCancelled> ordersCancelled) {
        ordersCancelled.forEach(orderCancelled -> {

            int partitionId = orderCancelled.getOrderId().intValue() & 0x1;

            ProducerRecord<Long, SpecificRecord> record = new ProducerRecord<>(
                    topic,
                    partitionId,
                    orderCancelled.getOrderId(),
                    map(orderCancelled));

            record.headers().add(new RecordHeader("type", "OrderCancelled".getBytes()));

            Future<RecordMetadata> recordMetadataFuture = kafkaConfiguration.kafkaProducer().send(record);
            try {
                RecordMetadata recordMetadata = recordMetadataFuture.get();
                log.info("record timestamp:" + recordMetadata.timestamp());
            } catch (Exception e) {
                fail("Exception thrown:" + e.getMessage());
            }
        });
    }

    @When("the following order filled events are published in kafka topic {string}:")
    public void the_following_order_filled_events_are_published_in_kafka_topic(String topic, List<OrderFilled> fills) {
        fills.forEach(fill -> {

            int partitionId = fill.getOrderId().intValue() & 0x1;

            var orderFilled = new com.example.order.kafka.OrderFilled();
            orderFilled.setOrderId(String.valueOf(fill.getOrderId()));
            orderFilled.setFilledQuantity(fill.getQuantityFilled());
            orderFilled.setCounterparty(fill.getCounterparty());

            ProducerRecord<Long, SpecificRecord> record = new ProducerRecord<>(
                    topic,
                    partitionId,
                    fill.getOrderId(),
                    orderFilled);

            Future<RecordMetadata> recordMetadataFuture = kafkaConfiguration.kafkaProducer().send(record);
            try {
                RecordMetadata recordMetadata = recordMetadataFuture.get();
                log.info("record timestamp:" + recordMetadata.timestamp());
            } catch (Exception e) {
                fail("Exception thrown:" + e.getMessage());
            }
        });
    }

    @Then("the following event is published in kafka topic {string}:")
    public void the_following_event_is_published_in_kafka_topic(final String topic,
                                                                final OrderPlaced orderPlaced) {

        ProducerRecord<Long, SpecificRecord> record = new ProducerRecord<>(
                topic,
                orderPlaced.getOrderId(),
                map(orderPlaced));
        Future<RecordMetadata> recordMetadataFuture = kafkaConfiguration.kafkaProducer().send(record);
        try {
            RecordMetadata recordMetadata = recordMetadataFuture.get();
            log.info("record timestamp:" + recordMetadata.timestamp());
        } catch (Exception e) {
            fail("Exception thrown:" + e.getMessage());
        }
    }

    @And("the following event is consumed from kafka topic {string}:")
    public void the_following_event_is_consumed_in_kafka_topic(final String topic,
                                                               final String expectedOrderPlaced) {

        //kafkaConfiguration.kafkaConsumer().subscribe(List.of(topic));
        KafkaConsumer<Long, SpecificRecord> kafkaConsumer = kafkaConsumer(UUID.randomUUID().toString());
        kafkaConsumer.subscribe(List.of(topic));
        ConsumerRecords<Long, SpecificRecord> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(5000L));
        assertThat(consumerRecords.count()).isPositive();
        assertThat(
                StreamSupport
                        .stream(consumerRecords.records(topic).spliterator(), false)
                        .map(ConsumerRecord::value)
                        .map(Object::toString))
                .contains(expectedOrderPlaced);
    }

    @Given("the following consumers from the same consumer group subscribe to topic {string}:")
    public void the_following_consumers_from_the_same_consumer_group_subscribe_to_topic(String topic, List<ConsumerOfGroup> consumers) {

        consumers.forEach(consumer -> {
            KafkaConsumer<Long, SpecificRecord> c = kafkaConsumer(consumer.getName());
            c.assign(Collections.singleton(new TopicPartition(topic, consumer.getPartitionId())));
            consumer.setConsumer(c);
            kafkaConsumers.add(consumer);
        });
    }

    @Then("consumer {string} of kafka topic {string} consumes OrderPlaced messages:")
    public void sees_order_placed_from_kafka_topic(String consumerName, String topic, List<OrderPlaced> expectedOrderPlaced) {

        ConsumerOfGroup consumerOfGroup = kafkaConsumers.stream()
                .filter(c -> c.getName().equals(consumerName))
                .findFirst()
                .orElseThrow();

        ConsumerRecords<Long, SpecificRecord> consumerRecords = consumerOfGroup.getConsumer().poll(Duration.ofMillis(5000L));
        assertThat(consumerRecords.count()).isPositive();

        expectedOrderPlaced.forEach(expectedMessage ->
                assertThat(StreamSupport.stream(consumerRecords.records(topic).spliterator(), false)
                        .filter(record -> StreamSupport.stream(record.headers().headers("type").spliterator(), false)
                                .anyMatch(h -> Arrays.equals(h.value(), "OrderPlaced".getBytes())))
                        .filter(record -> record.key().equals(expectedMessage.getOrderId()))
                        .filter(record -> ((com.example.order.kafka.OrderPlaced) record.value()).getQuantity() == expectedMessage.getQuantity())
                        .filter(record -> ((com.example.order.kafka.OrderPlaced) record.value()).getSide().equals(com.example.order.kafka.Side.valueOf(expectedMessage.getSide().name())))
                        .filter(record -> ((com.example.order.kafka.OrderPlaced) record.value()).getSymbol().equals(expectedMessage.getSymbol()))
                        .findFirst()).isPresent());
    }

    @Then("consumer {string} of kafka topic {string} consumes messages:")
    public void sees_order_event_from_kafka_topic(String consumerName, String topic, List<ExpectedOrderEvent> expectedOrderEvents) throws Exception {

        ConsumerOfGroup consumerOfGroup = kafkaConsumers.stream()
                .filter(c -> c.getName().equals(consumerName))
                .findFirst()
                .orElseThrow();

        ConsumerRecords<Long, SpecificRecord> consumerRecords = consumerOfGroup.getConsumer().poll(Duration.ofMillis(5000L));
        assertThat(consumerRecords.count()).isPositive();

        expectedOrderEvents.forEach(expectedMessage -> check(expectedMessage, consumerRecords, topic));
    }

    private void check(ExpectedOrderEvent expectedMessage, ConsumerRecords<Long, SpecificRecord> consumerRecords, String topic) {
        switch (expectedMessage.getEventType()) {
            case PLACED:
                assertThat(StreamSupport.stream(consumerRecords.records(topic).spliterator(), false)
                        .filter(record -> record.value() instanceof com.example.order.kafka.OrderPlaced)
                        .filter(record -> record.key().equals(expectedMessage.getOrderId()))
                        .findFirst())
                        .isPresent();
                break;
            case CANCELED:
                assertThat(StreamSupport.stream(consumerRecords.records(topic).spliterator(), false)
                        .filter(record -> record.value() instanceof com.example.order.kafka.OrderCanceled)
                        .filter(record -> record.key().equals(expectedMessage.getOrderId()))
                        .findFirst())
                        .isPresent();
                break;
            case FILLED:
                assertThat(StreamSupport.stream(consumerRecords.records(topic).spliterator(), false)
                        .filter(record -> record.value() instanceof com.example.order.kafka.OrderFilled)
                        .filter(record -> record.key().equals(expectedMessage.getOrderId()))
                        .findFirst())
                        .isPresent();
                break;
        }
    }

    @Then("consumer {string} of kafka topic {string} consumes OrderCancelled messages:")
    public void sees_order_cancelled_from_kafka_topic(String consumerName, String topic, List<OrderCancelled> expectedOrderCancelled) {

        ConsumerOfGroup consumerOfGroup = kafkaConsumers.stream()
                .filter(c -> c.getName().equals(consumerName))
                .findFirst()
                .orElseThrow();

        ConsumerRecords<Long, SpecificRecord> consumerRecords = consumerOfGroup.getConsumer().poll(Duration.ofMillis(5000L));
        assertThat(consumerRecords.count()).isPositive();

        expectedOrderCancelled.forEach(expectedMessage ->
                assertThat(StreamSupport.stream(consumerRecords.records(topic).spliterator(), false)
                        .filter(record -> record.key().equals(expectedMessage.getOrderId()))
                        .findFirst()).isPresent());
    }

    @After
    public void clearKafkaConsumers() {
        kafkaConsumers.forEach(c ->
                c.getConsumer().unsubscribe()
        );
    }
}
