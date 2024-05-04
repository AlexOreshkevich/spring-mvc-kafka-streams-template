package com.example.streams.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.streams.WordCountProcessor;
import java.util.Properties;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

public class WordCountProcessorUnitTest {

  private WordCountProcessor wordCountProcessor;

  @BeforeEach
  void setUp() {
    wordCountProcessor = new WordCountProcessor();
    wordCountProcessor.setTopicName("output-topic");
  }

  @Test
  void givenInputMessages_whenProcessed_thenWordCountIsProduced() {
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    wordCountProcessor.buildPipeline(streamsBuilder);

    // encapsulates our business logic under test from the WordCountProcessor
    Topology topology = streamsBuilder.build();

    // create the input and output topics for our testing
    // this eliminates the need to have a broker running and
    // still verify the pipeline behavior
    try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology,
        new Properties())) {

      TestInputTopic<String, String> inputTopic = topologyTestDriver.createInputTopic(
          "input-topic",
          new StringSerializer(),
          new StringSerializer()
      );

      TestOutputTopic<String, Long> outputTopic = topologyTestDriver.createOutputTopic(
          "output-topic",
          new StringDeserializer(),
          new LongDeserializer()
      );

      inputTopic.pipeInput("key", "hello world");
      inputTopic.pipeInput("key2", "hello");

      assertThat(outputTopic.readKeyValuesToList()).containsExactly(
          KeyValue.pair("hello", 1L),
          KeyValue.pair("world", 1L),
          KeyValue.pair("hello", 2L)
      );
    }
  }
}