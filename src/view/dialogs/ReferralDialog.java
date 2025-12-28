package view.dialogs;

import controller.ClinicianController;
import controller.FacilityController;
import controller.PatientController;
import controller.ReferralController;
import model.Clinician;
import model.Facility;
import model.Patient;
import model.Referral;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Dialog for adding or editing a referral.
 * Uses ReferralManager singleton through ReferralController.
 */
public class ReferralDialog extends JDialog {

    private ReferralController referralController;
    private PatientController patientController;
    private ClinicianController clinicianController;
    private FacilityController facilityController;
    private Referral referral;
    private boolean saved = false;

    private JComboBox<PatientItem> patientCombo;
    private JComboBox<ClinicianItem> referringClinicianCombo;
    private JComboBox<ClinicianItem> referredToClinicianCombo;
    private JComboBox<FacilityItem> referringFacilityCombo;
    private JComboBox<FacilityItem> referredToFacilityCombo;
    private JTextField referralDateField;
    private JComboBox<String> urgencyCombo;
    private JTextField reasonField;
    private JTextArea clinicalSummaryArea;
    private JTextField investigationsField;
    private JTextArea notesArea;
    private JComboBox<String> statusCombo;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReferralDialog(Frame parent, String title, Referral referral,
                          ReferralController referralController,
                          PatientController patientController,
                          ClinicianController clinicianController,
                          FacilityController facilityController) {
        super(parent, title, true);
        this.referral = referral;
        this.referralController = referralController;
        this.patientController = patientController;
        this.clinicianController = clinicianController;
        this.facilityController = facilityController;

        initComponents();
        if (referral != null) {
            populateFields();
        } else {
            referralDateField.setText(LocalDate.now().format(DATE_FORMAT));
        }

        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Patient
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Patient:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        patientCombo = new JComboBox<>();
        loadPatients();
        formPanel.add(patientCombo, gbc);

        // Referring Clinician
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Referring Clinician:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        referringClinicianCombo = new JComboBox<>();
        loadClinicians(referringClinicianCombo);
        formPanel.add(referringClinicianCombo, gbc);

        // Referred To Clinician
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Referred To Clinician:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        referredToClinicianCombo = new JComboBox<>();
        loadClinicians(referredToClinicianCombo);
        formPanel.add(referredToClinicianCombo, gbc);

        // Referring Facility
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Referring Facility:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        referringFacilityCombo = new JComboBox<>();
        loadFacilities(referringFacilityCombo);
        formPanel.add(referringFacilityCombo, gbc);

        // Referred To Facility
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Referred To Facility:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        referredToFacilityCombo = new JComboBox<>();
        loadFacilities(referredToFacilityCombo);
        formPanel.add(referredToFacilityCombo, gbc);

        // Referral Date
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Referral Date (YYYY-MM-DD):*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        referralDateField = new JTextField(20);
        formPanel.add(referralDateField, gbc);

        // Urgency Level
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Urgency Level:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        urgencyCombo = new JComboBox<>(new String[]{"Urgent", "Routine", "Non-urgent"});
        formPanel.add(urgencyCombo, gbc);

        // Reason
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Referral Reason:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        reasonField = new JTextField(30);
        formPanel.add(reasonField, gbc);

        // Clinical Summary
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Clinical Summary:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        clinicalSummaryArea = new JTextArea(4, 30);
        clinicalSummaryArea.setLineWrap(true);
        clinicalSummaryArea.setWrapStyleWord(true);
        JScrollPane summaryScroll = new JScrollPane(clinicalSummaryArea);
        formPanel.add(summaryScroll, gbc);

        // Requested Investigations
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Requested Investigations:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        investigationsField = new JTextField(30);
        formPanel.add(investigationsField, gbc);

        // Status (only for editing)
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        statusCombo = new JComboBox<>(new String[]{"New", "Pending", "In Progress", "Completed"});
        statusCombo.setEnabled(referral != null);
        formPanel.add(statusCombo, gbc);

        // Notes
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        notesArea = new JTextArea(3, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        formPanel.add(notesScroll, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveReferral());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadPatients() {
        List<Patient> patients = patientController.getAllPatients();
        for (Patient patient : patients) {
            patientCombo.addItem(new PatientItem(patient));
        }
    }

    private void loadClinicians(JComboBox<ClinicianItem> combo) {
        List<Clinician> clinicians = clinicianController.getAllClinicians();
        for (Clinician clinician : clinicians) {
            combo.addItem(new ClinicianItem(clinician));
        }
    }

    private void loadFacilities(JComboBox<FacilityItem> combo) {
        List<Facility> facilities = facilityController.getAllFacilities();
        for (Facility facility : facilities) {
            combo.addItem(new FacilityItem(facility));
        }
    }

    private void populateFields() {
        // Select Patient
        if (referral.getPatientId() != null) {
            for (int i = 0; i < patientCombo.getItemCount(); i++) {
                PatientItem item = patientCombo.getItemAt(i);
                if (item.getPatient().getPatientId().equals(referral.getPatientId())) {
                    patientCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select Referring Clinician
        if (referral.getReferringClinicianId() != null) {
            for (int i = 0; i < referringClinicianCombo.getItemCount(); i++) {
                ClinicianItem item = referringClinicianCombo.getItemAt(i);
                if (item.getClinician().getClinicianId().equals(referral.getReferringClinicianId())) {
                    referringClinicianCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select Referred To Clinician
        if (referral.getReferredToClinicianId() != null) {
            for (int i = 0; i < referredToClinicianCombo.getItemCount(); i++) {
                ClinicianItem item = referredToClinicianCombo.getItemAt(i);
                if (item.getClinician().getClinicianId().equals(referral.getReferredToClinicianId())) {
                    referredToClinicianCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select Referring Facility
        if (referral.getReferringFacilityId() != null) {
            for (int i = 0; i < referringFacilityCombo.getItemCount(); i++) {
                FacilityItem item = referringFacilityCombo.getItemAt(i);
                if (item.getFacility().getFacilityId().equals(referral.getReferringFacilityId())) {
                    referringFacilityCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select Referred To Facility
        if (referral.getReferredToFacilityId() != null) {
            for (int i = 0; i < referredToFacilityCombo.getItemCount(); i++) {
                FacilityItem item = referredToFacilityCombo.getItemAt(i);
                if (item.getFacility().getFacilityId().equals(referral.getReferredToFacilityId())) {
                    referredToFacilityCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (referral.getReferralDate() != null) {
            referralDateField.setText(referral.getReferralDate().format(DATE_FORMAT));
        }
        urgencyCombo.setSelectedItem(referral.getUrgencyLevel());
        reasonField.setText(referral.getReferralReason());
        clinicalSummaryArea.setText(referral.getClinicalSummary());
        investigationsField.setText(referral.getRequestedInvestigations());
        statusCombo.setSelectedItem(referral.getStatus());
        notesArea.setText(referral.getNotes());
    }

    private void saveReferral() {
        // Validation
        if (patientCombo.getSelectedItem() == null ||
                referringClinicianCombo.getSelectedItem() == null ||
                referredToClinicianCombo.getSelectedItem() == null ||
                referringFacilityCombo.getSelectedItem() == null ||
                referredToFacilityCombo.getSelectedItem() == null ||
                referralDateField.getText().trim().isEmpty() ||
                reasonField.getText().trim().isEmpty() ||
                clinicalSummaryArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields (marked with *).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Parse date
        LocalDate referralDate;
        try {
            referralDate = LocalDate.parse(referralDateField.getText().trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create or update referral
        if (referral == null) {
            referral = new Referral();
            referral.setReferralId(referralController.getNextReferralId());
            referral.setStatus("New");
            referral.setCreatedDate(LocalDate.now());
        }

        PatientItem selectedPatient = (PatientItem) patientCombo.getSelectedItem();
        ClinicianItem selectedReferringClinician = (ClinicianItem) referringClinicianCombo.getSelectedItem();
        ClinicianItem selectedReferredToClinician = (ClinicianItem) referredToClinicianCombo.getSelectedItem();
        FacilityItem selectedReferringFacility = (FacilityItem) referringFacilityCombo.getSelectedItem();
        FacilityItem selectedReferredToFacility = (FacilityItem) referredToFacilityCombo.getSelectedItem();

        referral.setPatientId(selectedPatient.getPatient().getPatientId());
        referral.setReferringClinicianId(selectedReferringClinician.getClinician().getClinicianId());
        referral.setReferredToClinicianId(selectedReferredToClinician.getClinician().getClinicianId());
        referral.setReferringFacilityId(selectedReferringFacility.getFacility().getFacilityId());
        referral.setReferredToFacilityId(selectedReferredToFacility.getFacility().getFacilityId());
        referral.setReferralDate(referralDate);
        referral.setUrgencyLevel((String) urgencyCombo.getSelectedItem());
        referral.setReferralReason(reasonField.getText().trim());
        referral.setClinicalSummary(clinicalSummaryArea.getText().trim());
        referral.setRequestedInvestigations(investigationsField.getText().trim());
        if (statusCombo.isEnabled()) {
            referral.setStatus((String) statusCombo.getSelectedItem());
        }
        referral.setNotes(notesArea.getText().trim());
        referral.setLastUpdated(LocalDate.now());

        // Save to controller (which uses ReferralManager singleton)
        if (referralController.getReferralById(referral.getReferralId()) != null) {
            // Update existing referral
            referralController.updateReferral(referral);
        } else {
            referralController.addReferral(referral);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    /**
     * Helper class for patient combo box items.
     */
    private static class PatientItem {
        private Patient patient;

        public PatientItem(Patient patient) {
            this.patient = patient;
        }

        public Patient getPatient() {
            return patient;
        }

        @Override
        public String toString() {
            return patient.getFullName() + " (" + patient.getNhsNumber() + ")";
        }
    }

    /**
     * Helper class for clinician combo box items.
     */
    private static class ClinicianItem {
        private Clinician clinician;

        public ClinicianItem(Clinician clinician) {
            this.clinician = clinician;
        }

        public Clinician getClinician() {
            return clinician;
        }

        @Override
        public String toString() {
            return clinician.getTitle() + " " + clinician.getFullName() + " (" + clinician.getSpeciality() + ")";
        }
    }

    /**
     * Helper class for facility combo box items.
     */
    private static class FacilityItem {
        private Facility facility;

        public FacilityItem(Facility facility) {
            this.facility = facility;
        }

        public Facility getFacility() {
            return facility;
        }

        @Override
        public String toString() {
            return facility.getFacilityName();
        }
    }
}
