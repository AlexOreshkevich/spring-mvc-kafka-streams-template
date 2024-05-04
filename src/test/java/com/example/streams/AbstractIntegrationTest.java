package com.example.streams;

import java.io.File;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base integration test. Provides underlying Kafka container management to let inheritors focus on
 * business logic.
 *
 * @author Aliaksandr Arashkevich
 */
@SpringBootTest(
    classes = KafkaStreamsTemplateApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT
)
@Testcontainers
public abstract class AbstractIntegrationTest {

  @TempDir
  private static File tempDir;

  @Autowired
  KafkaTemplate<String, String> kafkaTemplate;

  @Container
  public static final KafkaContainer KAFKA = new KafkaContainer(
      DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
  );

  @DynamicPropertySource
  static void registerKafkaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
    registry.add("spring.kafka.streams.state.dir", tempDir::getAbsolutePath);
  }
}