import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TSPTest {

    public static void main(String[] args) {
        RandomUtils.setSeedFromTime(); // set a new seed on each run

        int[] populations = {1000, 10000, 100000, 1000000};

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/results/dca1389.txt", true))) {
            for (int population : populations) {
                double min = Integer.MAX_VALUE;
                double avg = 0;
                double std = 0;

                // Perform 30 runs for each population size
                for (int i = 0; i < 30; i++) {
                    TSP eilTsp = new TSP("dca1389.tsp", population);
                    GA ga = new GA(100, 0.8, 0.1);
                    TSP.Tour bestPath = ga.execute(eilTsp);

                    // Save min, avg, and std
                    if (bestPath.getDistance() < min) {
                        min = bestPath.getDistance();
                    }
                    avg += bestPath.getDistance();
                    std += Math.pow(bestPath.getDistance(), 2);
                }

                // Print results for each population size
                System.out.println("Population: " + population);
                System.out.println("Min: " + min);
                System.out.println("Avg: " + avg / 30);
                System.out.println("Std: " + Math.sqrt(std / 30 - Math.pow(avg / 30, 2)));
                System.out.println(); // add a newline for better readability

                // Append results to the file
                writer.write("Population: " + population + "\n");
                writer.write("Min: " + min + "\n");
                writer.write("Avg: " + avg / 30 + "\n");
                writer.write("Std: " + Math.sqrt(std / 30 - Math.pow(avg / 30, 2)) + "\n");
                writer.write("\n"); // add a newline for better readability
            }

            // Write the seed used
            writer.write("Seed: " + RandomUtils.getSeed() + "\n");
            // Add current date and time
            LocalDateTime currentDateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = currentDateTime.format(formatter);
            writer.write("Date and Time: " + formattedDateTime + "\n");

            writer.write("---------------------------------------------------------\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
