package com.example.streams;

import java.util.Arrays;
import lombok.Setter;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WordCountProcessor {

  private static final Serde<String> STRING_SERDE = Serdes.String();

  @Value("${message.topic.name}")
  @Setter // for tests
  private String topicName;

  @Autowired
  public void buildPipeline(StreamsBuilder streamsBuilder) {

    // create a KStream from the input topic using the specified key and value SerDes
    KStream<String, String> messageStream = streamsBuilder.stream(
        "input-topic",
        Consumed.with(STRING_SERDE, STRING_SERDE)
    );

    // create a KTable by transforming, splitting, grouping, and then counting the data
    KTable<String, Long> wordCounts = messageStream
        .mapValues((ValueMapper<String, String>) String::toLowerCase)
        .flatMapValues(value -> Arrays.asList(value.split("\\W+")))
        .groupBy((key, word) -> word, Grouped.with(STRING_SERDE, STRING_SERDE))
        // materialize the result to an output stream
        .count(Materialized.as("counts"));

    wordCounts.toStream().to(topicName);
  }
}