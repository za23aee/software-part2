package controller;

import model.Prescription;
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
 * Controller class for managing Prescription data.
 * Handles CRUD operations and CSV file persistence.
 */
public class PrescriptionController {

    private List<Prescription> prescriptions;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] CSV_HEADER = {
            "prescription_id", "patient_id", "clinician_id", "appointment_id",
            "prescription_date", "medication_name", "dosage", "frequency",
            "duration_days", "quantity", "instructions", "pharmacy_name",
            "status", "issue_date", "collection_date"
    };

    public PrescriptionController() {
        prescriptions = new ArrayList<>();
    }

    /**
     * Loads prescriptions from CSV file.
     *
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV() throws IOException {
        String filePath = FilePathManager.getPrescriptionsFilePath();
        List<String[]> records = CSVReader.readCSV(filePath);
        prescriptions.clear();

        for (String[] record : records) {
            if (record.length >= 15) {
                Prescription prescription = new Prescription();
                prescription.setPrescriptionId(record[0]);
                prescription.setPatientId(record[1]);
                prescription.setClinicianId(record[2]);
                prescription.setAppointmentId(record[3]);
                prescription.setPrescriptionDate(parseDate(record[4]));
                prescription.setMedicationName(record[5]);
                prescription.setDosage(record[6]);
                prescription.setFrequency(record[7]);
                prescription.setDurationDays(parseInt(record[8]));
                prescription.setQuantity(record[9]);
                prescription.setInstructions(record[10]);
                prescription.setPharmacyName(record[11]);
                prescription.setStatus(record[12]);
                prescription.setIssueDate(parseDate(record[13]));
                prescription.setCollectionDate(parseDate(record[14]));
                prescriptions.add(prescription);
            }
        }
    }

    /**
     * Saves all prescriptions to CSV file.
     *
     * @throws IOException if file cannot be written
     */
    public void saveToCSV() throws IOException {
        String filePath = FilePathManager.getPrescriptionsFilePath();
        List<String[]> data = new ArrayList<>();

        for (Prescription prescription : prescriptions) {
            String[] row = {
                    prescription.getPrescriptionId(),
                    prescription.getPatientId(),
                    prescription.getClinicianId(),
                    prescription.getAppointmentId() != null ? prescription.getAppointmentId() : "",
                    formatDate(prescription.getPrescriptionDate()),
                    prescription.getMedicationName(),
                    prescription.getDosage(),
                    prescription.getFrequency(),
                    String.valueOf(prescription.getDurationDays()),
                    prescription.getQuantity(),
                    prescription.getInstructions(),
                    prescription.getPharmacyName(),
                    prescription.getStatus(),
                    formatDate(prescription.getIssueDate()),
                    formatDate(prescription.getCollectionDate())
            };
            data.add(row);
        }

        CSVWriter.writeCSV(filePath, CSV_HEADER, data);
    }

    /**
     * Gets all prescriptions.
     *
     * @return List of all prescriptions
     */
    public List<Prescription> getAllPrescriptions() {
        return new ArrayList<>(prescriptions);
    }

    /**
     * Gets a prescription by ID.
     *
     * @param prescriptionId The prescription ID
     * @return The prescription, or null if not found
     */
    public Prescription getPrescriptionById(String prescriptionId) {
        return prescriptions.stream()
                .filter(p -> p.getPrescriptionId().equals(prescriptionId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets prescriptions for a patient.
     *
     * @param patientId The patient ID
     * @return List of prescriptions for the patient
     */
    public List<Prescription> getPrescriptionsByPatient(String patientId) {
        return prescriptions.stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    /**
     * Gets prescriptions by clinician.
     *
     * @param clinicianId The clinician ID
     * @return List of prescriptions by the clinician
     */
    public List<Prescription> getPrescriptionsByClinician(String clinicianId) {
        return prescriptions.stream()
                .filter(p -> p.getClinicianId().equals(clinicianId))
                .collect(Collectors.toList());
    }

    /**
     * Gets prescriptions by status.
     *
     * @param status The status
     * @return List of prescriptions with the status
     */
    public List<Prescription> getPrescriptionsByStatus(String status) {
        return prescriptions.stream()
                .filter(p -> status.equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Gets issued prescriptions (not yet collected).
     *
     * @return List of issued prescriptions
     */
    public List<Prescription> getIssuedPrescriptions() {
        return prescriptions.stream()
                .filter(Prescription::isIssued)
                .collect(Collectors.toList());
    }

    /**
     * Searches prescriptions by medication name.
     *
     * @param medicationName The medication name to search for
     * @return List of matching prescriptions
     */
    public List<Prescription> searchByMedication(String medicationName) {
        String term = medicationName.toLowerCase();
        return prescriptions.stream()
                .filter(p -> p.getMedicationName().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new prescription.
     *
     * @param prescription The prescription to add
     */
    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
    }

    /**
     * Updates an existing prescription.
     *
     * @param prescription The prescription with updated data
     * @return true if updated, false if not found
     */
    public boolean updatePrescription(Prescription prescription) {
        for (int i = 0; i < prescriptions.size(); i++) {
            if (prescriptions.get(i).getPrescriptionId().equals(prescription.getPrescriptionId())) {
                prescriptions.set(i, prescription);
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a prescription by ID.
     *
     * @param prescriptionId The prescription ID
     * @return true if deleted, false if not found
     */
    public boolean deletePrescription(String prescriptionId) {
        return prescriptions.removeIf(p -> p.getPrescriptionId().equals(prescriptionId));
    }

    /**
     * Marks a prescription as collected.
     *
     * @param prescriptionId The prescription ID
     * @return true if marked, false if not found
     */
    public boolean markAsCollected(String prescriptionId) {
        Prescription prescription = getPrescriptionById(prescriptionId);
        if (prescription != null) {
            prescription.setStatus("Collected");
            prescription.setCollectionDate(LocalDate.now());
            return true;
        }
        return false;
    }

    /**
     * Gets the next available prescription ID.
     *
     * @return The next prescription ID
     */
    public String getNextPrescriptionId() {
        int maxId = 0;
        for (Prescription p : prescriptions) {
            String id = p.getPrescriptionId();
            if (id != null && id.startsWith("RX")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        return String.format("RX%03d", maxId + 1);
    }

    /**
     * Gets the count of prescriptions.
     *
     * @return Number of prescriptions
     */
    public int getPrescriptionCount() {
        return prescriptions.size();
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

    private int parseInt(String intStr) {
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMAT);
    }
}
