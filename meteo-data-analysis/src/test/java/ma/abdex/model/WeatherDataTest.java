package ma.abdex.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeatherDataTest {

    @Test
    void testFromCsv_ValidInput() {
        String csv = "Station1,25.5,65.0";
        WeatherData data = WeatherData.fromCsv(csv);

        assertEquals("Station1", data.getStation());
        assertEquals(25.5, data.getTemperature(), 0.01);
        assertEquals(65.0, data.getHumidity(), 0.01);
    }

    @Test
    void testFromCsv_InvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> {
            WeatherData.fromCsv("Station1,25.5");
        });
    }

    @Test
    void testToCsv() {
        WeatherData data = new WeatherData("Station1", 25.5, 65.0);
        String csv = data.toCsv();

        assertTrue(csv.contains("Station1"));
        assertTrue(csv.contains("25.5"));
        assertTrue(csv.contains("65.0"));
    }

    @Test
    void testTemperatureInCelsius() {
        WeatherData data = new WeatherData("Station1", 25.5, 65.0);
        assertEquals(25.5, data.getTemperature(), 0.01);

        data.setTemperature(100);
        assertEquals(100.0, data.getTemperature(), 0.01);
    }
}
