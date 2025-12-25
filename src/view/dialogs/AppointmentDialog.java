package view.dialogs;

import controller.AppointmentController;
import controller.ClinicianController;
import controller.FacilityController;
import controller.PatientController;
import model.Appointment;
import model.Clinician;
import model.Facility;
import model.Patient;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Dialog for adding or editing an appointment.
 */
public class AppointmentDialog extends JDialog {

    private AppointmentController appointmentController;
    private PatientController patientController;
    private ClinicianController clinicianController;
    private FacilityController facilityController;
    private Appointment appointment;
    private boolean saved = false;

    private JComboBox<PatientItem> patientCombo;
    private JComboBox<ClinicianItem> clinicianCombo;
    private JComboBox<FacilityItem> facilityCombo;
    private JTextField dateField;
    private JTextField timeField;
    private JSpinner durationSpinner;
    private JComboBox<String> typeCombo;
    private JComboBox<String> statusCombo;
    private JTextField reasonField;
    private JTextArea notesArea;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public AppointmentDialog(Frame parent, String title, Appointment appointment,
                             AppointmentController appointmentController,
                             PatientController patientController,
                             ClinicianController clinicianController,
                             FacilityController facilityController) {
        super(parent, title, true);
        this.appointment = appointment;
        this.appointmentController = appointmentController;
        this.patientController = patientController;
        this.clinicianController = clinicianController;
        this.facilityController = facilityController;

        initComponents();
        if (appointment != null) {
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
        formPanel.add(new JLabel("Clinician:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        clinicianCombo = new JComboBox<>();
        loadClinicians();
        formPanel.add(clinicianCombo, gbc);

        // Facility
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Facility:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        facilityCombo = new JComboBox<>();
        loadFacilities();
        formPanel.add(facilityCombo, gbc);

        // Date
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        dateField = new JTextField(15);
        formPanel.add(dateField, gbc);

        // Time
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Time (HH:MM):*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        timeField = new JTextField(15);
        formPanel.add(timeField, gbc);

        // Duration
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Duration (minutes):*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        durationSpinner = new JSpinner(new SpinnerNumberModel(15, 5, 120, 5));
        formPanel.add(durationSpinner, gbc);

        // Type
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Type:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        typeCombo = new JComboBox<>(new String[]{
                "Routine Consultation", "Follow-up", "Urgent Consultation",
                "Vaccination", "Health Check", "Specialist Consultation", "Emergency"
        });
        formPanel.add(typeCombo, gbc);

        // Status
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Status:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        statusCombo = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled"});
        formPanel.add(statusCombo, gbc);

        // Reason
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Reason for Visit:*"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        reasonField = new JTextField(20);
        formPanel.add(reasonField, gbc);

        // Notes
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(notesArea), gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveAppointment());
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

    private void loadFacilities() {
        List<Facility> facilities = facilityController.getAllFacilities();
        for (Facility facility : facilities) {
            facilityCombo.addItem(new FacilityItem(facility));
        }
    }

    private void populateFields() {
        // Select patient
        if (appointment.getPatientId() != null) {
            for (int i = 0; i < patientCombo.getItemCount(); i++) {
                if (patientCombo.getItemAt(i).getPatient().getPatientId().equals(appointment.getPatientId())) {
                    patientCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select clinician
        if (appointment.getClinicianId() != null) {
            for (int i = 0; i < clinicianCombo.getItemCount(); i++) {
                if (clinicianCombo.getItemAt(i).getClinician().getClinicianId().equals(appointment.getClinicianId())) {
                    clinicianCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Select facility
        if (appointment.getFacilityId() != null) {
            for (int i = 0; i < facilityCombo.getItemCount(); i++) {
                if (facilityCombo.getItemAt(i).getFacility().getFacilityId().equals(appointment.getFacilityId())) {
                    facilityCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (appointment.getAppointmentDate() != null) {
            dateField.setText(appointment.getAppointmentDate().format(DATE_FORMAT));
        }
        if (appointment.getAppointmentTime() != null) {
            timeField.setText(appointment.getAppointmentTime().format(TIME_FORMAT));
        }
        durationSpinner.setValue(appointment.getDurationMinutes());
        typeCombo.setSelectedItem(appointment.getAppointmentType());
        statusCombo.setSelectedItem(appointment.getStatus());
        reasonField.setText(appointment.getReasonForVisit());
        notesArea.setText(appointment.getNotes());
    }

    private void saveAppointment() {
        // Validation
        if (dateField.getText().trim().isEmpty() ||
                timeField.getText().trim().isEmpty() ||
                reasonField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(dateField.getText().trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            time = LocalTime.parse(timeField.getText().trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid time format. Please use HH:MM.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (appointment == null) {
            appointment = new Appointment();
            appointment.setAppointmentId(appointmentController.getNextAppointmentId());
            appointment.setCreatedDate(LocalDate.now());
        }

        PatientItem selectedPatient = (PatientItem) patientCombo.getSelectedItem();
        ClinicianItem selectedClinician = (ClinicianItem) clinicianCombo.getSelectedItem();
        FacilityItem selectedFacility = (FacilityItem) facilityCombo.getSelectedItem();

        appointment.setPatientId(selectedPatient.getPatient().getPatientId());
        appointment.setClinicianId(selectedClinician.getClinician().getClinicianId());
        appointment.setFacilityId(selectedFacility.getFacility().getFacilityId());
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setDurationMinutes((Integer) durationSpinner.getValue());
        appointment.setAppointmentType((String) typeCombo.getSelectedItem());
        appointment.setStatus((String) statusCombo.getSelectedItem());
        appointment.setReasonForVisit(reasonField.getText().trim());
        appointment.setNotes(notesArea.getText().trim());
        appointment.setLastModified(LocalDate.now());

        if (appointmentController.getAppointmentById(appointment.getAppointmentId()) != null) {
            appointmentController.updateAppointment(appointment);
        } else {
            appointmentController.addAppointment(appointment);
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

    private static class FacilityItem {
        private Facility facility;
        public FacilityItem(Facility facility) { this.facility = facility; }
        public Facility getFacility() { return facility; }
        @Override public String toString() { return facility.getFacilityName(); }
    }
}
