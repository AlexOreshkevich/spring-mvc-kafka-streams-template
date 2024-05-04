package com.example.streams;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Integration test that creates full Spring Boot context and makes sure that all beans were
 * properly initialized.
 *
 * @author Aliaksandr Arashkevich
 */
class ComponentTest extends AbstractIntegrationTest {

  @Autowired
  ApplicationContext context;

  @Test
  void shouldLoadApplicationContext() {

    // make sure that context itself was properly initialized
    assertThat(context).isNotNull();

    // make sure that all necessary beans were initialized as well
    // feel free to extend checks that are going below
    assertThat(context.getBean(KafkaProducer.class)).isNotNull();
  }
}