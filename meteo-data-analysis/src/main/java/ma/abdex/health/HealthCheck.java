package ma.abdex.health;

import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Health check manager for monitoring application status
 */
public class HealthCheck {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class);

    private static final HealthCheck instance = new HealthCheck();

    private final AtomicBoolean kafkaStreamsHealthy = new AtomicBoolean(false);
    private final AtomicBoolean generatorHealthy = new AtomicBoolean(false);
    private final AtomicBoolean prometheusHealthy = new AtomicBoolean(false);
    private final AtomicLong lastDataGeneratedTime = new AtomicLong(0);
    private final AtomicLong lastDataProcessedTime = new AtomicLong(0);

    // Prometheus gauge for health status
    private static final Gauge healthStatus = Gauge.build()
            .name("app_health_status")
            .help("Application component health status (1 = healthy, 0 = unhealthy)")
            .labelNames("component")
            .register();

    private HealthCheck() {
    }

    public static HealthCheck getInstance() {
        return instance;
    }

    public void setKafkaStreamsHealthy(boolean healthy) {
        kafkaStreamsHealthy.set(healthy);
        healthStatus.labels("kafka_streams").set(healthy ? 1 : 0);
        logger.debug("Kafka Streams health: {}", healthy);
    }

    public void setGeneratorHealthy(boolean healthy) {
        generatorHealthy.set(healthy);
        healthStatus.labels("generator").set(healthy ? 1 : 0);
        logger.debug("Generator health: {}", healthy);
    }

    public void setPrometheusHealthy(boolean healthy) {
        prometheusHealthy.set(healthy);
        healthStatus.labels("prometheus").set(healthy ? 1 : 0);
        logger.debug("Prometheus health: {}", healthy);
    }

    public void recordDataGenerated() {
        lastDataGeneratedTime.set(System.currentTimeMillis());
    }

    public void recordDataProcessed() {
        lastDataProcessedTime.set(System.currentTimeMillis());
    }

    public boolean isHealthy() {
        return kafkaStreamsHealthy.get() &&
                generatorHealthy.get() &&
                prometheusHealthy.get() &&
                isDataFlowHealthy();
    }

    private boolean isDataFlowHealthy() {
        long now = System.currentTimeMillis();
        long lastGenerated = lastDataGeneratedTime.get();
        long lastProcessed = lastDataProcessedTime.get();

        // Allow 30 seconds of no activity before considering unhealthy
        boolean genHealthy = (now - lastGenerated) < 30000 || lastGenerated == 0;
        boolean procHealthy = (now - lastProcessed) < 30000 || lastProcessed == 0;

        return genHealthy && procHealthy;
    }

    public String getHealthReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Application Health Report ===\n");
        report.append("Overall Status: ").append(isHealthy() ? "HEALTHY" : "UNHEALTHY").append("\n");
        report.append("Kafka Streams: ").append(kafkaStreamsHealthy.get() ? "UP" : "DOWN").append("\n");
        report.append("Generator: ").append(generatorHealthy.get() ? "UP" : "DOWN").append("\n");
        report.append("Prometheus: ").append(prometheusHealthy.get() ? "UP" : "DOWN").append("\n");

        long now = System.currentTimeMillis();
        long lastGen = lastDataGeneratedTime.get();
        long lastProc = lastDataProcessedTime.get();

        if (lastGen > 0) {
            report.append("Last data generated: ").append((now - lastGen) / 1000).append("s ago\n");
        }
        if (lastProc > 0) {
            report.append("Last data processed: ").append((now - lastProc) / 1000).append("s ago\n");
        }

        return report.toString();
    }
}
