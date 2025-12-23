package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for reading CSV files.
 * Handles proper CSV parsing including quoted fields with commas.
 */
public class CSVReader {

    /**
     * Reads all lines from a CSV file and returns them as a list of string arrays.
     * Skips the header row.
     *
     * @param filePath Path to the CSV file
     * @return List of string arrays, each representing a row
     * @throws IOException if file cannot be read
     */
    public static List<String[]> readCSV(String filePath) throws IOException {
        List<String[]> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header row
                }

                String[] values = parseCSVLine(line);
                records.add(values);
            }
        }

        return records;
    }

    /**
     * Reads all lines from a CSV file including the header.
     *
     * @param filePath Path to the CSV file
     * @return List of string arrays, including header as first element
     * @throws IOException if file cannot be read
     */
    public static List<String[]> readCSVWithHeader(String filePath) throws IOException {
        List<String[]> records = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] values = parseCSVLine(line);
                records.add(values);
            }
        }

        return records;
    }

    /**
     * Reads only the header row from a CSV file.
     *
     * @param filePath Path to the CSV file
     * @return String array of column headers
     * @throws IOException if file cannot be read
     */
    public static String[] readHeader(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line != null) {
                return parseCSVLine(line);
            }
        }
        return new String[0];
    }

    /**
     * Parses a single CSV line, handling quoted fields that may contain commas.
     *
     * @param line The CSV line to parse
     * @return Array of field values
     */
    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentField.append('"');
                    i++;
                } else {
                    // Toggle quote mode
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                result.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        // Add the last field
        result.add(currentField.toString().trim());

        return result.toArray(new String[0]);
    }
}
