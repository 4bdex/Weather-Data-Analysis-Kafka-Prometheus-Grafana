package ma.abdex.model;

import java.util.Objects;

/**
 * Model class representing weather data from a station
 */
public class WeatherData {
    private String station;
    private double temperature; // in Celsius
    private double humidity; // percentage
    private long timestamp; // for time series data

    public WeatherData() {
    }

    public WeatherData(String station, double temperature, double humidity) {
        this.station = station;
        this.temperature = temperature;
        this.humidity = humidity;
        this.timestamp = System.currentTimeMillis();
    }

    public WeatherData(String station, double temperature, double humidity, long timestamp) {
        this.station = station;
        this.temperature = temperature;
        this.humidity = humidity;
        this.timestamp = timestamp;
    }

    // Parse from CSV format: station,temperature,humidity
    public static WeatherData fromCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV string cannot be null or empty");
        }

        String[] parts = csv.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid CSV format (expected 3 fields): " + csv);
        }

        try {
            String station = parts[0].trim();
            double temperature = Double.parseDouble(parts[1].trim());
            double humidity = Double.parseDouble(parts[2].trim());

            // Validate the parsed data
            ma.abdex.util.ValidationUtils.validateStation(station);
            ma.abdex.util.ValidationUtils.validateTemperature(temperature);
            ma.abdex.util.ValidationUtils.validateHumidity(humidity);

            return new WeatherData(station, temperature, humidity);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format in CSV: " + csv, e);
        }
    }

    // Convert to CSV format
    public String toCsv() {
        return String.format("%s,%.1f,%.1f", station, temperature, humidity);
    }

    // Getters and Setters
    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("WeatherData{station='%s', temp=%.1f°C, humidity=%.1f%%, timestamp=%d}",
                station, temperature, humidity, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WeatherData that = (WeatherData) o;
        return Double.compare(that.temperature, temperature) == 0 &&
                Double.compare(that.humidity, humidity) == 0 &&
                Objects.equals(station, that.station);
    }

    @Override
    public int hashCode() {
        return Objects.hash(station, temperature, humidity);
    }
}
