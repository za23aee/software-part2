package model;

import util.FilePathManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton class for managing referrals in the healthcare system.
 * Ensures single instance creation of critical system components,
 * preventing resource conflicts and maintaining data consistency
 * across referral processes.
 */
public class ReferralManager {

    private static ReferralManager instance;
    private List<Referral> referralQueue;
    private List<String> auditLog;

    /**
     * Private constructor to prevent external instantiation.
     */
    private ReferralManager() {
        referralQueue = new ArrayList<>();
        auditLog = new ArrayList<>();
        logAudit("ReferralManager initialized");
    }

    /**
     * Gets the singleton instance of ReferralManager.
     * Thread-safe implementation using synchronized method.
     *
     * @return The single instance of ReferralManager
     */
    public static synchronized ReferralManager getInstance() {
        if (instance == null) {
            instance = new ReferralManager();
        }
        return instance;
    }

    /**
     * Adds a referral to the queue.
     *
     * @param referral The referral to add
     */
    public void addReferral(Referral referral) {
        referralQueue.add(referral);
        logAudit("Referral added: " + referral.getReferralId() + " - " + referral.getReferralReason());
    }

    /**
     * Removes a referral from the queue.
     *
     * @param referralId The ID of the referral to remove
     * @return true if referral was removed, false otherwise
     */
    public boolean removeReferral(String referralId) {
        boolean removed = referralQueue.removeIf(r -> r.getReferralId().equals(referralId));
        if (removed) {
            logAudit("Referral removed: " + referralId);
        }
        return removed;
    }

    /**
     * Gets all referrals in the queue.
     *
     * @return List of all referrals
     */
    public List<Referral> getAllReferrals() {
        return new ArrayList<>(referralQueue);
    }

