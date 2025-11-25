package ma.abdex.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.abdex.model.StationStats;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Custom serializer for StationStats objects
 */
public class StationStatsSerializer implements Serializer<StationStats> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, StationStats data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing StationStats", e);
        }
    }
}
