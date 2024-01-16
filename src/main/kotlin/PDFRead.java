import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Scanner;
import java.util.Vector;

public class PDFRead {
    public static void main(String[] args) {
        String filePath = "src/main/kotlin/Direct4me-seznam-lokacij.pdf";
        try {
            PDDocument document = PDDocument.load(new File(filePath));
            PDFTextStripper pdfStripper = new PDFTextStripper();

            String text = pdfStripper.getText(document);
            Scanner scanner = new Scanner(text);

            Vector<Location> locations = new Vector<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.matches("\\d{4} .*")) {
                    String[] parts = line.split("\\s+");

                    int postalCode = Integer.parseInt(parts[0]);
                    String postOfficeName = parts[1];
                    StringBuilder addressBuilder = new StringBuilder();
                    for (int i = 2; i < parts.length - 1; i++) {
                        addressBuilder.append(parts[i]).append(" ");
                    }
                    String address = addressBuilder.toString().trim();

                    locations.add(new Location(postalCode, postOfficeName, address));
                }
            }

            document.close();
            scanner.close();

            // For demonstration, print out the locations
            for (Location location : locations) {
                System.out.println(location.getPostalCode() + " " + location.getPostOfficeName() + " " + location.getAddress());

            }
            //callDistanceMatrixAPI(locations);
            //callDurationMatrixAPI(locations);
            writeCoordinatesToFile(locations);
            //writeLocationsToFile(locations);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void callDistanceMatrixAPI(Vector<Location> locations) {
        String apiKey = "AIzaSyBCyU6ZIp7eOLS9Zuc9GErl8pPgsJNLwyg"; // Please replace with your actual API key

        double[][] distanceMatrix = new double[96][96]; // Initialize 96x96 matrix

        // Fill the matrix with default values
        for (int i = 0; i < 96; i++) {
            for (int j = 0; j < 96; j++) {
                distanceMatrix[i][j] = (i == j) ? 0 : -1; // 0 for same location, -1 for others
            }
        }

