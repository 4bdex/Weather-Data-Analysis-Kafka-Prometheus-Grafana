package ma.abdex.util;

/**
 * Input validation utilities for weather data
 */
public class ValidationUtils {

    /**
     * Validate temperature value
     * 
     * @param temperature Temperature in Celsius
     * @throws IllegalArgumentException if temperature is invalid
     */
    public static void validateTemperature(double temperature) {
        if (temperature < -100 || temperature > 100) {
            throw new IllegalArgumentException(
                    String.format("Temperature out of valid range [-100, 100]: %.1f", temperature));
        }
        if (Double.isNaN(temperature) || Double.isInfinite(temperature)) {
            throw new IllegalArgumentException("Temperature must be a valid number");
        }
    }

    /**
     * Validate humidity value
     * 
     * @param humidity Humidity percentage
     * @throws IllegalArgumentException if humidity is invalid
     */
    public static void validateHumidity(double humidity) {
        if (humidity < 0 || humidity > 100) {
            throw new IllegalArgumentException(
                    String.format("Humidity out of valid range [0, 100]: %.1f", humidity));
        }
        if (Double.isNaN(humidity) || Double.isInfinite(humidity)) {
            throw new IllegalArgumentException("Humidity must be a valid number");
        }
    }

    /**
     * Validate station name
     * 
     * @param station Station identifier
     * @throws IllegalArgumentException if station is invalid
     */
    public static void validateStation(String station) {
        if (station == null || station.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        if (station.length() > 50) {
            throw new IllegalArgumentException("Station name too long (max 50 characters)");
        }
        if (!station.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException(
                    "Station name can only contain letters, numbers, underscores and hyphens");
        }
    }

    /**
     * Validate timestamp
     * 
     * @param timestamp Unix timestamp in milliseconds
     * @throws IllegalArgumentException if timestamp is invalid
     */
    public static void validateTimestamp(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long oneYearAgo = currentTime - (365L * 24 * 60 * 60 * 1000);
        long oneYearFuture = currentTime + (365L * 24 * 60 * 60 * 1000);

        if (timestamp < oneYearAgo || timestamp > oneYearFuture) {
            throw new IllegalArgumentException(
                    "Timestamp out of valid range (within one year of current time)");
        }
    }

    /**
     * Validate all weather data fields
     */
    public static void validateWeatherData(String station, double temperature,
            double humidity, long timestamp) {
        validateStation(station);
        validateTemperature(temperature);
        validateHumidity(humidity);
        validateTimestamp(timestamp);
    }
}
