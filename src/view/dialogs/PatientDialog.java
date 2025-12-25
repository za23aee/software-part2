package view.dialogs;

import controller.FacilityController;
import controller.PatientController;
import model.Facility;
import model.Patient;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Dialog for adding or editing a patient.
 */
public class PatientDialog extends JDialog {

    private PatientController patientController;
    private FacilityController facilityController;
    private Patient patient;
    private boolean saved = false;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField dobField;
    private JTextField nhsNumberField;
    private JComboBox<String> genderCombo;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;
    private JTextField postcodeField;
    private JTextField emergencyNameField;
    private JTextField emergencyPhoneField;
    private JComboBox<FacilityItem> gpSurgeryCombo;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PatientDialog(Frame parent, String title, Patient patient,
                         PatientController patientController, FacilityController facilityController) {
        super(parent, title, true);
        this.patient = patient;
        this.patientController = patientController;
        this.facilityController = facilityController;

        initComponents();
        if (patient != null) {
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

        // First Name
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("First Name:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        firstNameField = new JTextField(20);
        formPanel.add(firstNameField, gbc);

        // Last Name
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Last Name:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        lastNameField = new JTextField(20);
        formPanel.add(lastNameField, gbc);

        // Date of Birth
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        dobField = new JTextField(20);
        formPanel.add(dobField, gbc);

        // NHS Number
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("NHS Number:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        nhsNumberField = new JTextField(20);
        formPanel.add(nhsNumberField, gbc);

        // Gender
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Gender:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        genderCombo = new JComboBox<>(new String[]{"M", "F", "Other"});
        formPanel.add(genderCombo, gbc);

        // Phone
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Phone Number:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        phoneField = new JTextField(20);
        formPanel.add(phoneField, gbc);

        // Email
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Email:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        emailField = new JTextField(20);
        formPanel.add(emailField, gbc);

        // Address
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        addressField = new JTextField(20);
        formPanel.add(addressField, gbc);

        // Postcode
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Postcode:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        postcodeField = new JTextField(20);
        formPanel.add(postcodeField, gbc);

        // Emergency Contact Name
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Emergency Contact Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        emergencyNameField = new JTextField(20);
        formPanel.add(emergencyNameField, gbc);

        // Emergency Contact Phone
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Emergency Contact Phone:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        emergencyPhoneField = new JTextField(20);
        formPanel.add(emergencyPhoneField, gbc);

        // GP Surgery
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("GP Surgery:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gpSurgeryCombo = new JComboBox<>();
        loadGPSurgeries();
        formPanel.add(gpSurgeryCombo, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> savePatient());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadGPSurgeries() {
        List<Facility> surgeries = facilityController.getGPSurgeries();
        for (Facility facility : surgeries) {
            gpSurgeryCombo.addItem(new FacilityItem(facility));
        }
    }

    private void populateFields() {
        firstNameField.setText(patient.getFirstName());
        lastNameField.setText(patient.getLastName());
        if (patient.getDateOfBirth() != null) {
            dobField.setText(patient.getDateOfBirth().format(DATE_FORMAT));
        }
        nhsNumberField.setText(patient.getNhsNumber());
        genderCombo.setSelectedItem(patient.getGender());
        phoneField.setText(patient.getPhoneNumber());
        emailField.setText(patient.getEmail());
        addressField.setText(patient.getAddress());
        postcodeField.setText(patient.getPostcode());
        emergencyNameField.setText(patient.getEmergencyContactName());
        emergencyPhoneField.setText(patient.getEmergencyContactPhone());

        // Select GP Surgery
        String gpSurgeryId = patient.getGpSurgeryId();
        if (gpSurgeryId != null) {
            for (int i = 0; i < gpSurgeryCombo.getItemCount(); i++) {
                FacilityItem item = gpSurgeryCombo.getItemAt(i);
                if (item.getFacility().getFacilityId().equals(gpSurgeryId)) {
                    gpSurgeryCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void savePatient() {
        // Validation
        if (firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                dobField.getText().trim().isEmpty() ||
                nhsNumberField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                postcodeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields (marked with *).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Parse date
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobField.getText().trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create or update patient
        if (patient == null) {
            patient = new Patient();
            patient.setPatientId(patientController.getNextPatientId());
            patient.setRegistrationDate(LocalDate.now());
        }

        patient.setFirstName(firstNameField.getText().trim());
        patient.setLastName(lastNameField.getText().trim());
        patient.setDateOfBirth(dob);
        patient.setNhsNumber(nhsNumberField.getText().trim());
        patient.setGender((String) genderCombo.getSelectedItem());
        patient.setPhoneNumber(phoneField.getText().trim());
        patient.setEmail(emailField.getText().trim());
        patient.setAddress(addressField.getText().trim());
        patient.setPostcode(postcodeField.getText().trim());
        patient.setEmergencyContactName(emergencyNameField.getText().trim());
        patient.setEmergencyContactPhone(emergencyPhoneField.getText().trim());

        FacilityItem selectedSurgery = (FacilityItem) gpSurgeryCombo.getSelectedItem();
        if (selectedSurgery != null) {
            patient.setGpSurgeryId(selectedSurgery.getFacility().getFacilityId());
        }

        // Save to controller
        if (patientController.getPatientById(patient.getPatientId()) != null) {
            patientController.updatePatient(patient);
        } else {
            patientController.addPatient(patient);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
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