    /**
     * Gets a referral by its ID.
     *
     * @param referralId The referral ID
     * @return The referral, or null if not found
     */
    public Referral getReferralById(String referralId) {
        return referralQueue.stream()
                .filter(r -> r.getReferralId().equals(referralId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all referrals for a specific patient.
     *
     * @param patientId The patient ID
     * @return List of referrals for the patient
     */
    public List<Referral> getReferralsByPatient(String patientId) {
        return referralQueue.stream()
                .filter(r -> r.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    /**
     * Gets all urgent referrals.
     *
     * @return List of urgent referrals
     */
    public List<Referral> getUrgentReferrals() {
        return referralQueue.stream()
                .filter(Referral::isUrgent)
                .collect(Collectors.toList());
    }

    /**
     * Gets referrals by status.
     *
     * @param status The status to filter by
     * @return List of referrals with the specified status
     */
    public List<Referral> getReferralsByStatus(String status) {
        return referralQueue.stream()
                .filter(r -> status.equalsIgnoreCase(r.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a referral.
     *
     * @param referralId The referral ID
     * @param newStatus  The new status
     * @return true if updated successfully, false otherwise
     */
    public boolean updateReferralStatus(String referralId, String newStatus) {
        Referral referral = getReferralById(referralId);
        if (referral != null) {
            String oldStatus = referral.getStatus();
            referral.setStatus(newStatus);
            referral.setLastUpdated(LocalDate.now());
            logAudit("Referral " + referralId + " status changed from " + oldStatus + " to " + newStatus);
            return true;
        }
        return false;
    }

    /**
     * Updates an existing referral with new data.
     *
     * @param updatedReferral The referral with updated data
     * @return true if updated successfully, false if not found
     */
    public boolean updateReferral(Referral updatedReferral) {
        for (int i = 0; i < referralQueue.size(); i++) {
            if (referralQueue.get(i).getReferralId().equals(updatedReferral.getReferralId())) {
                referralQueue.set(i, updatedReferral);
                logAudit("Referral updated: " + updatedReferral.getReferralId());
                return true;
            }
        }
        return false;
    }

    /**
     * Generates a referral email/letter content and saves it to a text file.
     * This simulates sending an email without actually sending one.
     *
     * @param referral        The referral to generate email for
     * @param patient         The patient being referred
     * @param referringClinician The clinician making the referral
     * @param referredToClinician The clinician receiving the referral
     * @param referringFacility The facility making the referral
     * @param referredToFacility The facility receiving the referral
     * @return The file path where the email was saved
     * @throws IOException if file cannot be written
     */
    public String generateReferralEmail(Referral referral, Patient patient,
                                         Clinician referringClinician, Clinician referredToClinician,
                                         Facility referringFacility, Facility referredToFacility) throws IOException {

        String filename = "referral_" + referral.getReferralId() + "_" +
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        String filePath = FilePathManager.getOutputFilePath(filename);

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("=".repeat(60)).append("\n");
        emailContent.append("                 REFERRAL LETTER\n");
        emailContent.append("=".repeat(60)).append("\n\n");

        emailContent.append("Referral ID: ").append(referral.getReferralId()).append("\n");
        emailContent.append("Date: ").append(referral.getReferralDate()).append("\n");
        emailContent.append("Urgency: ").append(referral.getUrgencyLevel()).append("\n\n");

        emailContent.append("-".repeat(60)).append("\n");
        emailContent.append("PATIENT DETAILS\n");
        emailContent.append("-".repeat(60)).append("\n");
        if (patient != null) {
            emailContent.append("Name: ").append(patient.getFullName()).append("\n");
            emailContent.append("Date of Birth: ").append(patient.getDateOfBirth()).append("\n");
            emailContent.append("NHS Number: ").append(patient.getNhsNumber()).append("\n");
            emailContent.append("Address: ").append(patient.getAddress()).append("\n");
            emailContent.append("Phone: ").append(patient.getPhoneNumber()).append("\n");
        }
        emailContent.append("\n");

        emailContent.append("-".repeat(60)).append("\n");
        emailContent.append("REFERRING CLINICIAN\n");
        emailContent.append("-".repeat(60)).append("\n");
        if (referringClinician != null) {
            emailContent.append("Name: ").append(referringClinician.getFullName()).append("\n");
            emailContent.append("Title: ").append(referringClinician.getTitle()).append("\n");
            emailContent.append("Email: ").append(referringClinician.getEmail()).append("\n");
        }
        if (referringFacility != null) {
            emailContent.append("Facility: ").append(referringFacility.getFacilityName()).append("\n");
            emailContent.append("Address: ").append(referringFacility.getAddress()).append("\n");
        }
        emailContent.append("\n");

        emailContent.append("-".repeat(60)).append("\n");
        emailContent.append("REFERRED TO\n");
        emailContent.append("-".repeat(60)).append("\n");
        if (referredToClinician != null) {
            emailContent.append("Name: ").append(referredToClinician.getFullName()).append("\n");
            emailContent.append("Speciality: ").append(referredToClinician.getSpeciality()).append("\n");
            emailContent.append("Email: ").append(referredToClinician.getEmail()).append("\n");
        }
        if (referredToFacility != null) {
            emailContent.append("Facility: ").append(referredToFacility.getFacilityName()).append("\n");
            emailContent.append("Address: ").append(referredToFacility.getAddress()).append("\n");
        }
        emailContent.append("\n");

        emailContent.append("-".repeat(60)).append("\n");
        emailContent.append("CLINICAL INFORMATION\n");
        emailContent.append("-".repeat(60)).append("\n");
        emailContent.append("Reason for Referral: ").append(referral.getReferralReason()).append("\n\n");
        emailContent.append("Clinical Summary:\n").append(referral.getClinicalSummary()).append("\n\n");
        emailContent.append("Requested Investigations: ").append(referral.getRequestedInvestigations()).append("\n\n");
        if (referral.getNotes() != null && !referral.getNotes().isEmpty()) {
            emailContent.append("Additional Notes: ").append(referral.getNotes()).append("\n");
        }
        emailContent.append("\n");

        emailContent.append("=".repeat(60)).append("\n");
        emailContent.append("This referral was generated by the Healthcare Management System\n");
        emailContent.append("Generated on: ").append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        emailContent.append("=".repeat(60)).append("\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(emailContent.toString());
        }

        logAudit("Referral email generated for " + referral.getReferralId() + " at " + filePath);
        return filePath;
    }

    /**
     * Updates the Electronic Health Record (simulated by logging).
     *
     * @param referral The referral to update EHR for
     */
    public void updateEHR(Referral referral) {
        logAudit("EHR updated for referral: " + referral.getReferralId() +
                " - Patient: " + referral.getPatientId() +
                " - Status: " + referral.getStatus());
    }

    /**
     * Gets the next available referral ID.
     *
     * @return The next referral ID in sequence
     */
    public String getNextReferralId() {
        int maxId = 0;
        for (Referral r : referralQueue) {
            String id = r.getReferralId();
            if (id != null && id.startsWith("R")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid IDs
                }
            }
        }
        return String.format("R%03d", maxId + 1);
    }

    /**
     * Clears all referrals from the queue.
     */
    public void clearAllReferrals() {
        referralQueue.clear();
        logAudit("All referrals cleared from queue");
    }

    /**
     * Sets the referral queue (used when loading from CSV).
     *
     * @param referrals List of referrals to set
     */
    public void setReferrals(List<Referral> referrals) {
        this.referralQueue = new ArrayList<>(referrals);
        logAudit("Referral queue loaded with " + referrals.size() + " referrals");
    }

    /**
     * Gets the audit log.
     *
     * @return List of audit log entries
     */
    public List<String> getAuditLog() {
        return new ArrayList<>(auditLog);
    }

    /**
     * Logs an audit entry with timestamp.
     *
     * @param message The audit message
     */
    private void logAudit(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        auditLog.add("[" + timestamp + "] " + message);
    }

    /**
     * Gets the count of referrals in the queue.
     *
     * @return Number of referrals
     */
    public int getReferralCount() {
        return referralQueue.size();
    }
}
