package util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for managing file paths in the application.
 * Ensures consistent path handling across different environments.
 */
public class FilePathManager {

    private static String basePath;

    static {
        // Initialize base path to the project root directory
        basePath = System.getProperty("user.dir");
    }

    /**
     * Gets the base path of the application.
     *
     * @return Base path string
     */
    public static String getBasePath() {
        return basePath;
    }

    /**
     * Sets the base path of the application.
     * Useful for testing or when running from different locations.
     *
     * @param path New base path
     */
    public static void setBasePath(String path) {
        basePath = path;
    }

    /**
     * Gets the full path to a file in the data directory.
     *
     * @param filename Name of the file
     * @return Full path to the file
     */
    public static String getDataFilePath(String filename) {
        return Paths.get(basePath, "data", filename).toString();
    }

    /**
     * Gets the full path to the patients.csv file.
     *
     * @return Full path to patients.csv
     */
    public static String getPatientsFilePath() {
        return getDataFilePath("patients.csv");
    }

    /**
     * Gets the full path to the clinicians.csv file.
     *
     * @return Full path to clinicians.csv
     */
    public static String getCliniciansFilePath() {
        return getDataFilePath("clinicians.csv");
    }

    /**
     * Gets the full path to the facilities.csv file.
     *
     * @return Full path to facilities.csv
     */
    public static String getFacilitiesFilePath() {
        return getDataFilePath("facilities.csv");
    }

    /**
     * Gets the full path to the appointments.csv file.
     *
     * @return Full path to appointments.csv
     */
    public static String getAppointmentsFilePath() {
        return getDataFilePath("appointments.csv");
    }

    /**
     * Gets the full path to the prescriptions.csv file.
     *
     * @return Full path to prescriptions.csv
     */
    public static String getPrescriptionsFilePath() {
        return getDataFilePath("prescriptions.csv");
    }

    /**
     * Gets the full path to the referrals.csv file.
     *
     * @return Full path to referrals.csv
     */
    public static String getReferralsFilePath() {
        return getDataFilePath("referrals.csv");
    }

    /**
     * Gets the full path to the staff.csv file.
     *
     * @return Full path to staff.csv
     */
    public static String getStaffFilePath() {
        return getDataFilePath("staff.csv");
    }

    /**
     * Gets the full path for output files (like referral emails).
     *
     * @param filename Name of the output file
     * @return Full path to the output file
     */
    public static String getOutputFilePath(String filename) {
        String outputDir = Paths.get(basePath, "output").toString();
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return Paths.get(outputDir, filename).toString();
    }

    /**
     * Ensures the data directory exists.
     *
     * @return true if directory exists or was created successfully
     */
    public static boolean ensureDataDirectoryExists() {
        File dataDir = new File(Paths.get(basePath, "data").toString());
        if (!dataDir.exists()) {
            return dataDir.mkdirs();
        }
        return true;
    }

    /**
     * Checks if a file exists.
     *
     * @param filePath Path to the file
     * @return true if file exists
     */
    public static boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }
}
