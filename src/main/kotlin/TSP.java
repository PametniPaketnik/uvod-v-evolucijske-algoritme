import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
                return 0;
            case WEIGHTED:
                return 0;
            default:
                return Double.MAX_VALUE;
        }
    }

    public Tour generateTour() {
        //TODO generate random tour, use RandomUtils
        return null;
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
                .filter(line -> line.startsWith("EDGE_WEIGHT_TYPE : "))
                .findFirst()
                .ifPresent(distanceTypeLine -> {
                    String distanceTypeString = distanceTypeLine.substring("EDGE_WEIGHT_TYPE : ".length()).trim();
                    System.out.println("Distance type: " + distanceTypeString);
                    switch (distanceTypeString) {
                        case "EUC_2D" -> distanceType = DistanceType.EUCLIDEAN;
                        case "EXPLICIT" -> distanceType = DistanceType.WEIGHTED;
                        default -> System.err.println("Unknown distance type: " + distanceTypeString);
                    }
                });

        if (distanceType == DistanceType.EUCLIDEAN) {
            lines.stream()
                    .filter(line -> line.startsWith("NODE_COORD_SECTION"))
                    .findFirst()
                    .ifPresent(nodeCoordSectionLine -> {
                        int index = lines.indexOf(nodeCoordSectionLine) + 1;
                        for (int i = 0; i < numberOfCities; i++) {
                            String[] cityData = lines.get(index + i).trim().split("\\s+");
                            City city = new City();
                            city.index = Integer.parseInt(cityData[0]);
                            city.x = Double.parseDouble(cityData[1]);
                            city.y = Double.parseDouble(cityData[2]);
                            cities.add(city);
                        }
                    });
            for (City city : cities) {
                System.out.println(city.index + " " + city.x + " " + city.y);
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
