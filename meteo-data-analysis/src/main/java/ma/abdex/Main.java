package ma.abdex;

import ma.abdex.metrics.PrometheusMetricsServer;
import ma.abdex.producer.WeatherDataGenerator;
import ma.abdex.streams.WeatherStreamsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Main application for Weather Data Analysis using Kafka Streams
 * 
 * This application:
 * 1. Generates time-series weather data from multiple stations
 * 2. Filters data where temperature > 30°C
 * 3. Aggregates data by station (average temperature and humidity)
 * 4. Outputs results to console and Kafka topic
 * 5. Exposes Prometheus metrics on port 8080
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String APPLICATION_ID = "weather-analysis-app";
    private static final int PROMETHEUS_PORT = 8080;

    public static void main(String[] args) {
        logger.info("=== Weather Data Analysis Application ===");
        logger.info("Bootstrap Servers: {}", BOOTSTRAP_SERVERS);
        logger.info("Prometheus Metrics: http://localhost:{}/metrics", PROMETHEUS_PORT);

        // Initialize components
        PrometheusMetricsServer metricsServer = null;
        WeatherStreamsProcessor processor = null;
        WeatherDataGenerator generator = null;

        try {
            // Start Prometheus metrics server
            logger.info("\nStarting Prometheus Metrics Server...");
            metricsServer = new PrometheusMetricsServer(PROMETHEUS_PORT);
            metricsServer.start();

            // Start Kafka Streams processor
            logger.info("\nStarting Kafka Streams Processor...");
            processor = new WeatherStreamsProcessor(BOOTSTRAP_SERVERS, APPLICATION_ID);
            processor.start();

            // Wait a bit for streams to initialize
            Thread.sleep(2000);

            // Start weather data generator
            logger.info("\nStarting Weather Data Generator...");
            generator = new WeatherDataGenerator(BOOTSTRAP_SERVERS);

            // Generate initial batch of data
            logger.info("\nGenerating initial data batch...");
            generator.generateBatch(3);

            // Start continuous generation every 5 seconds
            logger.info("\nStarting continuous data generation (every 5 seconds)...");
            generator.startGenerating(5);

            logger.info("\nApplication is running!");
            logger.info("Watch the console for aggregated results.");
            logger.info("");
            logger.info("Prometheus metrics: http://localhost:{}/metrics", PROMETHEUS_PORT);
            logger.info("Grafana dashboard: http://localhost:3000 (admin/admin)");
            logger.info("");
            logger.info("Press ENTER to stop the application\n");

            // Wait for user input to stop
            try (Scanner scanner = new Scanner(System.in)) {
                scanner.nextLine();
            }

            logger.info("\nStopping application...");

        } catch (Exception e) {
            logger.error("Error running application", e);
        } finally {
            // Clean shutdown
            if (generator != null) {
                generator.stop();
            }
            if (processor != null) {
                processor.close();
            }
            if (metricsServer != null) {
                metricsServer.stop();
            }
            logger.info("Application stopped successfully");
        }
    }
}