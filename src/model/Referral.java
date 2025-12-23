package model;

import java.time.LocalDate;

/**
 * Represents a referral between primary and secondary care in the healthcare management system.
 * Maps to referrals.csv data file.
 */
public class Referral {
    private String referralId;
    private String patientId;
    private String referringClinicianId;
    private String referredToClinicianId;
    private String referringFacilityId;
    private String referredToFacilityId;
    private LocalDate referralDate;
    private String urgencyLevel;
    private String referralReason;
    private String clinicalSummary;
    private String requestedInvestigations;
    private String status;
    private String appointmentId;
    private String notes;
    private LocalDate createdDate;
    private LocalDate lastUpdated;

    public Referral() {
    }

    public Referral(String referralId, String patientId, String referringClinicianId,
                    String referredToClinicianId, String referringFacilityId, String referredToFacilityId,
                    LocalDate referralDate, String urgencyLevel, String referralReason,
                    String clinicalSummary, String requestedInvestigations, String status,
                    String appointmentId, String notes, LocalDate createdDate, LocalDate lastUpdated) {
        this.referralId = referralId;
        this.patientId = patientId;
        this.referringClinicianId = referringClinicianId;
        this.referredToClinicianId = referredToClinicianId;
        this.referringFacilityId = referringFacilityId;
        this.referredToFacilityId = referredToFacilityId;
        this.referralDate = referralDate;
        this.urgencyLevel = urgencyLevel;
        this.referralReason = referralReason;
        this.clinicalSummary = clinicalSummary;
        this.requestedInvestigations = requestedInvestigations;
        this.status = status;
        this.appointmentId = appointmentId;
        this.notes = notes;
        this.createdDate = createdDate;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public String getReferralId() {
        return referralId;
    }

    public void setReferralId(String referralId) {
        this.referralId = referralId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getReferringClinicianId() {
        return referringClinicianId;
    }

    public void setReferringClinicianId(String referringClinicianId) {
        this.referringClinicianId = referringClinicianId;
    }

    public String getReferredToClinicianId() {
        return referredToClinicianId;
    }

    public void setReferredToClinicianId(String referredToClinicianId) {
        this.referredToClinicianId = referredToClinicianId;
    }

    public String getReferringFacilityId() {
        return referringFacilityId;
    }

    public void setReferringFacilityId(String referringFacilityId) {
        this.referringFacilityId = referringFacilityId;
    }

    public String getReferredToFacilityId() {
        return referredToFacilityId;
    }

    public void setReferredToFacilityId(String referredToFacilityId) {
        this.referredToFacilityId = referredToFacilityId;
    }

    public LocalDate getReferralDate() {
        return referralDate;
    }

    public void setReferralDate(LocalDate referralDate) {
        this.referralDate = referralDate;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public String getReferralReason() {
        return referralReason;
    }

    public void setReferralReason(String referralReason) {
        this.referralReason = referralReason;
    }

    public String getClinicalSummary() {
        return clinicalSummary;
    }

    public void setClinicalSummary(String clinicalSummary) {
        this.clinicalSummary = clinicalSummary;
    }

    public String getRequestedInvestigations() {
        return requestedInvestigations;
    }

    public void setRequestedInvestigations(String requestedInvestigations) {
        this.requestedInvestigations = requestedInvestigations;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isUrgent() {
        return "Urgent".equalsIgnoreCase(urgencyLevel);
    }

    public boolean isRoutine() {
        return "Routine".equalsIgnoreCase(urgencyLevel);
    }

    public boolean isNew() {
        return "New".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }

    public boolean isInProgress() {
        return "In Progress".equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return referralId + " - " + referralReason + " (" + urgencyLevel + ")";
    }
}
