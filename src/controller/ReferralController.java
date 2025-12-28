package controller;

import model.Clinician;
import model.Facility;
import model.Patient;
import model.Referral;
import model.ReferralManager;
import util.CSVReader;
import util.CSVWriter;
import util.FilePathManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for managing Referral data.
 * Uses the ReferralManager singleton for referral operations.
 * Handles CRUD operations and CSV file persistence.
 */
public class ReferralController {

    private ReferralManager referralManager;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] CSV_HEADER = {
            "referral_id", "patient_id", "referring_clinician_id", "referred_to_clinician_id",
            "referring_facility_id", "referred_to_facility_id", "referral_date", "urgency_level",
            "referral_reason", "clinical_summary", "requested_investigations", "status",
            "appointment_id", "notes", "created_date", "last_updated"
    };

    public ReferralController() {
        // Use the singleton instance of ReferralManager
        this.referralManager = ReferralManager.getInstance();
    }

    /**
     * Loads referrals from CSV file.
     *
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV() throws IOException {
        String filePath = FilePathManager.getReferralsFilePath();
        List<String[]> records = CSVReader.readCSV(filePath);
        List<Referral> referrals = new ArrayList<>();

        for (String[] record : records) {
            if (record.length >= 16) {
                Referral referral = new Referral();
                referral.setReferralId(record[0]);
                referral.setPatientId(record[1]);
                referral.setReferringClinicianId(record[2]);
                referral.setReferredToClinicianId(record[3]);
                referral.setReferringFacilityId(record[4]);
                referral.setReferredToFacilityId(record[5]);
                referral.setReferralDate(parseDate(record[6]));
                referral.setUrgencyLevel(record[7]);
                referral.setReferralReason(record[8]);
                referral.setClinicalSummary(record[9]);
                referral.setRequestedInvestigations(record[10]);
                referral.setStatus(record[11]);
                referral.setAppointmentId(record[12]);
                referral.setNotes(record[13]);
                referral.setCreatedDate(parseDate(record[14]));
                referral.setLastUpdated(parseDate(record[15]));
                referrals.add(referral);
            }
        }

        // Load into the singleton manager
        referralManager.setReferrals(referrals);
    }

    /**
     * Saves all referrals to CSV file.
     *
     * @throws IOException if file cannot be written
     */
    public void saveToCSV() throws IOException {
        String filePath = FilePathManager.getReferralsFilePath();
        List<String[]> data = new ArrayList<>();
        List<Referral> referrals = referralManager.getAllReferrals();

        for (Referral referral : referrals) {
            String[] row = {
                    referral.getReferralId(),
                    referral.getPatientId(),
                    referral.getReferringClinicianId(),
                    referral.getReferredToClinicianId(),
                    referral.getReferringFacilityId(),
                    referral.getReferredToFacilityId(),
                    formatDate(referral.getReferralDate()),
                    referral.getUrgencyLevel(),
                    referral.getReferralReason(),
                    referral.getClinicalSummary(),
                    referral.getRequestedInvestigations(),
                    referral.getStatus(),
                    referral.getAppointmentId() != null ? referral.getAppointmentId() : "",
                    referral.getNotes() != null ? referral.getNotes() : "",
                    formatDate(referral.getCreatedDate()),
                    formatDate(referral.getLastUpdated())
            };
            data.add(row);
        }

        CSVWriter.writeCSV(filePath, CSV_HEADER, data);
    }

    /**
     * Gets all referrals.
     *
     * @return List of all referrals
     */
    public List<Referral> getAllReferrals() {
        return referralManager.getAllReferrals();
    }

    /**
     * Gets a referral by ID.
     *
     * @param referralId The referral ID
     * @return The referral, or null if not found
     */
    public Referral getReferralById(String referralId) {
        return referralManager.getReferralById(referralId);
    }

    /**
     * Gets referrals for a patient.
     *
     * @param patientId The patient ID
     * @return List of referrals for the patient
     */
    public List<Referral> getReferralsByPatient(String patientId) {
        return referralManager.getReferralsByPatient(patientId);
    }

    /**
     * Gets urgent referrals.
     *
     * @return List of urgent referrals
     */
    public List<Referral> getUrgentReferrals() {
        return referralManager.getUrgentReferrals();
    }

    /**
     * Gets referrals by status.
     *
     * @param status The status
     * @return List of referrals with the status
     */
    public List<Referral> getReferralsByStatus(String status) {
        return referralManager.getReferralsByStatus(status);
    }

    /**
     * Adds a new referral.
     *
     * @param referral The referral to add
     */
    public void addReferral(Referral referral) {
        referralManager.addReferral(referral);
    }

    /**
     * Creates a new referral with full details.
     *
     * @param patientId              Patient ID
     * @param referringClinicianId   Referring clinician ID
     * @param referredToClinicianId  Referred to clinician ID
     * @param referringFacilityId    Referring facility ID
     * @param referredToFacilityId   Referred to facility ID
     * @param urgencyLevel           Urgency level
     * @param referralReason         Reason for referral
     * @param clinicalSummary        Clinical summary
     * @param requestedInvestigations Requested investigations
     * @param notes                  Additional notes
     * @return The created referral
     */
    public Referral createReferral(String patientId, String referringClinicianId,
                                    String referredToClinicianId, String referringFacilityId,
                                    String referredToFacilityId, String urgencyLevel,
                                    String referralReason, String clinicalSummary,
                                    String requestedInvestigations, String notes) {
        Referral referral = new Referral();
        referral.setReferralId(referralManager.getNextReferralId());
        referral.setPatientId(patientId);
        referral.setReferringClinicianId(referringClinicianId);
        referral.setReferredToClinicianId(referredToClinicianId);
        referral.setReferringFacilityId(referringFacilityId);
        referral.setReferredToFacilityId(referredToFacilityId);
        referral.setReferralDate(LocalDate.now());
        referral.setUrgencyLevel(urgencyLevel);
        referral.setReferralReason(referralReason);
        referral.setClinicalSummary(clinicalSummary);
        referral.setRequestedInvestigations(requestedInvestigations);
        referral.setStatus("New");
        referral.setNotes(notes);
        referral.setCreatedDate(LocalDate.now());
        referral.setLastUpdated(LocalDate.now());

        referralManager.addReferral(referral);
        return referral;
    }

    /**
     * Updates referral status.
     *
     * @param referralId The referral ID
     * @param newStatus  The new status
     * @return true if updated, false if not found
     */
    public boolean updateStatus(String referralId, String newStatus) {
        return referralManager.updateReferralStatus(referralId, newStatus);
    }

    /**
     * Updates an existing referral.
     *
     * @param referral The referral with updated data
     * @return true if updated, false if not found
     */
    public boolean updateReferral(Referral referral) {
        return referralManager.updateReferral(referral);
    }

    /**
     * Deletes a referral.
     *
     * @param referralId The referral ID
     * @return true if deleted, false if not found
     */
    public boolean deleteReferral(String referralId) {
        return referralManager.removeReferral(referralId);
    }

    /**
     * Generates a referral email/letter.
     *
     * @param referral            The referral
     * @param patient             The patient
     * @param referringClinician  The referring clinician
     * @param referredToClinician The referred to clinician
     * @param referringFacility   The referring facility
     * @param referredToFacility  The referred to facility
     * @return File path where email was saved
     * @throws IOException if file cannot be written
     */
    public String generateReferralEmail(Referral referral, Patient patient,
                                         Clinician referringClinician, Clinician referredToClinician,
                                         Facility referringFacility, Facility referredToFacility) throws IOException {
        return referralManager.generateReferralEmail(referral, patient,
                referringClinician, referredToClinician, referringFacility, referredToFacility);
    }

    /**
     * Updates EHR for a referral.
     *
     * @param referral The referral
     */
    public void updateEHR(Referral referral) {
        referralManager.updateEHR(referral);
    }

    /**
     * Gets the next available referral ID.
     *
     * @return The next referral ID
     */
    public String getNextReferralId() {
        return referralManager.getNextReferralId();
    }

    /**
     * Gets the audit log.
     *
     * @return List of audit entries
     */
    public List<String> getAuditLog() {
        return referralManager.getAuditLog();
    }

    /**
     * Gets the count of referrals.
     *
     * @return Number of referrals
     */
    public int getReferralCount() {
        return referralManager.getReferralCount();
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
