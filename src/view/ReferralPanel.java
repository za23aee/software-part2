package view;

import controller.ClinicianController;
import controller.FacilityController;
import controller.PatientController;
import controller.ReferralController;
import model.Clinician;
import model.Facility;
import model.Patient;
import model.Referral;
import view.dialogs.ReferralDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Panel for displaying and managing referrals.
 * Uses ReferralManager singleton through ReferralController.
 */
public class ReferralPanel extends JPanel {

    private ReferralController referralController;
    private PatientController patientController;
    private ClinicianController clinicianController;
    private FacilityController facilityController;
    private JTable referralTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> urgencyFilterCombo;
    private TableRowSorter<DefaultTableModel> sorter;

    private static final String[] COLUMN_NAMES = {
            "ID", "Date", "Patient", "From Clinician", "To Clinician",
            "Urgency", "Reason", "Status"
    };

    public ReferralPanel(ReferralController referralController,
                         PatientController patientController,
                         ClinicianController clinicianController,
                         FacilityController facilityController) {
        this.referralController = referralController;
        this.patientController = patientController;
        this.clinicianController = clinicianController;
        this.facilityController = facilityController;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createTopPanel();
        createTablePanel();
        createButtonPanel();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));

        JLabel titleLabel = new JLabel("Referral Management (Singleton Pattern)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        filterPanel.add(new JLabel("Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{"All", "New", "Pending", "In Progress", "Completed"});
        statusFilterCombo.addActionListener(e -> applyFilter());
        filterPanel.add(statusFilterCombo);

        filterPanel.add(new JLabel("Urgency:"));
        urgencyFilterCombo = new JComboBox<>(new String[]{"All", "Urgent", "Routine", "Non-urgent"});
        urgencyFilterCombo.addActionListener(e -> applyFilter());
        filterPanel.add(urgencyFilterCombo);

        filterPanel.add(Box.createHorizontalStrut(10));

        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(12);
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                applyFilter();
            }
        });
        filterPanel.add(searchField);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            urgencyFilterCombo.setSelectedIndex(0);
            applyFilter();
        });
        filterPanel.add(clearButton);

        topPanel.add(filterPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void createTablePanel() {
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        referralTable = new JTable(tableModel);
        referralTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        referralTable.setRowHeight(25);
        referralTable.getTableHeader().setReorderingAllowed(false);

        sorter = new TableRowSorter<>(tableModel);
        referralTable.setRowSorter(sorter);

        referralTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewReferralDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(referralTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton addButton = new JButton("New Referral");
        addButton.addActionListener(e -> addReferral());

        JButton viewButton = new JButton("View Details");
        viewButton.addActionListener(e -> viewReferralDetails());

        JButton generateEmailButton = new JButton("Generate Referral Letter");
        generateEmailButton.addActionListener(e -> generateReferralLetter());

        JButton updateStatusButton = new JButton("Update Status");
        updateStatusButton.addActionListener(e -> updateStatus());

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteReferral());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        buttonPanel.add(addButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(generateEmailButton);
        buttonPanel.add(updateStatusButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void applyFilter() {
        String searchText = searchField.getText().trim().toLowerCase();
        String statusFilter = (String) statusFilterCombo.getSelectedItem();
        String urgencyFilter = (String) urgencyFilterCombo.getSelectedItem();

        RowFilter<DefaultTableModel, Object> combinedFilter = new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                boolean matchesSearch = true;
                boolean matchesStatus = true;
                boolean matchesUrgency = true;

                if (!searchText.isEmpty()) {
                    matchesSearch = false;
                    for (int i = 0; i < entry.getValueCount(); i++) {
                        if (entry.getStringValue(i).toLowerCase().contains(searchText)) {
                            matchesSearch = true;
                            break;
                        }
                    }
                }

                if (!"All".equals(statusFilter)) {
                    matchesStatus = entry.getStringValue(7).equalsIgnoreCase(statusFilter);
                }

                if (!"All".equals(urgencyFilter)) {
                    matchesUrgency = entry.getStringValue(5).equalsIgnoreCase(urgencyFilter);
                }

                return matchesSearch && matchesStatus && matchesUrgency;
            }
        };

        sorter.setRowFilter(combinedFilter);
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Referral> referrals = referralController.getAllReferrals();

        for (Referral referral : referrals) {
            String patientName = "";
            if (referral.getPatientId() != null) {
                Patient patient = patientController.getPatientById(referral.getPatientId());
                if (patient != null) {
                    patientName = patient.getFullName();
                }
            }

            String fromClinician = "";
            if (referral.getReferringClinicianId() != null) {
                Clinician clinician = clinicianController.getClinicianById(referral.getReferringClinicianId());
                if (clinician != null) {
                    fromClinician = clinician.getTitle() + " " + clinician.getLastName();
                }
            }

            String toClinician = "";
            if (referral.getReferredToClinicianId() != null) {
                Clinician clinician = clinicianController.getClinicianById(referral.getReferredToClinicianId());
                if (clinician != null) {
                    toClinician = clinician.getTitle() + " " + clinician.getLastName();
                }
            }

            Object[] row = {
                    referral.getReferralId(),
                    referral.getReferralDate() != null ? referral.getReferralDate().toString() : "",
                    patientName,
                    fromClinician,
                    toClinician,
                    referral.getUrgencyLevel(),
                    referral.getReferralReason(),
                    referral.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void addReferral() {
        ReferralDialog dialog = new ReferralDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "New Referral",
                null,
                referralController,
                patientController,
                clinicianController,
                facilityController
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void viewReferralDetails() {
        int selectedRow = referralTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a referral to view.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = referralTable.convertRowIndexToModel(selectedRow);
        String referralId = (String) tableModel.getValueAt(modelRow, 0);
        Referral referral = referralController.getReferralById(referralId);

        if (referral != null) {
            Patient patient = patientController.getPatientById(referral.getPatientId());
            Clinician fromClinician = clinicianController.getClinicianById(referral.getReferringClinicianId());
            Clinician toClinician = clinicianController.getClinicianById(referral.getReferredToClinicianId());
            Facility fromFacility = facilityController.getFacilityById(referral.getReferringFacilityId());
            Facility toFacility = facilityController.getFacilityById(referral.getReferredToFacilityId());

            StringBuilder details = new StringBuilder();
            details.append("REFERRAL DETAILS\n");
            details.append("================\n\n");
            details.append("Referral ID: ").append(referral.getReferralId()).append("\n");
            details.append("Date: ").append(referral.getReferralDate()).append("\n");
            details.append("Urgency: ").append(referral.getUrgencyLevel()).append("\n");
            details.append("Status: ").append(referral.getStatus()).append("\n\n");

            details.append("PATIENT\n");
            details.append("-------\n");
            if (patient != null) {
                details.append("Name: ").append(patient.getFullName()).append("\n");
                details.append("NHS Number: ").append(patient.getNhsNumber()).append("\n");
            }
            details.append("\n");

            details.append("REFERRING CLINICIAN\n");
            details.append("-------------------\n");
            if (fromClinician != null) {
                details.append("Name: ").append(fromClinician.getFullName()).append("\n");
            }
            if (fromFacility != null) {
                details.append("Facility: ").append(fromFacility.getFacilityName()).append("\n");
            }
            details.append("\n");

            details.append("REFERRED TO\n");
            details.append("-----------\n");
            if (toClinician != null) {
                details.append("Name: ").append(toClinician.getFullName()).append("\n");
                details.append("Speciality: ").append(toClinician.getSpeciality()).append("\n");
            }
            if (toFacility != null) {
                details.append("Facility: ").append(toFacility.getFacilityName()).append("\n");
            }
            details.append("\n");

            details.append("CLINICAL INFORMATION\n");
            details.append("--------------------\n");
            details.append("Reason: ").append(referral.getReferralReason()).append("\n\n");
            details.append("Clinical Summary:\n").append(referral.getClinicalSummary()).append("\n\n");
            details.append("Requested Investigations: ").append(referral.getRequestedInvestigations()).append("\n");
            if (referral.getNotes() != null && !referral.getNotes().isEmpty()) {
                details.append("\nNotes: ").append(referral.getNotes()).append("\n");
            }

            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setCaretPosition(0);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Referral Details - " + referral.getReferralId(),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void generateReferralLetter() {
        int selectedRow = referralTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a referral to generate a letter.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = referralTable.convertRowIndexToModel(selectedRow);
        String referralId = (String) tableModel.getValueAt(modelRow, 0);
        Referral referral = referralController.getReferralById(referralId);

        if (referral != null) {
            try {
                Patient patient = patientController.getPatientById(referral.getPatientId());
                Clinician fromClinician = clinicianController.getClinicianById(referral.getReferringClinicianId());
                Clinician toClinician = clinicianController.getClinicianById(referral.getReferredToClinicianId());
                Facility fromFacility = facilityController.getFacilityById(referral.getReferringFacilityId());
                Facility toFacility = facilityController.getFacilityById(referral.getReferredToFacilityId());

                String filePath = referralController.generateReferralEmail(
                        referral, patient, fromClinician, toClinician, fromFacility, toFacility);

                JOptionPane.showMessageDialog(this,
                        "Referral letter generated successfully!\n\nSaved to:\n" + filePath,
                        "Letter Generated",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error generating referral letter: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateStatus() {
        int selectedRow = referralTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a referral to update.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = referralTable.convertRowIndexToModel(selectedRow);
        String referralId = (String) tableModel.getValueAt(modelRow, 0);

        String[] statuses = {"New", "Pending", "In Progress", "Completed"};
        String newStatus = (String) JOptionPane.showInputDialog(this,
                "Select new status:",
                "Update Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statuses,
                statuses[0]);

        if (newStatus != null) {
            if (referralController.updateStatus(referralId, newStatus)) {
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "Status updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void deleteReferral() {
        int selectedRow = referralTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a referral to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = referralTable.convertRowIndexToModel(selectedRow);
        String referralId = (String) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this referral?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (referralController.deleteReferral(referralId)) {
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "Referral deleted successfully.",
                        "Delete Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
