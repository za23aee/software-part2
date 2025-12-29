package view;

import controller.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Main application frame for the Healthcare Management System.
 * Uses JTabbedPane for navigation between different views.
 * Implements MVC pattern as the main View component.
 */
public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private JLabel statusLabel;

    // Controllers
    private PatientController patientController;
    private ClinicianController clinicianController;
    private FacilityController facilityController;
    private AppointmentController appointmentController;
    private PrescriptionController prescriptionController;
    private ReferralController referralController;
    private StaffController staffController;

    // Panels
    private PatientPanel patientPanel;
    private ClinicianPanel clinicianPanel;
    private FacilityPanel facilityPanel;
    private AppointmentPanel appointmentPanel;
    private PrescriptionPanel prescriptionPanel;
    private ReferralPanel referralPanel;
    private StaffPanel staffPanel;

    public MainFrame() {
        initializeControllers();
        initializeFrame();
        createMenuBar();
        createTabbedPane();
        createStatusBar();
        loadAllData();
    }

    private void initializeControllers() {
        patientController = new PatientController();
        clinicianController = new ClinicianController();
        facilityController = new FacilityController();
        appointmentController = new AppointmentController();
        prescriptionController = new PrescriptionController();
        referralController = new ReferralController();
        staffController = new StaffController();
    }

    private void initializeFrame() {
        setTitle("Healthcare Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadDataItem = new JMenuItem("Reload Data");
        JMenuItem saveDataItem = new JMenuItem("Save All Data");
        JMenuItem exitItem = new JMenuItem("Exit");

        loadDataItem.addActionListener(e -> loadAllData());
        saveDataItem.addActionListener(e -> saveAllData());
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(loadDataItem);
        fileMenu.add(saveDataItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();

        // Create panels with their controllers
        patientPanel = new PatientPanel(patientController, facilityController);
        clinicianPanel = new ClinicianPanel(clinicianController, facilityController);
        facilityPanel = new FacilityPanel(facilityController);
        appointmentPanel = new AppointmentPanel(appointmentController, patientController,
                                                 clinicianController, facilityController);
        prescriptionPanel = new PrescriptionPanel(prescriptionController, patientController,
                                                   clinicianController);
        referralPanel = new ReferralPanel(referralController, patientController,
                                          clinicianController, facilityController);
        staffPanel = new StaffPanel(staffController, facilityController);

        tabbedPane.addTab("Patients", patientPanel);
        tabbedPane.addTab("Clinicians", clinicianPanel);
        tabbedPane.addTab("Facilities", facilityPanel);
        tabbedPane.addTab("Appointments", appointmentPanel);
        tabbedPane.addTab("Prescriptions", prescriptionPanel);
        tabbedPane.addTab("Referrals", referralPanel);
        tabbedPane.addTab("Staff", staffPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel(" Ready");
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void loadAllData() {
        setStatus("Loading data...");
        try {
            patientController.loadFromCSV();
            clinicianController.loadFromCSV();
            facilityController.loadFromCSV();
            appointmentController.loadFromCSV();
            prescriptionController.loadFromCSV();
            referralController.loadFromCSV();
            staffController.loadFromCSV();

            // Refresh all panels
            refreshAllPanels();

            setStatus("Data loaded successfully. Patients: " + patientController.getPatientCount() +
                    ", Clinicians: " + clinicianController.getClinicianCount() +
                    ", Appointments: " + appointmentController.getAppointmentCount());
        } catch (IOException e) {
            setStatus("Error loading data: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error loading data: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAllData() {
        setStatus("Saving data...");
        try {
            patientController.saveToCSV();
            clinicianController.saveToCSV();
            facilityController.saveToCSV();
            appointmentController.saveToCSV();
            prescriptionController.saveToCSV();
            referralController.saveToCSV();
            staffController.saveToCSV();

            setStatus("All data saved successfully.");
            JOptionPane.showMessageDialog(this,
                    "All data saved successfully.",
                    "Save Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            setStatus("Error saving data: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error saving data: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAllPanels() {
        patientPanel.refreshData();
        clinicianPanel.refreshData();
        facilityPanel.refreshData();
        appointmentPanel.refreshData();
        prescriptionPanel.refreshData();
        referralPanel.refreshData();
        staffPanel.refreshData();
    }

    public void setStatus(String message) {
        statusLabel.setText(" " + message);
    }

    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Do you want to save changes before exiting?",
                "Exit Application",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            saveAllData();
            System.exit(0);
        } else if (choice == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
        // Cancel - do nothing
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Healthcare Management System\n" +
                        "Version 1.0\n\n" +
                        "A Java Swing application implementing:\n" +
                        "- MVC Architecture Pattern\n" +
                        "- Singleton Design Pattern for Referral Management\n\n" +
                        "Features:\n" +
                        "- Patient Management\n" +
                        "- Clinician Management\n" +
                        "- Appointment Scheduling\n" +
                        "- Prescription Management\n" +
                        "- Referral Processing\n\n" +
                        "Software Architecture Assignment - Part 2",
                "About Healthcare Management System",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Getter methods for controllers (used by panels if needed)
    public PatientController getPatientController() {
        return patientController;
    }

    public ClinicianController getClinicianController() {
        return clinicianController;
    }

    public FacilityController getFacilityController() {
        return facilityController;
    }

    public AppointmentController getAppointmentController() {
        return appointmentController;
    }

    public PrescriptionController getPrescriptionController() {
        return prescriptionController;
    }

    public ReferralController getReferralController() {
        return referralController;
    }

    public StaffController getStaffController() {
        return staffController;
    }
}
