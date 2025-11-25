package ma.abdex.model;

/**
 * Model class representing aggregated statistics for a station
 */
public class StationStats {
    private String station;
    private double avgTemperatureCelsius;
    private double avgHumidity;
    private int count;

    public StationStats() {
    }

    public StationStats(String station, double avgTemperatureCelsius, double avgHumidity, int count) {
        this.station = station;
        this.avgTemperatureCelsius = avgTemperatureCelsius;
        this.avgHumidity = avgHumidity;
        this.count = count;
    }

    // Getters and Setters
    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public double getAvgTemperatureCelsius() {
        return avgTemperatureCelsius;
    }

    public void setAvgTemperatureCelsius(double avgTemperatureCelsius) {
        this.avgTemperatureCelsius = avgTemperatureCelsius;
    }

    public double getAvgHumidity() {
        return avgHumidity;
    }

    public void setAvgHumidity(double avgHumidity) {
        this.avgHumidity = avgHumidity;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return String.format("%s : Average Temperature = %.1f°C, Average Humidity = %.1f%% (Count: %d)",
                station, avgTemperatureCelsius, avgHumidity, count);
    }

    public String toJson() {
        return String.format("{\"station\":\"%s\",\"avgTemperature\":%.2f,\"avgHumidity\":%.2f,\"count\":%d}",
                station, avgTemperatureCelsius, avgHumidity, count);
    }
}
