package ma.abdex.streams;

import ma.abdex.metrics.PrometheusMetricsServer;
import ma.abdex.model.StationStats;
import ma.abdex.model.WeatherData;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

/**
 * Kafka Streams processor for weather data analysis
 * Performs filtering, transformation, grouping and aggregation
 */
public class WeatherStreamsProcessor {
    private static final Logger logger = LoggerFactory.getLogger(WeatherStreamsProcessor.class);

    private static final String INPUT_TOPIC = "weather-data";
    private static final String OUTPUT_TOPIC = "station-averages";
    private static final double TEMPERATURE_THRESHOLD = 30.0; // °C

    private final KafkaStreams streams;

    public WeatherStreamsProcessor(String bootstrapServers, String applicationId) {
        Properties props = createStreamsConfig(bootstrapServers, applicationId);
        StreamsBuilder builder = new StreamsBuilder();
        buildTopology(builder);
        this.streams = new KafkaStreams(builder.build(), props);

        // Add shutdown hook for clean closure
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered - closing Kafka Streams");
            close();
        }));
    }

    /**
     * Configure Kafka Streams properties
     */
    private Properties createStreamsConfig(String bootstrapServers, String applicationId) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // Optimization settings
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        // Note: CACHE_MAX_BYTES_BUFFERING_CONFIG is deprecated in Kafka 3.0+
        // Use statestore.cache.max.bytes instead
        props.put(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 10 * 1024 * 1024L);

        return props;
    }

    /**
     * Build the Kafka Streams topology
     */
    private void buildTopology(StreamsBuilder builder) {
        // Step 1: Read data from 'weather-data' topic as KStream
        KStream<String, String> weatherStream = builder.stream(
                INPUT_TOPIC,
                Consumed.with(Serdes.String(), Serdes.String()));

        // Step 2: Parse CSV and filter temperatures > 30°C
        KStream<String, WeatherData> filteredStream = weatherStream
                .mapValues(value -> {
                    try {
                        return WeatherData.fromCsv(value);
                    } catch (Exception e) {
                        logger.error("Error parsing weather data: {}", value, e);
                        return null;
                    }
                })
                .filter((key, value) -> value != null)
                .peek((key, value) -> {
                    logger.debug("Received: {}", value);
                    // Record consumed message
                    PrometheusMetricsServer.recordKafkaConsumed(INPUT_TOPIC);
                })
                .filter((key, value) -> {
                    boolean isHigh = value.getTemperature() > TEMPERATURE_THRESHOLD;
                    if (isHigh) {
                        logger.info("High temperature detected: {}", value);
                    }
                    return isHigh;
                });

        // Step 3: Group by station and calculate averages
        KGroupedStream<String, WeatherData> groupedStream = filteredStream
                .groupBy(
                        (key, value) -> value.getStation(),
                        Grouped.with(Serdes.String(), Serdes.serdeFrom(
                                new WeatherDataSerializer(),
                                new WeatherDataDeserializer())));

        // Aggregate to calculate averages
        KTable<String, StationStats> aggregatedTable = groupedStream
                .aggregate(
                        // Initializer - create empty stats
                        () -> new StationStats("", 0.0, 0.0, 0),

                        // Aggregator - accumulate values
                        (station, newData, aggregate) -> {
                            int newCount = aggregate.getCount() + 1;

                            // Calculate running averages in Celsius
                            double newAvgTemp = ((aggregate.getAvgTemperatureCelsius() * aggregate.getCount())
                                    + newData.getTemperature()) / newCount;

                            double newAvgHumidity = ((aggregate.getAvgHumidity() * aggregate.getCount())
                                    + newData.getHumidity()) / newCount;

                            StationStats updated = new StationStats(
                                    station,
                                    newAvgTemp,
                                    newAvgHumidity,
                                    newCount);

                            logger.info("Updated aggregation: {}", updated);

                            // Record Prometheus metrics for aggregation
                            PrometheusMetricsServer.recordAggregatedStats(station, newAvgTemp, newAvgHumidity,
                                    newCount);

                            return updated;
                        },

                        // Materialized with custom serdes
                        Materialized.with(
                                Serdes.String(),
                                Serdes.serdeFrom(new StationStatsSerializer(), new StationStatsDeserializer())));

        // Step 5: Write results to output topic
        aggregatedTable
                .toStream()
                .peek((key, value) -> logger.info("Publishing to {}: {}", OUTPUT_TOPIC, value))
                .mapValues(StationStats::toString)
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));

        // Also print to console for debugging
        aggregatedTable
                .toStream()
                .foreach((key, value) -> System.out.println(value));
    }

    /**
     * Start the Kafka Streams application
     */
    public void start() {
        logger.info("Starting Kafka Streams processor");
        streams.start();
        logger.info("Kafka Streams processor started successfully");
    }

    /**
     * Close the Kafka Streams application cleanly
     */
    public void close() {
        logger.info("Closing Kafka Streams processor");
        streams.close(Duration.ofSeconds(10));
        logger.info("Kafka Streams processor closed");
    }

    /**
     * Get the Kafka Streams instance (useful for monitoring)
     */
    public KafkaStreams getStreams() {
        return streams;
    }
}
