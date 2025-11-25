package ma.abdex.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    void testGetInstance() {
        AppConfig config = AppConfig.getInstance();
        assertNotNull(config);

        // Singleton test
        AppConfig config2 = AppConfig.getInstance();
        assertSame(config, config2);
    }

    @Test
    void testGetKafkaBootstrapServers() {
        AppConfig config = AppConfig.getInstance();
        String servers = config.getKafkaBootstrapServers();
        assertNotNull(servers);
        assertFalse(servers.isEmpty());
    }

    @Test
    void testGetTemperatureThreshold() {
        AppConfig config = AppConfig.getInstance();
        double threshold = config.getTemperatureThreshold();
        assertTrue(threshold > 0);
    }

    @Test
    void testGetStations() {
        AppConfig config = AppConfig.getInstance();
        String[] stations = config.getStations();
        assertNotNull(stations);
        assertTrue(stations.length > 0);
    }

    @Test
    void testGetPrometheusPort() {
        AppConfig config = AppConfig.getInstance();
        int port = config.getPrometheusPort();
        assertTrue(port > 0 && port < 65536);
    }
}
