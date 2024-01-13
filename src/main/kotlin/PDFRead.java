import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
