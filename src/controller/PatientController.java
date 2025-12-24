package controller;

import model.Patient;
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
 * Controller class for managing Patient data.
 * Handles CRUD operations and CSV file persistence.
 */
public class PatientController {

    private List<Patient> patients;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] CSV_HEADER = {
            "patient_id", "first_name", "last_name", "date_of_birth", "nhs_number",
            "gender", "phone_number", "email", "address", "postcode",
            "emergency_contact_name", "emergency_contact_phone", "registration_date", "gp_surgery_id"
    };

    public PatientController() {
        patients = new ArrayList<>();
    }

    /**
     * Loads patients from CSV file.
     *
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV() throws IOException {
        String filePath = FilePathManager.getPatientsFilePath();
        List<String[]> records = CSVReader.readCSV(filePath);
        patients.clear();

        for (String[] record : records) {
            if (record.length >= 14) {
                Patient patient = new Patient();
                patient.setPatientId(record[0]);
                patient.setFirstName(record[1]);
                patient.setLastName(record[2]);
                patient.setDateOfBirth(parseDate(record[3]));
                patient.setNhsNumber(record[4]);
                patient.setGender(record[5]);
                patient.setPhoneNumber(record[6]);
                patient.setEmail(record[7]);
                patient.setAddress(record[8]);
                patient.setPostcode(record[9]);
                patient.setEmergencyContactName(record[10]);
                patient.setEmergencyContactPhone(record[11]);
                patient.setRegistrationDate(parseDate(record[12]));
                patient.setGpSurgeryId(record[13]);
                patients.add(patient);
            }
        }
    }

    /**
     * Saves all patients to CSV file.
     *
     * @throws IOException if file cannot be written
     */
    public void saveToCSV() throws IOException {
        String filePath = FilePathManager.getPatientsFilePath();
        List<String[]> data = new ArrayList<>();

        for (Patient patient : patients) {
            String[] row = {
                    patient.getPatientId(),
                    patient.getFirstName(),
                    patient.getLastName(),
                    formatDate(patient.getDateOfBirth()),
                    patient.getNhsNumber(),
                    patient.getGender(),
                    patient.getPhoneNumber(),
                    patient.getEmail(),
                    patient.getAddress(),
                    patient.getPostcode(),
                    patient.getEmergencyContactName(),
                    patient.getEmergencyContactPhone(),
                    formatDate(patient.getRegistrationDate()),
                    patient.getGpSurgeryId()
            };
            data.add(row);
        }

        CSVWriter.writeCSV(filePath, CSV_HEADER, data);
    }

    /**
     * Gets all patients.
     *
     * @return List of all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients);
    }

    /**
     * Gets a patient by ID.
     *
     * @param patientId The patient ID
     * @return The patient, or null if not found
     */
    public Patient getPatientById(String patientId) {
        return patients.stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets a patient by NHS number.
     *
     * @param nhsNumber The NHS number
     * @return The patient, or null if not found
     */
    public Patient getPatientByNhsNumber(String nhsNumber) {
        return patients.stream()
                .filter(p -> p.getNhsNumber().equals(nhsNumber))
                .findFirst()
                .orElse(null);
    }

    /**
     * Searches patients by name.
     *
     * @param searchTerm The search term
     * @return List of matching patients
     */
    public List<Patient> searchByName(String searchTerm) {
        String term = searchTerm.toLowerCase();
        return patients.stream()
                .filter(p -> p.getFirstName().toLowerCase().contains(term) ||
                        p.getLastName().toLowerCase().contains(term) ||
                        p.getFullName().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    /**
     * Gets patients by GP surgery.
     *
     * @param gpSurgeryId The GP surgery ID
     * @return List of patients registered at the surgery
     */
    public List<Patient> getPatientsByGPSurgery(String gpSurgeryId) {
        return patients.stream()
                .filter(p -> p.getGpSurgeryId().equals(gpSurgeryId))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new patient.
     *
     * @param patient The patient to add
     */
    public void addPatient(Patient patient) {
        patients.add(patient);
    }

    /**
     * Updates an existing patient.
     *
     * @param patient The patient with updated data
     * @return true if updated, false if not found
     */
    public boolean updatePatient(Patient patient) {
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getPatientId().equals(patient.getPatientId())) {
                patients.set(i, patient);
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a patient by ID.
     *
     * @param patientId The patient ID
     * @return true if deleted, false if not found
     */
    public boolean deletePatient(String patientId) {
        return patients.removeIf(p -> p.getPatientId().equals(patientId));
    }

    /**
     * Gets the next available patient ID.
     *
     * @return The next patient ID
     */
    public String getNextPatientId() {
        int maxId = 0;
        for (Patient p : patients) {
            String id = p.getPatientId();
            if (id != null && id.startsWith("P")) {
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
        return String.format("P%03d", maxId + 1);
    }

    /**
     * Gets the count of patients.
     *
     * @return Number of patients
     */
    public int getPatientCount() {
        return patients.size();
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
