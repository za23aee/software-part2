package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for writing CSV files.
 * Handles proper CSV formatting including escaping quotes and commas.
 */
public class CSVWriter {

    /**
     * Writes data to a CSV file, overwriting existing content.
     *
     * @param filePath Path to the CSV file
     * @param header   Column headers
     * @param data     List of string arrays representing rows
     * @throws IOException if file cannot be written
     */
    public static void writeCSV(String filePath, String[] header, List<String[]> data) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            bw.write(formatCSVLine(header));
            bw.newLine();

            // Write data rows
            for (String[] row : data) {
                bw.write(formatCSVLine(row));
                bw.newLine();
            }
        }
    }

    /**
     * Appends a single row to an existing CSV file.
     *
     * @param filePath Path to the CSV file
     * @param row      String array representing the row to append
     * @throws IOException if file cannot be written
     */
    public static void appendRow(String filePath, String[] row) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(formatCSVLine(row));
            bw.newLine();
        }
    }

    /**
     * Appends multiple rows to an existing CSV file.
     *
     * @param filePath Path to the CSV file
     * @param rows     List of string arrays representing rows to append
     * @throws IOException if file cannot be written
     */
    public static void appendRows(String filePath, List<String[]> rows) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            for (String[] row : rows) {
                bw.write(formatCSVLine(row));
                bw.newLine();
            }
        }
    }

    /**
     * Formats an array of values into a CSV line.
     * Handles escaping of commas and quotes within fields.
     *
     * @param values Array of field values
     * @return Formatted CSV line
     */
    private static String formatCSVLine(String[] values) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(",");
            }

            String value = values[i];
            if (value == null) {
                value = "";
            }

            // Check if value needs quoting
            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                // Escape quotes by doubling them
                value = value.replace("\"", "\"\"");
                sb.append("\"").append(value).append("\"");
            } else {
                sb.append(value);
            }
        }

        return sb.toString();
    }

    /**
     * Escapes a single value for CSV format.
     *
     * @param value The value to escape
     * @return Escaped value
     */
    public static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}
