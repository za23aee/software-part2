package controller;

import model.Clinician;
import util.CSVReader;
import util.CSVWriter;
import util.FilePathManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller class for managing Clinician data.
 * Handles CRUD operations and CSV file persistence.
 */
public class ClinicianController {

    private List<Clinician> clinicians;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] CSV_HEADER = {
            "clinician_id", "first_name", "last_name", "title", "speciality",
            "gmc_number", "phone_number", "email", "workplace_id", "workplace_type",
            "employment_status", "start_date"
    };

    public ClinicianController() {
        clinicians = new ArrayList<>();
    }

    /**
     * Loads clinicians from CSV file.
     *
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV() throws IOException {
        String filePath = FilePathManager.getCliniciansFilePath();
        List<String[]> records = CSVReader.readCSV(filePath);
        clinicians.clear();

        for (String[] record : records) {
            if (record.length >= 12) {
                Clinician clinician = new Clinician();
                clinician.setClinicianId(record[0]);
                clinician.setFirstName(record[1]);
                clinician.setLastName(record[2]);
                clinician.setTitle(record[3]);
                clinician.setSpeciality(record[4]);
                clinician.setGmcNumber(record[5]);
                clinician.setPhoneNumber(record[6]);
                clinician.setEmail(record[7]);
                clinician.setWorkplaceId(record[8]);
                clinician.setWorkplaceType(record[9]);
                clinician.setEmploymentStatus(record[10]);
                clinician.setStartDate(parseDate(record[11]));
                clinicians.add(clinician);
            }
        }
    }

    /**
     * Saves all clinicians to CSV file.
     *
     * @throws IOException if file cannot be written
     */
    public void saveToCSV() throws IOException {
        String filePath = FilePathManager.getCliniciansFilePath();
        List<String[]> data = new ArrayList<>();

        for (Clinician clinician : clinicians) {
            String[] row = {
                    clinician.getClinicianId(),
                    clinician.getFirstName(),
                    clinician.getLastName(),
                    clinician.getTitle(),
                    clinician.getSpeciality(),
                    clinician.getGmcNumber(),
                    clinician.getPhoneNumber(),
                    clinician.getEmail(),
                    clinician.getWorkplaceId(),
                    clinician.getWorkplaceType(),
                    clinician.getEmploymentStatus(),
                    formatDate(clinician.getStartDate())
            };
            data.add(row);
        }

        CSVWriter.writeCSV(filePath, CSV_HEADER, data);
    }

    /**
     * Gets all clinicians.
     *
     * @return List of all clinicians
     */
    public List<Clinician> getAllClinicians() {
        return new ArrayList<>(clinicians);
    }

    /**
     * Gets a clinician by ID.
     *
     * @param clinicianId The clinician ID
     * @return The clinician, or null if not found
     */
    public Clinician getClinicianById(String clinicianId) {
        return clinicians.stream()
                .filter(c -> c.getClinicianId().equals(clinicianId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all GPs.
     *
     * @return List of GPs
     */
    public List<Clinician> getGPs() {
        return clinicians.stream()
                .filter(Clinician::isGP)
                .collect(Collectors.toList());
    }

    /**
     * Gets all specialists (consultants).
     *
     * @return List of specialists
     */
    public List<Clinician> getSpecialists() {
        return clinicians.stream()
                .filter(Clinician::isSpecialist)
                .collect(Collectors.toList());
    }

    /**
     * Gets all nurses.
     *
     * @return List of nurses
     */
    public List<Clinician> getNurses() {
        return clinicians.stream()
                .filter(Clinician::isNurse)
                .collect(Collectors.toList());
    }

    /**
     * Gets clinicians by workplace.
     *
     * @param workplaceId The workplace ID
     * @return List of clinicians at the workplace
     */
    public List<Clinician> getCliniciansByWorkplace(String workplaceId) {
        return clinicians.stream()
                .filter(c -> c.getWorkplaceId().equals(workplaceId))
                .collect(Collectors.toList());
    }

    /**
     * Gets clinicians by speciality.
     *
     * @param speciality The speciality
     * @return List of clinicians with the speciality
     */
    public List<Clinician> getCliniciansBySpeciality(String speciality) {
        return clinicians.stream()
                .filter(c -> c.getSpeciality().equalsIgnoreCase(speciality))
                .collect(Collectors.toList());
    }

    /**
     * Searches clinicians by name.
     *
     * @param searchTerm The search term
     * @return List of matching clinicians
     */
    public List<Clinician> searchByName(String searchTerm) {
        String term = searchTerm.toLowerCase();
        return clinicians.stream()
                .filter(c -> c.getFirstName().toLowerCase().contains(term) ||
                        c.getLastName().toLowerCase().contains(term) ||
                        c.getFullName().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new clinician.
     *
     * @param clinician The clinician to add
     */
    public void addClinician(Clinician clinician) {
        clinicians.add(clinician);
    }

    /**
     * Updates an existing clinician.
     *
     * @param clinician The clinician with updated data
     * @return true if updated, false if not found
     */
    public boolean updateClinician(Clinician clinician) {
        for (int i = 0; i < clinicians.size(); i++) {
            if (clinicians.get(i).getClinicianId().equals(clinician.getClinicianId())) {
                clinicians.set(i, clinician);
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a clinician by ID.
     *
     * @param clinicianId The clinician ID
     * @return true if deleted, false if not found
     */
    public boolean deleteClinician(String clinicianId) {
        return clinicians.removeIf(c -> c.getClinicianId().equals(clinicianId));
    }

    /**
     * Gets the next available clinician ID.
     *
     * @return The next clinician ID
     */
    public String getNextClinicianId() {
        int maxId = 0;
        for (Clinician c : clinicians) {
            String id = c.getClinicianId();
            if (id != null && id.startsWith("C")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        return String.format("C%03d", maxId + 1);
    }

    /**
     * Gets the count of clinicians.
     *
     * @return Number of clinicians
     */
    public int getClinicianCount() {
        return clinicians.size();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMAT);
    }
}
