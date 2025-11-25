package ma.abdex.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void testValidateTemperature_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.validateTemperature(25.0));
        assertDoesNotThrow(() -> ValidationUtils.validateTemperature(-50.0));
        assertDoesNotThrow(() -> ValidationUtils.validateTemperature(99.9));
    }

    @Test
    void testValidateTemperature_TooHigh() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateTemperature(101.0));
    }

    @Test
    void testValidateTemperature_TooLow() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateTemperature(-101.0));
    }

    @Test
    void testValidateTemperature_NaN() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateTemperature(Double.NaN));
    }

    @Test
    void testValidateHumidity_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.validateHumidity(50.0));
        assertDoesNotThrow(() -> ValidationUtils.validateHumidity(0.0));
        assertDoesNotThrow(() -> ValidationUtils.validateHumidity(100.0));
    }

    @Test
    void testValidateHumidity_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateHumidity(-1.0));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateHumidity(101.0));
    }

    @Test
    void testValidateStation_Valid() {
        assertDoesNotThrow(() -> ValidationUtils.validateStation("Station1"));
        assertDoesNotThrow(() -> ValidationUtils.validateStation("Station_ABC-123"));
    }

    @Test
    void testValidateStation_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateStation(null));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateStation(""));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateStation("Station With Spaces"));
    }

    @Test
    void testValidateTimestamp_Valid() {
        long now = System.currentTimeMillis();
        assertDoesNotThrow(() -> ValidationUtils.validateTimestamp(now));
    }

    @Test
    void testValidateTimestamp_TooOld() {
        long twoYearsAgo = System.currentTimeMillis() - (2L * 365 * 24 * 60 * 60 * 1000);
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.validateTimestamp(twoYearsAgo));
    }
}
