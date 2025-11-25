package ma.abdex.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for loading application properties
 */
public class AppConfig {
    private static final String CONFIG_FILE = "application.properties";
    private static AppConfig instance;
    private final Properties properties;

    private AppConfig() {
        properties = new Properties();
        loadProperties();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration", e);
        }
    }

    public String getKafkaBootstrapServers() {
        return getProperty("kafka.bootstrap.servers", "localhost:9092");
    }

    public String getKafkaApplicationId() {
        return getProperty("kafka.application.id", "weather-analysis-app");
    }

    public int getKafkaStreamsCommitInterval() {
        return getIntProperty("kafka.streams.commit.interval.ms", 1000);
    }

    public long getKafkaStreamsCacheMaxBytes() {
        return getLongProperty("kafka.streams.cache.max.bytes", 10485760L);
    }

    public int getKafkaStreamsNumThreads() {
        return getIntProperty("kafka.streams.num.stream.threads", 2);
    }

    public String getInputTopic() {
        return getProperty("kafka.topic.input", "weather-data");
    }

    public String getOutputTopic() {
        return getProperty("kafka.topic.output", "station-averages");
    }

    public int getTopicPartitions() {
        return getIntProperty("kafka.topic.partitions", 3);
    }

    public int getTopicReplicationFactor() {
        return getIntProperty("kafka.topic.replication.factor", 1);
    }

    public double getTemperatureThreshold() {
        return getDoubleProperty("weather.temperature.threshold", 30.0);
    }

    public int getGeneratorIntervalSeconds() {
        return getIntProperty("weather.generator.interval.seconds", 5);
    }

    public String[] getStations() {
        String stationsStr = getProperty("weather.stations", "Station1,Station2,Station3");
        return stationsStr.split(",");
    }

    public String getProducerAcks() {
        return getProperty("kafka.producer.acks", "all");
    }

    public int getProducerRetries() {
        return getIntProperty("kafka.producer.retries", 3);
    }

    public String getProducerCompressionType() {
        return getProperty("kafka.producer.compression.type", "snappy");
    }

    public int getPrometheusPort() {
        return getIntProperty("prometheus.port", 8080);
    }

    public boolean isPrometheusEnabled() {
        return getBooleanProperty("prometheus.enabled", true);
    }

    public int getShutdownTimeoutSeconds() {
        return getIntProperty("app.shutdown.timeout.seconds", 30);
    }

    // Helper methods
    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid integer value for " + key + ": " + value);
            }
        }
        return defaultValue;
    }

    private long getLongProperty(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid long value for " + key + ": " + value);
            }
        }
        return defaultValue;
    }

    private double getDoubleProperty(String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid double value for " + key + ": " + value);
            }
        }
        return defaultValue;
    }

    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value.trim());
        }
        return defaultValue;
    }

    /**
     * Get all properties
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }
}