        try {
            FileWriter fileWriter = new FileWriter("distance_matrix.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (int i = 0; i < Math.min(locations.size(), 96); i++) {
                for (int j = 0; j < Math.min(locations.size(), 96); j++) {
                    if (i != j) {
                        Location originLocation = locations.get(i);
                        Location destinationLocation = locations.get(j);

                        String origin = originLocation.getAddress() + " " + originLocation.getPostOfficeName() + " " + originLocation.getPostalCode();
                        String destination = destinationLocation.getAddress() + " " + destinationLocation.getPostOfficeName() + " " + destinationLocation.getPostalCode();

                        try {
                            String urlString = "https://maps.googleapis.com/maps/api/distancematrix/json"
                                    + "?origins=" + URLEncoder.encode(origin, "UTF-8")
                                    + "&destinations=" + URLEncoder.encode(destination, "UTF-8")
                                    + "&key=" + apiKey;

                            URL url = new URL(urlString);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");

                            int responseCode = connection.getResponseCode();

                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                String line;
                                StringBuilder response = new StringBuilder();

                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }

                                reader.close();
                                JSONObject jsonObject = new JSONObject(response.toString());
                                int distanceMeters = jsonObject.getJSONArray("rows")
                                        .getJSONObject(0)
                                        .getJSONArray("elements")
                                        .getJSONObject(0)
                                        .getJSONObject("distance")
                                        .getInt("value");

                                // Convert meters to kilometers and round to 1 decimal place
                                double distanceKilometers = Math.round(distanceMeters / 100.0) / 10.0;
                                distanceMatrix[i][j] = distanceKilometers;

                            } else {
                                System.out.println("Error: " + responseCode);
                                distanceMatrix[i][j] = -1; // Indicate an error
                            }

                            connection.disconnect();

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            distanceMatrix[i][j] = -1; // Indicate an error
                        } catch (IOException e) {
                            e.printStackTrace();
                            distanceMatrix[i][j] = -1; // Indicate an error
                        }
                    } else {
                        // If origin and destination are the same, the distance is 0
                        distanceMatrix[i][j] = 0.0;
                    }

                    // Write the distance to the file
                    bufferedWriter.write(distanceMatrix[i][j] + (j == locations.size() - 1 ? "\n" : "\t"));
                }
                System.out.println("Completed line: " + (i + 1));
            }

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void callDurationMatrixAPI(Vector<Location> locations) {
        String apiKey = "AIzaSyBCyU6ZIp7eOLS9Zuc9GErl8pPgsJNLwyg"; // Please replace with your actual API key

        double[][] durationMatrix = new double[96][96]; // Initialize 96x96 matrix

        // Fill the matrix with default values
        for (int i = 0; i < 96; i++) {
            for (int j = 0; j < 96; j++) {
                durationMatrix[i][j] = (i == j) ? 0 : -1; // 0 for same location, -1 for others
            }
        }
        try {
            FileWriter fileWriter = new FileWriter("duration_matrix.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (int i = 0; i < Math.min(locations.size(), 96); i++) {
                for (int j = 0; j < Math.min(locations.size(), 96); j++) {
                    if (i != j) {
                        Location originLocation = locations.get(i);
                        Location destinationLocation = locations.get(j);

                        String origin = originLocation.getAddress() + " " + originLocation.getPostOfficeName() + " " + originLocation.getPostalCode();
                        String destination = destinationLocation.getAddress() + " " + destinationLocation.getPostOfficeName() + " " + destinationLocation.getPostalCode();

                        try {
                            String urlString = "https://maps.googleapis.com/maps/api/distancematrix/json"
                                    + "?origins=" + URLEncoder.encode(origin, "UTF-8")
                                    + "&destinations=" + URLEncoder.encode(destination, "UTF-8")
                                    + "&key=" + apiKey;

                            URL url = new URL(urlString);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("GET");

                            int responseCode = connection.getResponseCode();

                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                String line;
                                StringBuilder response = new StringBuilder();

                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }

                                reader.close();
                                JSONObject jsonObject = new JSONObject(response.toString());
                                int durationSeconds = jsonObject.getJSONArray("rows")
                                        .getJSONObject(0)
                                        .getJSONArray("elements")
                                        .getJSONObject(0)
                                        .getJSONObject("duration")
                                        .getInt("value");

                                // Convert seconds to minutes and round to 1 decimal place
                                double durationMinutes = Math.round(durationSeconds / 6.0) / 10.0;
                                durationMatrix[i][j] = durationMinutes;

                            } else {
                                System.out.println("Error: " + responseCode);
                                durationMatrix[i][j] = -1; // Indicate an error
                            }

                            connection.disconnect();

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                            durationMatrix[i][j] = -1; // Indicate an error
                        } catch (IOException e) {
                            e.printStackTrace();
                            durationMatrix[i][j] = -1; // Indicate an error
                        }
                    } else {
                        // If origin and destination are the same, the duration is 0
                        durationMatrix[i][j] = 0.0;
                    }

                    // Write the duration to the file
                    bufferedWriter.write(durationMatrix[i][j] + (j == locations.size() - 1 ? "\n" : "\t"));
                }
                System.out.println("Completed line: " + (i + 1));
            }

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeCoordinatesToFile(Vector<Location> locations) {
        String apiKey = "AIzaSyC2a1IxcFw_Lb3qGwM3t6NlK4osuXKOhR4"; // Replace with your actual API key

        try {
            FileWriter fileWriter = new FileWriter("src/main/resources/realProblem96_data1.tsp");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("NAME : realProblem96\n");
            bufferedWriter.write("TYPE : TSP\n");
            bufferedWriter.write("DIMENSIONS : 96\n");
            bufferedWriter.write("EDGE_WEIGHT_TYPE : EXPLICIT\n");
            bufferedWriter.write("EDGE_WEIGHT_FORMAT : FULL_MATRIX\n");
            bufferedWriter.write("NODE_COORD_SECTION\n");

            int nodeNumber = 1;
            for (Location location : locations) {
                String address = location.getAddress() + " " + location.getPostOfficeName() + " " + location.getPostalCode();
                double[] coords = getCoordinatesFromAddress(address, apiKey);

                if (coords != null)
                {
                    bufferedWriter.write(String.format(Locale.ENGLISH, "%d %s%n", nodeNumber++, location.getAddress()));
                    bufferedWriter.write(String.format(Locale.ENGLISH, "%f %f%n", coords[0], coords[1]));
                }
            }

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double[] getCoordinatesFromAddress(String address, String apiKey) {
        try {
            String urlString = "https://maps.googleapis.com/maps/api/geocode/json"
                    + "?address=" + URLEncoder.encode(address, "UTF-8")
                    + "&key=" + apiKey;

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONObject location = jsonObject.getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location");

                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");

                return new double[]{lat, lng};
            } else {
                System.out.println("Geocoding API request failed with response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeLocationsToFile(Vector<Location> locations) {
        try {
            FileWriter fileWriter = new FileWriter("src/main/resources/realProblem96_data.tsp");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("NAME : realProblem96\n");
            bufferedWriter.write("TYPE : TSP\n");
            bufferedWriter.write("DIMENSIONS : 96\n");
            bufferedWriter.write("DATA\n");

            int i = 1;
            for (Location location : locations) {
                bufferedWriter.write(i++ + " " + location.getAddress() + "\n");
            }

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}