import java.util.Arrays;
import java.util.List;

public class TSPMain2 {
    public static void main(String[] args) {

        RandomUtils.setSeedFromTime();

        // Create an array of city data (index, x, y)
        double[][] citiesData = {
                {1, 46.050678, 14.514648},
                {2, 46.056964, 14.506656},
                {3, 46.056292, 14.534729},
                {4, 46.080174, 14.512328},
                {5, 46.039050, 14.499390},
                {6, 46.078630, 14.472450},
                {7, 46.054350, 14.511058},
                {8, 46.090413, 14.472588},
                {9, 46.202705, 14.541540},
                {10, 46.102442, 14.529620},

                // Add more cities as needed
        };
        for (int i = 0; i < 5; i++) {
            TSP eilTsp = new TSP("realProblem96.tsp", 1000, citiesData, "duration_matrix.tsp");
            //TSP eilTsp = new TSP("bays29.tsp", 1000);
            GA ga = new GA(100, 0.8, 0.1);
            TSP.Tour bestPath = ga.execute(eilTsp);

            System.out.println(bestPath.getDistance());
            System.out.println(Arrays.toString(bestPath.getPathIndexes()));
        }
        System.out.println(RandomUtils.getSeed());

    }
}
