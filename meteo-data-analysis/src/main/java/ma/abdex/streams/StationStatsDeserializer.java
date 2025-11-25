package ma.abdex.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.abdex.model.StationStats;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Custom deserializer for StationStats objects
 */
public class StationStatsDeserializer implements Deserializer<StationStats> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public StationStats deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, StationStats.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing StationStats", e);
        }
    }
}
