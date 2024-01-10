import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        //TODO implement
        switch (distanceType) {
            case EUCLIDEAN:
                return Math.sqrt(Math.pow(to.x - from.x, 2) + Math.pow(to.y - from.y, 2));
            case WEIGHTED:
                if (from.index < to.index)
                    return weights[from.index - 1][to.index - 1];
                else if (from.index > to.index)
                    return weights[to.index - 1][from.index - 1];
                else
                    return 0;
            default:
                return Double.MAX_VALUE;
        }
    }

    public Tour generateTour() {
        //TODO generate random tour, use RandomUtils
        Tour randomTour = new Tour(numberOfCities);
        List<City> citiesCopy = new ArrayList<>(cities);

        Collections.shuffle(citiesCopy);

        randomTour.setPath(citiesCopy.toArray(new City[0]));

        System.out.println();
        for (City city : randomTour.getPath()) {
            System.out.print(city.index + " ");
        }

        return randomTour;
        //return null;
    }

    private void loadData(String path) {
        //TODO set starting city, which is always at index 0

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
        System.out.println(lines);
        //TODO parse data

        lines.stream()
                .filter(line -> line.startsWith("NAME : "))
                .findFirst()
                .ifPresent(nameLine -> {
                    name = nameLine.substring("NAME : ".length()).trim();
                    System.out.println("Name: " + name);
                });

        lines.stream()
                .filter(line -> line.startsWith("DIMENSION : "))
                .findFirst()
                .ifPresent(dimensionLine -> {
                    numberOfCities = Integer.parseInt(dimensionLine.substring("DIMENSION : ".length()).trim());
                    System.out.println("Number of cities: " + numberOfCities);
                });

        lines.stream()
                .filter(line -> line.startsWith("EDGE_WEIGHT_TYPE"))
                .findFirst()
                .ifPresent(distanceTypeLine -> {
                    String afterColon = distanceTypeLine.substring(distanceTypeLine.indexOf(':') + 1).trim();
                    String distanceTypeString = afterColon.split("\\s+")[0]; // Extract the first word after the colon
                    System.out.println("Distance type: " + distanceTypeString);
                    switch (distanceTypeString) {
                        case "EUC_2D" -> distanceType = DistanceType.EUCLIDEAN;
                        case "EXPLICIT" -> distanceType = DistanceType.WEIGHTED;
                        default -> System.err.println("Unknown distance type: " + distanceTypeString);
                    }
                });


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

            cities.forEach(city -> System.out.println(city.index + " " + city.x + " " + city.y));

            if (!cities.isEmpty()) {
                start = cities.get(0); // Set the first city as the starting city
                System.out.println("Starting city: " + start.index + " " + start.x + " " + start.y);
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

            // print weights
            for (double[] weight : weights) {
                for (double v : weight) {
                    System.out.print(v + " ");
                }
                System.out.println();
            }
            System.out.println("Cities:");
            cities.forEach(city -> System.out.println(city.index + " " + city.x + " " + city.y));

            if (!cities.isEmpty()) {
                start = cities.get(0); // Set the first city as the starting city
                System.out.println("Starting city: " + start.index + " " + start.x + " " + start.y);
            }
        }
        System.out.println(calculateDistance(start, cities.get(2)));
    }

    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    public int getNumberOfEvaluations() {
        return numberOfEvaluations;
    }
}
