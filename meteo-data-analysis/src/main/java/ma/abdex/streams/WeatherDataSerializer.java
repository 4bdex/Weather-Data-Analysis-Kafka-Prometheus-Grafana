package ma.abdex.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.abdex.model.WeatherData;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Custom serializer for WeatherData objects
 */
public class WeatherDataSerializer implements Serializer<WeatherData> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, WeatherData data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing WeatherData", e);
        }
    }
}
