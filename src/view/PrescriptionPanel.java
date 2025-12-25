package view;

import controller.ClinicianController;
import controller.PatientController;
import controller.PrescriptionController;
import model.Clinician;
import model.Patient;
import model.Prescription;
import view.dialogs.PrescriptionDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Panel for displaying and managing prescriptions.
 * Provides JTable view with filtering and CRUD functionality.
 */
public class PrescriptionPanel extends JPanel {

    private PrescriptionController prescriptionController;
    private PatientController patientController;
    private ClinicianController clinicianController;
    private JTable prescriptionTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private TableRowSorter<DefaultTableModel> sorter;

    private static final String[] COLUMN_NAMES = {
            "ID", "Date", "Patient", "Clinician", "Medication",
            "Dosage", "Frequency", "Pharmacy", "Status"
    };

    public PrescriptionPanel(PrescriptionController prescriptionController,
                             PatientController patientController,
                             ClinicianController clinicianController) {
        this.prescriptionController = prescriptionController;
        this.patientController = patientController;
        this.clinicianController = clinicianController;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createTopPanel();
        createTablePanel();
        createButtonPanel();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));

        JLabel titleLabel = new JLabel("Prescription Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        filterPanel.add(new JLabel("Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{"All", "Issued", "Collected"});
        statusFilterCombo.addActionListener(e -> applyFilter());
        filterPanel.add(statusFilterCombo);

        filterPanel.add(Box.createHorizontalStrut(20));

        filterPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
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

        prescriptionTable = new JTable(tableModel);
        prescriptionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        prescriptionTable.setRowHeight(25);
        prescriptionTable.getTableHeader().setReorderingAllowed(false);

        sorter = new TableRowSorter<>(tableModel);
        prescriptionTable.setRowSorter(sorter);

        prescriptionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editPrescription();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(prescriptionTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton addButton = new JButton("New Prescription");
        addButton.addActionListener(e -> addPrescription());

        JButton editButton = new JButton("Edit Prescription");
        editButton.addActionListener(e -> editPrescription());

        JButton collectButton = new JButton("Mark Collected");
        collectButton.addActionListener(e -> markCollected());

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deletePrescription());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(collectButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void applyFilter() {
        String searchText = searchField.getText().trim().toLowerCase();
        String statusFilter = (String) statusFilterCombo.getSelectedItem();

        RowFilter<DefaultTableModel, Object> searchFilter = null;
        RowFilter<DefaultTableModel, Object> statusRowFilter = null;

        if (!searchText.isEmpty()) {
            searchFilter = RowFilter.regexFilter("(?i)" + searchText);
        }

        if (!"All".equals(statusFilter)) {
            statusRowFilter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    String status = entry.getStringValue(8);
                    return status.equalsIgnoreCase(statusFilter);
                }
            };
        }

        if (searchFilter != null && statusRowFilter != null) {
            sorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(searchFilter, statusRowFilter)));
        } else if (searchFilter != null) {
            sorter.setRowFilter(searchFilter);
        } else if (statusRowFilter != null) {
            sorter.setRowFilter(statusRowFilter);
        } else {
            sorter.setRowFilter(null);
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Prescription> prescriptions = prescriptionController.getAllPrescriptions();

        for (Prescription prescription : prescriptions) {
            String patientName = "";
            if (prescription.getPatientId() != null) {
                Patient patient = patientController.getPatientById(prescription.getPatientId());
                if (patient != null) {
                    patientName = patient.getFullName();
                }
            }

            String clinicianName = "";
            if (prescription.getClinicianId() != null) {
                Clinician clinician = clinicianController.getClinicianById(prescription.getClinicianId());
                if (clinician != null) {
                    clinicianName = clinician.getTitle() + " " + clinician.getFullName();
                }
            }

            Object[] row = {
                    prescription.getPrescriptionId(),
                    prescription.getPrescriptionDate() != null ? prescription.getPrescriptionDate().toString() : "",
                    patientName,
                    clinicianName,
                    prescription.getMedicationName(),
                    prescription.getDosage(),
                    prescription.getFrequency(),
                    prescription.getPharmacyName(),
                    prescription.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void addPrescription() {
        PrescriptionDialog dialog = new PrescriptionDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "New Prescription",
                null,
                prescriptionController,
                patientController,
                clinicianController
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void editPrescription() {
        int selectedRow = prescriptionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a prescription to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = prescriptionTable.convertRowIndexToModel(selectedRow);
        String prescriptionId = (String) tableModel.getValueAt(modelRow, 0);
        Prescription prescription = prescriptionController.getPrescriptionById(prescriptionId);

        if (prescription != null) {
            PrescriptionDialog dialog = new PrescriptionDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Edit Prescription",
                    prescription,
                    prescriptionController,
                    patientController,
                    clinicianController
            );
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                refreshData();
            }
        }
    }

    private void markCollected() {
        int selectedRow = prescriptionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a prescription to mark as collected.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = prescriptionTable.convertRowIndexToModel(selectedRow);
        String prescriptionId = (String) tableModel.getValueAt(modelRow, 0);

        if (prescriptionController.markAsCollected(prescriptionId)) {
            refreshData();
            JOptionPane.showMessageDialog(this,
                    "Prescription marked as collected.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deletePrescription() {
        int selectedRow = prescriptionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a prescription to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = prescriptionTable.convertRowIndexToModel(selectedRow);
        String prescriptionId = (String) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this prescription?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (prescriptionController.deletePrescription(prescriptionId)) {
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "Prescription deleted successfully.",
                        "Delete Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
