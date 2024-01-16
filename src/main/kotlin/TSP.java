import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TSP {

    enum DistanceType {EUCLIDEAN, WEIGHTED}

    public class City {
        public int index;
        public double x, y;
    }

    public class Tour {

        double distance;
        int dimension;
        City[] path;

        public Tour(Tour tour) {
            distance = tour.distance;
            dimension = tour.dimension;
            path = tour.path.clone();
        }

        public Tour(int dimension) {
            this.dimension = dimension;
            path = new City[dimension];
            distance = Double.MAX_VALUE;
        }

        public Tour clone() {
            return new Tour(this);
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public City[] getPath() {
            return path;
        }

        public int[] getPathIndexes() {
            int[] pathIndexes = new int[dimension];
            for (int i = 0; i < dimension; i++) {
                pathIndexes[i] = path[i].index;
            }
            return pathIndexes;
        }

        public void setPath(City[] path) {
            this.path = path.clone();
        }

        public void setCity(int index, City city) {
            path[index] = city;
            distance = Double.MAX_VALUE;
        }
    }

    String name;
    City start;
    List<City> cities = new ArrayList<>();
    int numberOfCities;
    double[][] weights;
    DistanceType distanceType = DistanceType.EUCLIDEAN;
    int numberOfEvaluations, maxEvaluations;


    public TSP(String path, int maxEvaluations) {
        loadData(path);
        numberOfEvaluations = 0;
        this.maxEvaluations = maxEvaluations;
    }

    public TSP(String path, int maxEvaluations, double[][] citiesData, String pathToWeights) {
        numberOfEvaluations = 0;
        this.maxEvaluations = maxEvaluations;
        this.name = path;

        for (double[] cityData : citiesData) {
            City city = new City();
            city.index = (int) cityData[0];
            city.x = cityData[1];
            city.y = cityData[2];
            cities.add(city);
        }
        start = cities.get(0);

        loadNewData(pathToWeights);
    }

    public void evaluate(Tour tour) {
        double distance = 0;
        distance += calculateDistance(start, tour.getPath()[0]);
        for (int index = 0; index < numberOfCities; index++) {
            if (index + 1 < numberOfCities)
                distance += calculateDistance(tour.getPath()[index], tour.getPath()[index + 1]);
            else
                distance += calculateDistance(tour.getPath()[index], start);
        }
        tour.setDistance(distance);
        numberOfEvaluations++;
    }

    private double calculateDistance(City from, City to) {
        //DONE
        switch (distanceType) {
            case EUCLIDEAN:
                return Math.sqrt(Math.pow(to.x - from.x, 2) + Math.pow(to.y - from.y, 2));
            case WEIGHTED:
                return weights[from.index - 1][to.index - 1];
            default:
                return Double.MAX_VALUE;
        }
    }

    public Tour generateTour() {
        //DONE generate random tour, use RandomUtils DONE
        Tour randomTour = new Tour(numberOfCities);
        List<City> citiesCopy = new ArrayList<>(cities);

        Collections.shuffle(citiesCopy);

        randomTour.setPath(citiesCopy.toArray(new City[0]));

        return randomTour;
    }

    private void loadData(String path) {
        //DONE set starting city, which is always at index 0

        InputStream inputStream = TSP.class.getClassLoader().getResourceAsStream(path);
        if(inputStream == null) {
            System.err.println("File "+path+" not found!");
            return;
        }

        List<String> lines = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            String line = br.readLine();
            while (line != null) {
                lines.add(line);
               line = br.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //DONE parse data

        lines.stream()
                .filter(line -> line.startsWith("NAME"))
                .findFirst()
                .ifPresent(nameLine -> {
                    String afterColon = nameLine.substring(nameLine.indexOf(':') + 1).trim();
                    name = afterColon.split("\s+")[0];
                });

        lines.stream()
                .filter(line -> line.startsWith("DIMENSION"))
                .findFirst()
                .ifPresent(dimensionLine -> {
                    String afterColon = dimensionLine.substring(dimensionLine.indexOf(':') + 1).trim();
                    String distanceTypeString = afterColon.split("\s+")[0];
                    numberOfCities = Integer.parseInt(distanceTypeString);
                });

        lines.stream()
                .filter(line -> line.startsWith("EDGE_WEIGHT_TYPE"))
                .findFirst()
                .ifPresent(distanceTypeLine -> {
                    String afterColon = distanceTypeLine.substring(distanceTypeLine.indexOf(':') + 1).trim();
                    String distanceTypeString = afterColon.split("\\s+")[0];

                    switch (distanceTypeString) {
                        case "EUC_2D" -> distanceType = DistanceType.EUCLIDEAN;
                        case "EXPLICIT" -> distanceType = DistanceType.WEIGHTED;
                        default -> System.err.println("Unknown distance type: " + distanceTypeString);
                    }
                });

        if (Objects.equals(name, "realProblem96")) {
            System.out.println("Name: " + name);
            boolean foundNodeCoordSection = false;
            for (String line : lines) {
                if (foundNodeCoordSection && !line.trim().equals("EOF")) {
                    String[] cityData = line.trim().split("\\s+");
                    if (cityData.length == 3) {
                        City city = new City();
                        city.index = Integer.parseInt(cityData[0]);
                        city.x = Double.parseDouble(cityData[1]);
                        city.y = Double.parseDouble(cityData[2]);
                        cities.add(city);
                    }
                }
                if (line.startsWith("NODE_COORD_SECTION")) {
                    foundNodeCoordSection = true;
                }
            }

            if (!cities.isEmpty()) {
                start = cities.get(0);
            }

            // load weights from txt file
            InputStream inputStreamMatrix = TSP.class.getClassLoader().getResourceAsStream("distance_matrix.tsp");
            if(inputStreamMatrix == null) {
                System.err.println("File distance_matrix.tsp not found!");
                return;
            }

            List<String> linesMatrix = new ArrayList<>();
            try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStreamMatrix))) {

                String line = br.readLine();
                while (line != null) {
                    linesMatrix.add(line);
                    line = br.readLine();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // read weights
            List<List<Double>> weightsList = new ArrayList<>();
            for (String line : linesMatrix) {
                String[] tokens = line.trim().split("\\s+");
                List<Double> row = new ArrayList<>();
                for (String token : tokens) {
                    if (!token.isEmpty()) {
                        row.add(Double.parseDouble(token));
                    }
                }
                weightsList.add(row);
            }

            weights = new double[weightsList.size()][weightsList.get(0).size()];
            for (int i = 0; i < weightsList.size(); i++) {
                for (int j = 0; j < weightsList.get(i).size(); j++) {
                    weights[i][j] = weightsList.get(i).get(j);
                }
            }
            return;
        }

        if (distanceType == DistanceType.EUCLIDEAN) {
            boolean foundNodeCoordSection = false;
            for (String line : lines) {
                if (foundNodeCoordSection && !line.trim().equals("EOF")) {
                    String[] cityData = line.trim().split("\\s+");
                    if (cityData.length == 3) {
                        City city = new City();
                        city.index = Integer.parseInt(cityData[0]);
                        city.x = Double.parseDouble(cityData[1]);
                        city.y = Double.parseDouble(cityData[2]);
                        cities.add(city);
                    }
                }
                if (line.startsWith("NODE_COORD_SECTION")) {
                    foundNodeCoordSection = true;
                }
            }
            //cities.forEach(city -> System.out.println(city.index + " " + city.x + " " + city.y));

            if (!cities.isEmpty()) {
                start = cities.get(0);
            }
        }

        else if (distanceType == DistanceType.WEIGHTED) {
            boolean foundEdgeWeightSection = false;
            boolean foundDisplayDataSection = false;
            List<List<Double>> weightsList = new ArrayList<>();

            for (String line : lines) {
                if (foundEdgeWeightSection && !foundDisplayDataSection && !line.trim().equals("DISPLAY_DATA_SECTION") && !line.trim().equals("EOF")) {
                    String[] tokens = line.trim().split("\\s+");
                    List<Double> row = new ArrayList<>();
                    for (String token : tokens) {
                        if (!token.isEmpty()) {
                            row.add(Double.parseDouble(token));
                        }
                    }
                    weightsList.add(row);
                }
                if (foundDisplayDataSection && !line.trim().equals("EOF")) {
                    String[] cityData = line.trim().split("\\s+");
                    if (cityData.length == 3) {
                        City city = new City();
                        city.index = Integer.parseInt(cityData[0]);
                        city.x = Double.parseDouble(cityData[1]);
                        city.y = Double.parseDouble(cityData[2]);
                        cities.add(city);
                    }
                }

                if (line.startsWith("EDGE_WEIGHT_SECTION")) {
                    foundEdgeWeightSection = true;
                }
                if (line.startsWith("DISPLAY_DATA_SECTION")) {
                    foundDisplayDataSection = true;
                }
            }

            // Convert the List<List<Double>> to a double[][]
            weights = new double[weightsList.size()][weightsList.get(0).size()];
            for (int i = 0; i < weightsList.size(); i++) {
                for (int j = 0; j < weightsList.get(i).size(); j++) {
                    weights[i][j] = weightsList.get(i).get(j);
                }
            }

            if (!cities.isEmpty()) {
                start = cities.get(0);
            }
        }
    }

    public void loadNewData(String pathToWeights) {
        distanceType = DistanceType.WEIGHTED;
        numberOfCities = cities.size();

        InputStream inputStreamMatrix = TSP.class.getClassLoader().getResourceAsStream(pathToWeights);
        if(inputStreamMatrix == null) {
            System.err.println("File " + pathToWeights + " not found!");
            return;
        }

        List<String> linesMatrix = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStreamMatrix))) {

            String line = br.readLine();
            while (line != null) {
                linesMatrix.add(line);
                line = br.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<List<Double>> weightsList = new ArrayList<>();
        for (String line : linesMatrix) {
            String[] tokens = line.trim().split("\\s+");
            List<Double> row = new ArrayList<>();
            for (String token : tokens) {
                if (!token.isEmpty()) {
                    row.add(Double.parseDouble(token));
                }
            }
            weightsList.add(row);
        }

        weights = new double[weightsList.size()][weightsList.get(0).size()];
        for (int i = 0; i < weightsList.size(); i++) {
            for (int j = 0; j < weightsList.get(i).size(); j++) {
                weights[i][j] = weightsList.get(i).get(j);
            }
        }
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    public int getNumberOfEvaluations() {
        return numberOfEvaluations;
    }
}
