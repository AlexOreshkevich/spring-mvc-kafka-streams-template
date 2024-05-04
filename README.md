# spring-webflux-kafka-streams-template

Template project for Spring Boot and Kafka Streams with Reactive REST API (WebFlux)

## Dependencies

- Lombok (Java annotation library which helps to reduce boilerplate code)
- Spring for Apache Kafka (Publish, subscribe, store, and process streams of records)
- Spring for Apache Kafka Streams (Building stream processing applications with Apache Kafka
  Streams)
- Spring Web (Build web applications with Spring MVC)
- Spring Boot Actuator (Supports built in (or custom) endpoints that let you monitor and manage your
  application - such as application health, metrics, sessions, etc.)
- Testcontainers (Provide lightweight, throwaway instances of common databases, Selenium web
  browsers, or anything else that can run in a Docker container)

## Sample application

Our sample application reads streaming events from an input Kafka topic. Once the records
are read, it processes them to split the text and counts the individual words.
Subsequently, it sends the updated word count to the Kafka output. In addition to the
output topic, we’ll also create a simple REST service to expose this count over an HTTP
endpoint.

Overall, the output topic will be continuously updated with the words extracted from the
input events and their updated counts.

Based on https://www.baeldung.com/spring-boot-kafka-streams.

## Improvements Changelog

We used Spring Initializr (https://start.spring.io/) for creating stub for this project.
After generation, several improvements were made, to make it more production-ready and
in order to improve developer experience:

- Maven build replaced with Gradle on Kotlin DSL
- Dependency on Lombok replaced
  with [Lombok Gradle Plugin](https://plugins.gradle.org/plugin/io.freefair.lombok)
- `@SpringBootTest` replaced with `@WebFluxTest` for integration test (which disables full
  auto-configuration and only applies configuration relevant to WebFlux tests) which improves
  performance
- `confluentinc/cp-kafka` upgraded to `7.6.1` (to support `aarch64`)
- `io.spring.dependency-management` plugin replaced with Gradle `platform` plugin
- [Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html) with explicit
  separation of component, integration and functional tests
- Gradle parallel build turned on (`gradle.properties`)