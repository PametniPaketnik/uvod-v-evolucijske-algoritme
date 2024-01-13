import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
            callDistanceMatrixAPI(locations);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void callDistanceMatrixAPI(Vector<Location> locations) {
        String apiKey = "AIzaSyBCyU6ZIp7eOLS9Zuc9GErl8pPgsJNLwyg";

        for (Location originLocation : locations) {
            for (Location destinationLocation : locations) {
                if (!originLocation.equals(destinationLocation)) {

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

                            System.out.println("From " + origin + " to " + destination + ": " + response.toString());
                            JSONObject jsonObject = new JSONObject(response.toString());
                            String distanceText = jsonObject.getJSONArray("rows")
                                    .getJSONObject(0)
                                    .getJSONArray("elements")
                                    .getJSONObject(0)
                                    .getJSONObject("distance")
                                    .getString("text");
                            System.out.println("Distance: " + distanceText);

                        } else {
                            System.out.println("Error: " + responseCode);
                        }

                        connection.disconnect();

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
