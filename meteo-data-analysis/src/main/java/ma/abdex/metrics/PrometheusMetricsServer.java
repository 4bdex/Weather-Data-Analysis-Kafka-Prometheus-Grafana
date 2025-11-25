package ma.abdex.metrics;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Prometheus server that exposes weather metrics
 * Starts an HTTP server on port 8080 to expose /metrics endpoint
 */
public class PrometheusMetricsServer {
    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetricsServer.class);

    private HTTPServer server;
    private final int port;

    // Prometheus Metrics

    // Counter: Total number of measurements received per station
    public static final Counter weatherDataReceived = Counter.build()
            .name("weather_data_received_total")
            .help("Total number of weather data received")
            .labelNames("station")
            .register();

    // Counter: Number of high temperatures detected (> 30°C)
    public static final Counter highTemperatureDetected = Counter.build()
            .name("weather_high_temperature_detected_total")
            .help("Number of high temperatures detected (> 30°C)")
            .labelNames("station")
            .register();

    // Gauge: Current temperature in Celsius per station
    public static final Gauge currentTemperatureCelsius = Gauge.build()
            .name("weather_current_temperature_celsius")
            .help("Current temperature in degrees Celsius")
            .labelNames("station")
            .register();

    // Gauge: Current humidity per station
    public static final Gauge currentHumidity = Gauge.build()
            .name("weather_current_humidity_percent")
            .help("Current humidity rate in percentage")
            .labelNames("station")
            .register();

    // Gauge: Average aggregated temperature in Celsius per station
    public static final Gauge avgTemperatureCelsius = Gauge.build()
            .name("weather_avg_temperature_celsius")
            .help("Average aggregated temperature in Celsius")
            .labelNames("station")
            .register();

    // Gauge: Average aggregated humidity per station
    public static final Gauge avgHumidity = Gauge.build()
            .name("weather_avg_humidity_percent")
            .help("Average aggregated humidity in percentage")
            .labelNames("station")
            .register();

    // Gauge: Number of measurements in aggregation per station
    public static final Gauge aggregationCount = Gauge.build()
            .name("weather_aggregation_count")
            .help("Number of measurements in aggregation")
            .labelNames("station")
            .register();

    // Histogram: Temperature distribution
    public static final Histogram temperatureDistribution = Histogram.build()
            .name("weather_temperature_distribution_celsius")
            .help("Temperature distribution in Celsius")
            .buckets(0, 10, 20, 30, 35, 40, 45, 50)
            .labelNames("station")
            .register();

    // Histogram: Humidity distribution
    public static final Histogram humidityDistribution = Histogram.build()
            .name("weather_humidity_distribution_percent")
            .help("Humidity distribution in percentage")
            .buckets(0, 20, 40, 60, 80, 100)
            .labelNames("station")
            .register();

    // Counter: Messages produits dans Kafka
    public static final Counter kafkaMessagesProduced = Counter.build()
            .name("kafka_messages_produced_total")
            .help("Nombre total de messages produits dans Kafka")
            .labelNames("topic", "station")
            .register();

    // Counter: Messages consommés depuis Kafka
    public static final Counter kafkaMessagesConsumed = Counter.build()
            .name("kafka_messages_consumed_total")
            .help("Nombre total de messages consommés depuis Kafka")
            .labelNames("topic")
            .register();

    public PrometheusMetricsServer(int port) {
        this.port = port;
    }

    /**
     * Start the HTTP server to expose metrics
     */
    public void start() throws IOException {
        server = new HTTPServer(port);
        logger.info("Prometheus metrics server started on port {} (endpoint: http://localhost:{}/metrics)", port, port);
    }

    /**
     * Stop the HTTP server
     */
    public void stop() {
        if (server != null) {
            try {
                server.close();
                logger.info("Prometheus metrics server stopped");
            } catch (Exception e) {
                logger.error("Error stopping Prometheus server", e);
            }
        }
    }

    /**
     * Record new weather data
     */
    public static void recordWeatherData(String station, double temperatureCelsius, double humidity) {
        // Increment counter
        weatherDataReceived.labels(station).inc();

        // Update gauges
        currentTemperatureCelsius.labels(station).set(temperatureCelsius);
        currentHumidity.labels(station).set(humidity);

        // Record in histograms
        temperatureDistribution.labels(station).observe(temperatureCelsius);
        humidityDistribution.labels(station).observe(humidity);

        // If high temperature
        if (temperatureCelsius > 30.0) {
            highTemperatureDetected.labels(station).inc();
        }
    }

    /**
     * Record aggregated statistics
     */
    public static void recordAggregatedStats(String station, double avgTempCelsius, double avgHum, int count) {
        avgTemperatureCelsius.labels(station).set(avgTempCelsius);
        avgHumidity.labels(station).set(avgHum);
        aggregationCount.labels(station).set(count);
    }

    /**
     * Record a Kafka message produced
     */
    public static void recordKafkaProduced(String topic, String station) {
        kafkaMessagesProduced.labels(topic, station).inc();
    }

    /**
     * Record a Kafka message consumed
     */
    public static void recordKafkaConsumed(String topic) {
        kafkaMessagesConsumed.labels(topic).inc();
    }
}
