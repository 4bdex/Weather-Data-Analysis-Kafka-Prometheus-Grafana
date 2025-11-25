package ma.abdex.producer;

import ma.abdex.metrics.PrometheusMetricsServer;
import ma.abdex.model.WeatherData;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Generator for weather data time series
 * Produces data to Kafka topic 'weather-data'
 */
public class WeatherDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(WeatherDataGenerator.class);

    private static final String TOPIC = "weather-data";
    private static final String[] STATIONS = { "Station1", "Station2", "Station3", "Station4", "Station5" };

    private final KafkaProducer<String, String> producer;
    private final Random random;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running;

    public WeatherDataGenerator(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        this.producer = new KafkaProducer<>(props);
        this.random = new Random();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.running = false;
    }

    /**
     * Start generating weather data at regular intervals
     * 
     * @param intervalSeconds Interval between data generation in seconds
     */
    public void startGenerating(int intervalSeconds) {
        if (running) {
            logger.warn("Generator is already running");
            return;
        }

        running = true;
        logger.info("Starting weather data generator with interval: {} seconds", intervalSeconds);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                generateAndSendData();
            } catch (Exception e) {
                logger.error("Error generating weather data", e);
            }
        }, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * Generate and send weather data for all stations
     */
    private void generateAndSendData() {
        for (String station : STATIONS) {
            WeatherData data = generateWeatherData(station);
            sendData(data);
        }
    }

    /**
     * Generate realistic weather data with some variation
     */
    private WeatherData generateWeatherData(String station) {
        // Generate temperature between 15°C and 45°C
        // Higher probability for temperatures > 30°C to test filtering
        double temperature;
        if (random.nextDouble() < 0.4) {
            // 40% chance of high temperature (30-45°C)
            temperature = 30.0 + (random.nextDouble() * 15.0);
        } else {
            // 60% chance of normal temperature (15-30°C)
            temperature = 15.0 + (random.nextDouble() * 15.0);
        }

        // Generate humidity between 30% and 90%
        double humidity = 30.0 + (random.nextDouble() * 60.0);

        // Round to 1 decimal place
        temperature = Math.round(temperature * 10.0) / 10.0;
        humidity = Math.round(humidity * 10.0) / 10.0;

        return new WeatherData(station, temperature, humidity);
    }

    /**
     * Send weather data to Kafka topic
     */
    private void sendData(WeatherData data) {
        String value = data.toCsv();
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, data.getStation(), value);

        // Record Prometheus metrics
        PrometheusMetricsServer.recordWeatherData(data.getStation(), data.getTemperature(), data.getHumidity());

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                logger.error("Error sending data: {}", value, exception);
            } else {
                logger.info("Sent: {} -> partition: {}, offset: {}",
                        value, metadata.partition(), metadata.offset());
                // Enregistrer le message produit
                PrometheusMetricsServer.recordKafkaProduced(TOPIC, data.getStation());
            }
        });
    }

    /**
     * Stop the generator
     */
    public void stop() {
        if (!running) {
            return;
        }

        running = false;
        logger.info("Stopping weather data generator");

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        producer.flush();
        producer.close();
        logger.info("Weather data generator stopped");
    }

    /**
     * Generate a single batch of data (useful for testing)
     */
    public void generateBatch(int count) {
        logger.info("Generating {} batches of weather data", count);
        for (int i = 0; i < count; i++) {
            generateAndSendData();
            try {
                Thread.sleep(100); // Small delay between batches
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        producer.flush();
    }
}
