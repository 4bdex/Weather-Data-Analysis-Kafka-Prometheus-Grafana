package ma.abdex.util;

import ma.abdex.model.WeatherData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class to generate sample weather data for testing
 */
public class TestDataGenerator {

    private static final Random random = new Random();
    private static final String[] STATIONS = { "Station1", "Station2", "Station3", "Station4", "Station5" };

    /**
     * Generate a list of sample weather data
     * 
     * @param count               Number of records to generate
     * @param highTempProbability Probability (0.0 to 1.0) of generating temperature
     *                            > 30°C
     * @return List of WeatherData
     */
    public static List<WeatherData> generateSampleData(int count, double highTempProbability) {
        List<WeatherData> dataList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String station = STATIONS[random.nextInt(STATIONS.length)];
            double temperature = generateTemperature(highTempProbability);
            double humidity = generateHumidity();

            WeatherData data = new WeatherData(station, temperature, humidity);
            dataList.add(data);
        }

        return dataList;
    }

    /**
     * Generate temperature with controlled probability of high values
     */
    private static double generateTemperature(double highTempProbability) {
        double temperature;
        if (random.nextDouble() < highTempProbability) {
            // High temperature (30-45°C)
            temperature = 30.0 + (random.nextDouble() * 15.0);
        } else {
            // Normal temperature (15-30°C)
            temperature = 15.0 + (random.nextDouble() * 15.0);
        }
        return Math.round(temperature * 10.0) / 10.0;
    }

    /**
     * Generate humidity between 30% and 90%
     */
    private static double generateHumidity() {
        double humidity = 30.0 + (random.nextDouble() * 60.0);
        return Math.round(humidity * 10.0) / 10.0;
    }

    /**
     * Print sample data in CSV format
     */
    public static void main(String[] args) {
        System.out.println("=== Sample Weather Data ===\n");
        System.out.println("Format: station,temperature,humidity\n");

        List<WeatherData> samples = generateSampleData(20, 0.4);

        System.out.println("All Data:");
        System.out.println("---------");
        for (WeatherData data : samples) {
            System.out.println(data.toCsv());
        }

        System.out.println("\nFiltered Data (temp > 30°C):");
        System.out.println("-----------------------------");
        samples.stream()
                .filter(data -> data.getTemperature() > 30.0)
                .forEach(data -> {
                    System.out.printf("%s -> %.1f°C\n",
                            data.toCsv(),
                            data.getTemperature());
                });

        System.out.println("\nStatistics:");
        System.out.println("-----------");
        long highTempCount = samples.stream()
                .filter(data -> data.getTemperature() > 30.0)
                .count();
        System.out.printf("Total records: %d\n", samples.size());
        System.out.printf("High temperature records: %d (%.1f%%)\n",
                highTempCount,
                (highTempCount * 100.0 / samples.size()));
    }
}
