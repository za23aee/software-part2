package view;

import controller.FacilityController;
import controller.PatientController;
import model.Facility;
import model.Patient;
import view.dialogs.PatientDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Panel for displaying and managing patients.
 * Provides JTable view with search, add, edit, and delete functionality.
 */
public class PatientPanel extends JPanel {

    private PatientController patientController;
    private FacilityController facilityController;
    private JTable patientTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    private static final String[] COLUMN_NAMES = {
            "ID", "First Name", "Last Name", "DOB", "NHS Number",
            "Gender", "Phone", "Email", "Postcode", "GP Surgery"
    };

    public PatientPanel(PatientController patientController, FacilityController facilityController) {
        this.patientController = patientController;
        this.facilityController = facilityController;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createTopPanel();
        createTablePanel();
        createButtonPanel();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));

        JLabel titleLabel = new JLabel("Patient Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterTable();
            }
        });
        searchPanel.add(searchField);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            filterTable();
        });
        searchPanel.add(clearButton);

        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void createTablePanel() {
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        patientTable = new JTable(tableModel);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setRowHeight(25);
        patientTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        patientTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        patientTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        patientTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        patientTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        patientTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        patientTable.getColumnModel().getColumn(5).setPreferredWidth(60);
        patientTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        patientTable.getColumnModel().getColumn(7).setPreferredWidth(180);
        patientTable.getColumnModel().getColumn(8).setPreferredWidth(80);
        patientTable.getColumnModel().getColumn(9).setPreferredWidth(150);

        sorter = new TableRowSorter<>(tableModel);
        patientTable.setRowSorter(sorter);

        // Double-click to edit
        patientTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editPatient();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(patientTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton addButton = new JButton("Add Patient");
        addButton.addActionListener(e -> addPatient());

        JButton editButton = new JButton("Edit Patient");
        editButton.addActionListener(e -> editPatient());

        JButton deleteButton = new JButton("Delete Patient");
        deleteButton.addActionListener(e -> deletePatient());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Patient> patients = patientController.getAllPatients();

        for (Patient patient : patients) {
            String gpSurgeryName = "";
            if (patient.getGpSurgeryId() != null) {
                Facility facility = facilityController.getFacilityById(patient.getGpSurgeryId());
                if (facility != null) {
                    gpSurgeryName = facility.getFacilityName();
                }
            }

            Object[] row = {
                    patient.getPatientId(),
                    patient.getFirstName(),
                    patient.getLastName(),
                    patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : "",
                    patient.getNhsNumber(),
                    patient.getGender(),
                    patient.getPhoneNumber(),
                    patient.getEmail(),
                    patient.getPostcode(),
                    gpSurgeryName
            };
            tableModel.addRow(row);
        }
    }

    private void addPatient() {
        PatientDialog dialog = new PatientDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Add New Patient",
                null,
                patientController,
                facilityController
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void editPatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a patient to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = patientTable.convertRowIndexToModel(selectedRow);
        String patientId = (String) tableModel.getValueAt(modelRow, 0);
        Patient patient = patientController.getPatientById(patientId);

        if (patient != null) {
            PatientDialog dialog = new PatientDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Edit Patient",
                    patient,
                    patientController,
                    facilityController
            );
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                refreshData();
            }
        }
    }

    private void deletePatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a patient to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = patientTable.convertRowIndexToModel(selectedRow);
        String patientId = (String) tableModel.getValueAt(modelRow, 0);
        String patientName = tableModel.getValueAt(modelRow, 1) + " " + tableModel.getValueAt(modelRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete patient: " + patientName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (patientController.deletePatient(patientId)) {
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "Patient deleted successfully.",
                        "Delete Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete patient.",
                        "Delete Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
