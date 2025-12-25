package view.dialogs;

import controller.ClinicianController;
import controller.FacilityController;
import model.Clinician;
import model.Facility;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Dialog for adding or editing a clinician.
 */
public class ClinicianDialog extends JDialog {

    private ClinicianController clinicianController;
    private FacilityController facilityController;
    private Clinician clinician;
    private boolean saved = false;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JComboBox<String> titleCombo;
    private JTextField specialityField;
    private JTextField gmcNumberField;
    private JTextField phoneField;
    private JTextField emailField;
    private JComboBox<FacilityItem> workplaceCombo;
    private JComboBox<String> workplaceTypeCombo;
    private JComboBox<String> employmentStatusCombo;
    private JTextField startDateField;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ClinicianDialog(Frame parent, String title, Clinician clinician,
                           ClinicianController clinicianController, FacilityController facilityController) {
        super(parent, title, true);
        this.clinician = clinician;
        this.clinicianController = clinicianController;
        this.facilityController = facilityController;

        initComponents();
        if (clinician != null) {
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

        // Title
        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Title:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        titleCombo = new JComboBox<>(new String[]{"GP", "Consultant", "Senior Nurse", "Nurse", "Practice Nurse", "Staff Nurse"});
        formPanel.add(titleCombo, gbc);

        // First Name
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
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

        // Speciality
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Speciality:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        specialityField = new JTextField(20);
        formPanel.add(specialityField, gbc);

        // GMC Number
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("GMC/NMC Number:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gmcNumberField = new JTextField(20);
        formPanel.add(gmcNumberField, gbc);

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

        // Workplace
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Workplace:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        workplaceCombo = new JComboBox<>();
        loadWorkplaces();
        formPanel.add(workplaceCombo, gbc);

        // Workplace Type
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Workplace Type:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        workplaceTypeCombo = new JComboBox<>(new String[]{"GP Surgery", "Hospital"});
        formPanel.add(workplaceTypeCombo, gbc);

        // Employment Status
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Employment Status:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        employmentStatusCombo = new JComboBox<>(new String[]{"Full-time", "Part-time", "Locum"});
        formPanel.add(employmentStatusCombo, gbc);

        // Start Date
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Start Date (YYYY-MM-DD):*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        startDateField = new JTextField(20);
        formPanel.add(startDateField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveClinician());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadWorkplaces() {
        List<Facility> facilities = facilityController.getAllFacilities();
        for (Facility facility : facilities) {
            workplaceCombo.addItem(new FacilityItem(facility));
        }
    }

    private void populateFields() {
        titleCombo.setSelectedItem(clinician.getTitle());
        firstNameField.setText(clinician.getFirstName());
        lastNameField.setText(clinician.getLastName());
        specialityField.setText(clinician.getSpeciality());
        gmcNumberField.setText(clinician.getGmcNumber());
        phoneField.setText(clinician.getPhoneNumber());
        emailField.setText(clinician.getEmail());
        workplaceTypeCombo.setSelectedItem(clinician.getWorkplaceType());
        employmentStatusCombo.setSelectedItem(clinician.getEmploymentStatus());
        if (clinician.getStartDate() != null) {
            startDateField.setText(clinician.getStartDate().format(DATE_FORMAT));
        }

        // Select Workplace
        String workplaceId = clinician.getWorkplaceId();
        if (workplaceId != null) {
            for (int i = 0; i < workplaceCombo.getItemCount(); i++) {
                FacilityItem item = workplaceCombo.getItemAt(i);
                if (item.getFacility().getFacilityId().equals(workplaceId)) {
                    workplaceCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveClinician() {
        // Validation
        if (firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                specialityField.getText().trim().isEmpty() ||
                gmcNumberField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                startDateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields (marked with *).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Parse date
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(startDateField.getText().trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create or update clinician
        if (clinician == null) {
            clinician = new Clinician();
            clinician.setClinicianId(clinicianController.getNextClinicianId());
        }

        clinician.setTitle((String) titleCombo.getSelectedItem());
        clinician.setFirstName(firstNameField.getText().trim());
        clinician.setLastName(lastNameField.getText().trim());
        clinician.setSpeciality(specialityField.getText().trim());
        clinician.setGmcNumber(gmcNumberField.getText().trim());
        clinician.setPhoneNumber(phoneField.getText().trim());
        clinician.setEmail(emailField.getText().trim());
        clinician.setWorkplaceType((String) workplaceTypeCombo.getSelectedItem());
        clinician.setEmploymentStatus((String) employmentStatusCombo.getSelectedItem());
        clinician.setStartDate(startDate);

        FacilityItem selectedWorkplace = (FacilityItem) workplaceCombo.getSelectedItem();
        if (selectedWorkplace != null) {
            clinician.setWorkplaceId(selectedWorkplace.getFacility().getFacilityId());
        }

        // Save to controller
        if (clinicianController.getClinicianById(clinician.getClinicianId()) != null) {
            clinicianController.updateClinician(clinician);
        } else {
            clinicianController.addClinician(clinician);
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
