package view;

import controller.ClinicianController;
import controller.FacilityController;
import model.Clinician;
import model.Facility;
import view.dialogs.ClinicianDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Panel for displaying and managing clinicians.
 * Provides JTable view with filtering by type and CRUD functionality.
 */
public class ClinicianPanel extends JPanel {

    private ClinicianController clinicianController;
    private FacilityController facilityController;
    private JTable clinicianTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private TableRowSorter<DefaultTableModel> sorter;

    private static final String[] COLUMN_NAMES = {
            "ID", "Title", "First Name", "Last Name", "Speciality",
            "GMC Number", "Phone", "Email", "Workplace", "Status"
    };

    public ClinicianPanel(ClinicianController clinicianController, FacilityController facilityController) {
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

        JLabel titleLabel = new JLabel("Clinician Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Filter by type
        filterPanel.add(new JLabel("Filter:"));
        filterCombo = new JComboBox<>(new String[]{"All", "GPs", "Specialists", "Nurses"});
        filterCombo.addActionListener(e -> applyFilter());
        filterPanel.add(filterCombo);

        filterPanel.add(Box.createHorizontalStrut(20));

        // Search
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
            filterCombo.setSelectedIndex(0);
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

        clinicianTable = new JTable(tableModel);
        clinicianTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clinicianTable.setRowHeight(25);
        clinicianTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        clinicianTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        clinicianTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        clinicianTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        clinicianTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        clinicianTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        clinicianTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        clinicianTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        clinicianTable.getColumnModel().getColumn(7).setPreferredWidth(180);
        clinicianTable.getColumnModel().getColumn(8).setPreferredWidth(150);
        clinicianTable.getColumnModel().getColumn(9).setPreferredWidth(80);

        sorter = new TableRowSorter<>(tableModel);
        clinicianTable.setRowSorter(sorter);

        // Double-click to edit
        clinicianTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editClinician();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(clinicianTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton addButton = new JButton("Add Clinician");
        addButton.addActionListener(e -> addClinician());

        JButton editButton = new JButton("Edit Clinician");
        editButton.addActionListener(e -> editClinician());

        JButton deleteButton = new JButton("Delete Clinician");
        deleteButton.addActionListener(e -> deleteClinician());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void applyFilter() {
        String searchText = searchField.getText().trim().toLowerCase();
        String filterType = (String) filterCombo.getSelectedItem();

        RowFilter<DefaultTableModel, Object> searchFilter = null;
        RowFilter<DefaultTableModel, Object> typeFilter = null;

        if (!searchText.isEmpty()) {
            searchFilter = RowFilter.regexFilter("(?i)" + searchText);
        }

        if (!"All".equals(filterType)) {
            final String titleFilter;
            switch (filterType) {
                case "GPs":
                    titleFilter = "GP";
                    break;
                case "Specialists":
                    titleFilter = "Consultant";
                    break;
                case "Nurses":
                    titleFilter = "Nurse";
                    break;
                default:
                    titleFilter = null;
            }

            if (titleFilter != null) {
                typeFilter = new RowFilter<DefaultTableModel, Object>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                        String title = entry.getStringValue(1).toLowerCase();
                        return title.contains(titleFilter.toLowerCase());
                    }
                };
            }
        }

        if (searchFilter != null && typeFilter != null) {
            sorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(searchFilter, typeFilter)));
        } else if (searchFilter != null) {
            sorter.setRowFilter(searchFilter);
        } else if (typeFilter != null) {
            sorter.setRowFilter(typeFilter);
        } else {
            sorter.setRowFilter(null);
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Clinician> clinicians = clinicianController.getAllClinicians();

        for (Clinician clinician : clinicians) {
            String workplaceName = "";
            if (clinician.getWorkplaceId() != null) {
                Facility facility = facilityController.getFacilityById(clinician.getWorkplaceId());
                if (facility != null) {
                    workplaceName = facility.getFacilityName();
                }
            }

            Object[] row = {
                    clinician.getClinicianId(),
                    clinician.getTitle(),
                    clinician.getFirstName(),
                    clinician.getLastName(),
                    clinician.getSpeciality(),
                    clinician.getGmcNumber(),
                    clinician.getPhoneNumber(),
                    clinician.getEmail(),
                    workplaceName,
                    clinician.getEmploymentStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void addClinician() {
        ClinicianDialog dialog = new ClinicianDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Add New Clinician",
                null,
                clinicianController,
                facilityController
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void editClinician() {
        int selectedRow = clinicianTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a clinician to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = clinicianTable.convertRowIndexToModel(selectedRow);
        String clinicianId = (String) tableModel.getValueAt(modelRow, 0);
        Clinician clinician = clinicianController.getClinicianById(clinicianId);

        if (clinician != null) {
            ClinicianDialog dialog = new ClinicianDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Edit Clinician",
                    clinician,
                    clinicianController,
                    facilityController
            );
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                refreshData();
            }
        }
    }

    private void deleteClinician() {
        int selectedRow = clinicianTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a clinician to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = clinicianTable.convertRowIndexToModel(selectedRow);
        String clinicianId = (String) tableModel.getValueAt(modelRow, 0);
        String clinicianName = tableModel.getValueAt(modelRow, 1) + " " +
                tableModel.getValueAt(modelRow, 2) + " " + tableModel.getValueAt(modelRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete clinician: " + clinicianName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (clinicianController.deleteClinician(clinicianId)) {
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "Clinician deleted successfully.",
                        "Delete Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete clinician.",
                        "Delete Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
