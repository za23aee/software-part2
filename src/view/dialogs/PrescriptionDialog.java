package view.dialogs;

import controller.ClinicianController;
import controller.PatientController;
import controller.PrescriptionController;
import model.Clinician;
import model.Patient;
import model.Prescription;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Dialog for adding or editing a prescription.
 */
public class PrescriptionDialog extends JDialog {

    private PrescriptionController prescriptionController;
    private PatientController patientController;
    private ClinicianController clinicianController;
    private Prescription prescription;
    private boolean saved = false;

    private JComboBox<PatientItem> patientCombo;
    private JComboBox<ClinicianItem> clinicianCombo;
    private JTextField dateField;
    private JTextField medicationField;
    private JTextField dosageField;
    private JTextField frequencyField;
    private JSpinner durationSpinner;
    private JTextField quantityField;
    private JTextArea instructionsArea;
    private JTextField pharmacyField;
    private JComboBox<String> statusCombo;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PrescriptionDialog(Frame parent, String title, Prescription prescription,
                              PrescriptionController prescriptionController,
                              PatientController patientController,
                              ClinicianController clinicianController) {
        super(parent, title, true);
        this.prescription = prescription;
        this.prescriptionController = prescriptionController;
        this.patientController = patientController;
        this.clinicianController = clinicianController;

        initComponents();
        if (prescription != null) {
            populateFields();
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

        // Clinician
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Prescribing Clinician:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        clinicianCombo = new JComboBox<>();
        loadClinicians();
        formPanel.add(clinicianCombo, gbc);

        // Date
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        dateField = new JTextField(15);
        dateField.setText(LocalDate.now().format(DATE_FORMAT));
        formPanel.add(dateField, gbc);

        // Medication
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Medication Name:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        medicationField = new JTextField(20);
        formPanel.add(medicationField, gbc);

        // Dosage
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Dosage:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        dosageField = new JTextField(20);
        formPanel.add(dosageField, gbc);

        // Frequency
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Frequency:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        frequencyField = new JTextField(20);
        formPanel.add(frequencyField, gbc);

        // Duration
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Duration (days):*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        durationSpinner = new JSpinner(new SpinnerNumberModel(28, 1, 365, 1));
        formPanel.add(durationSpinner, gbc);

        // Quantity
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Quantity:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        quantityField = new JTextField(20);
        formPanel.add(quantityField, gbc);

        // Instructions
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Instructions:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        instructionsArea = new JTextArea(3, 20);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(instructionsArea), gbc);

        // Pharmacy
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Pharmacy:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        pharmacyField = new JTextField(20);
        formPanel.add(pharmacyField, gbc);

        // Status
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Status:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        statusCombo = new JComboBox<>(new String[]{"Issued", "Collected"});
        formPanel.add(statusCombo, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> savePrescription());
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

    private void loadClinicians() {
        List<Clinician> clinicians = clinicianController.getAllClinicians();
        for (Clinician clinician : clinicians) {
            clinicianCombo.addItem(new ClinicianItem(clinician));
        }
    }

    private void populateFields() {
        // Select patient
        if (prescription.getPatientId() != null) {
            for (int i = 0; i < patientCombo.getItemCount(); i++) {
                if (patientCombo.getItemAt(i).getPatient().getPatientId().equals(prescription.getPatientId())) {
                    patientCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select clinician
        if (prescription.getClinicianId() != null) {
            for (int i = 0; i < clinicianCombo.getItemCount(); i++) {
                if (clinicianCombo.getItemAt(i).getClinician().getClinicianId().equals(prescription.getClinicianId())) {
                    clinicianCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (prescription.getPrescriptionDate() != null) {
            dateField.setText(prescription.getPrescriptionDate().format(DATE_FORMAT));
        }
        medicationField.setText(prescription.getMedicationName());
        dosageField.setText(prescription.getDosage());
        frequencyField.setText(prescription.getFrequency());
        durationSpinner.setValue(prescription.getDurationDays());
        quantityField.setText(prescription.getQuantity());
        instructionsArea.setText(prescription.getInstructions());
        pharmacyField.setText(prescription.getPharmacyName());
        statusCombo.setSelectedItem(prescription.getStatus());
    }

    private void savePrescription() {
        // Validation
        if (medicationField.getText().trim().isEmpty() ||
                dosageField.getText().trim().isEmpty() ||
                frequencyField.getText().trim().isEmpty() ||
                quantityField.getText().trim().isEmpty() ||
                instructionsArea.getText().trim().isEmpty() ||
                pharmacyField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (prescription == null) {
            prescription = new Prescription();
            prescription.setPrescriptionId(prescriptionController.getNextPrescriptionId());
            prescription.setIssueDate(LocalDate.now());
        }

        PatientItem selectedPatient = (PatientItem) patientCombo.getSelectedItem();
        ClinicianItem selectedClinician = (ClinicianItem) clinicianCombo.getSelectedItem();

        prescription.setPatientId(selectedPatient.getPatient().getPatientId());
        prescription.setClinicianId(selectedClinician.getClinician().getClinicianId());
        prescription.setPrescriptionDate(date);
        prescription.setMedicationName(medicationField.getText().trim());
        prescription.setDosage(dosageField.getText().trim());
        prescription.setFrequency(frequencyField.getText().trim());
        prescription.setDurationDays((Integer) durationSpinner.getValue());
        prescription.setQuantity(quantityField.getText().trim());
        prescription.setInstructions(instructionsArea.getText().trim());
        prescription.setPharmacyName(pharmacyField.getText().trim());
        prescription.setStatus((String) statusCombo.getSelectedItem());

        if (prescriptionController.getPrescriptionById(prescription.getPrescriptionId()) != null) {
            prescriptionController.updatePrescription(prescription);
        } else {
            prescriptionController.addPrescription(prescription);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    // Helper classes
    private static class PatientItem {
        private Patient patient;
        public PatientItem(Patient patient) { this.patient = patient; }
        public Patient getPatient() { return patient; }
        @Override public String toString() { return patient.getFullName() + " (" + patient.getNhsNumber() + ")"; }
    }

    private static class ClinicianItem {
        private Clinician clinician;
        public ClinicianItem(Clinician clinician) { this.clinician = clinician; }
        public Clinician getClinician() { return clinician; }
        @Override public String toString() { return clinician.getTitle() + " " + clinician.getFullName(); }
    }
}
