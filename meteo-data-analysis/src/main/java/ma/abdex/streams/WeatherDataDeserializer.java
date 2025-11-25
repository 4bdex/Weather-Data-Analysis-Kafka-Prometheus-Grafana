package ma.abdex.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import ma.abdex.model.WeatherData;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Custom deserializer for WeatherData objects
 */
public class WeatherDataDeserializer implements Deserializer<WeatherData> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public WeatherData deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.readValue(data, WeatherData.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing WeatherData", e);
        }
    }
}
