package com.example.streams.integration;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.streams.AbstractIntegrationTest;
import com.example.streams.WordCountProcessor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;

/**
 * We’ve sent a POST to our REST controller, which, in turn, sends the message to the Kafka input
 * topic. As part of the setup, we’ve also started a Kafka consumer. This listens asynchronously to
 * the output Kafka topic and updates the BlockingQueue with the received word counts.
 * <p>
 * During the test execution, the application should process the input messages. Following on, we
 * can verify the expected output both from the topic as well as the state store using the REST
 * service.
 */
@Slf4j
public class KafkaStreamsIntegrationTest extends AbstractIntegrationTest {

  private final BlockingQueue<String> output = new LinkedBlockingQueue<>();

  private KafkaMessageListenerContainer<Integer, String> consumer;

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Value("${message.topic.name}")
  private String topicName;

  @BeforeEach
  public void setUp() {
    assertThat(KAFKA.isRunning()).isTrue();
    output.clear();
    createConsumer();
  }

  @Test
  void givenInputMessages_whenPostToEndpoint_thenWordCountsReceivedOnOutput() throws Exception {

    postMessage("test message");

    startOutputTopicConsumer();

    // assert correct counts from output topic
    assertThat(output.poll(2, MINUTES)).isEqualTo("test:1");
    assertThat(output.poll(2, MINUTES)).isEqualTo("message:1");

    // assert correct count from REST service
    assertThat(getCountFromRestServiceFor("test")).isEqualTo(1);
    assertThat(getCountFromRestServiceFor("message")).isEqualTo(1);

    postMessage("another test message");

    // assert correct counts from output topic
    assertThat(output.poll(2, MINUTES)).isEqualTo("another:1");
    assertThat(output.poll(2, MINUTES)).isEqualTo("test:2");
    assertThat(output.poll(2, MINUTES)).isEqualTo("message:2");

    // assert correct count from REST service
    assertThat(getCountFromRestServiceFor("another")).isEqualTo(1);
    assertThat(getCountFromRestServiceFor("test")).isEqualTo(2);
    assertThat(getCountFromRestServiceFor("message")).isEqualTo(2);
  }

  private void postMessage(String message) {
    HttpEntity<String> request = new HttpEntity<>(message, new HttpHeaders());
    restTemplate.postForEntity(createURLWithPort("/message"), request, null);
  }

  private int getCountFromRestServiceFor(String word) {
    HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());
    ResponseEntity<String> response = restTemplate.exchange(
        createURLWithPort("/count/" + word),
        HttpMethod.GET, entity, String.class
    );
    return Integer.parseInt(Objects.requireNonNull(response.getBody()));
  }

  private String createURLWithPort(String uri) {
    return "http://localhost:" + port + uri;
  }

  private void createConsumer() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "baeldung-" + UUID.randomUUID());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);

    // set up the consumer for the word count output
    DefaultKafkaConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<>(props);
    ContainerProperties containerProperties = new ContainerProperties(topicName);
    consumer = new KafkaMessageListenerContainer<>(cf, containerProperties);
    consumer.setBeanName("templateTests");

    consumer.setupMessageListener((MessageListener<String, Long>) record -> {
      log.info("Record received: {}", record);
      output.add(record.key() + ":" + record.value());
    });
  }

  private void startOutputTopicConsumer() {
    consumer.start();
  }
}