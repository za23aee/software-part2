package view;

import controller.FacilityController;
import model.Facility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Panel for displaying and managing facilities (GP Surgeries and Hospitals).
 * Provides JTable view with filtering and display functionality.
 */
public class FacilityPanel extends JPanel {

    private FacilityController facilityController;
    private JTable facilityTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private TableRowSorter<DefaultTableModel> sorter;

    private static final String[] COLUMN_NAMES = {
            "ID", "Name", "Type", "Address", "Postcode",
            "Phone", "Email", "Manager", "Capacity"
    };

    public FacilityPanel(FacilityController facilityController) {
        this.facilityController = facilityController;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createTopPanel();
        createTablePanel();
        createButtonPanel();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));

        JLabel titleLabel = new JLabel("Facility Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Filter by type
        filterPanel.add(new JLabel("Filter:"));
        filterCombo = new JComboBox<>(new String[]{"All", "GP Surgeries", "Hospitals"});
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

        facilityTable = new JTable(tableModel);
        facilityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        facilityTable.setRowHeight(25);
        facilityTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        facilityTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        facilityTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        facilityTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        facilityTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        facilityTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        facilityTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        facilityTable.getColumnModel().getColumn(6).setPreferredWidth(200);
        facilityTable.getColumnModel().getColumn(7).setPreferredWidth(150);
        facilityTable.getColumnModel().getColumn(8).setPreferredWidth(70);

        sorter = new TableRowSorter<>(tableModel);
        facilityTable.setRowSorter(sorter);

        // Double-click to view details
        facilityTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewFacilityDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(facilityTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton viewButton = new JButton("View Details");
        viewButton.addActionListener(e -> viewFacilityDetails());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        buttonPanel.add(viewButton);
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
            final String facilityType;
            switch (filterType) {
                case "GP Surgeries":
                    facilityType = "GP Surgery";
                    break;
                case "Hospitals":
                    facilityType = "Hospital";
                    break;
                default:
                    facilityType = null;
            }

            if (facilityType != null) {
                typeFilter = new RowFilter<DefaultTableModel, Object>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                        String type = entry.getStringValue(2);
                        return type.equalsIgnoreCase(facilityType);
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
        List<Facility> facilities = facilityController.getAllFacilities();

        for (Facility facility : facilities) {
            Object[] row = {
                    facility.getFacilityId(),
                    facility.getFacilityName(),
                    facility.getFacilityType(),
                    facility.getAddress(),
                    facility.getPostcode(),
                    facility.getPhoneNumber(),
                    facility.getEmail(),
                    facility.getManagerName(),
                    facility.getCapacity()
            };
            tableModel.addRow(row);
        }
    }

    private void viewFacilityDetails() {
        int selectedRow = facilityTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a facility to view.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = facilityTable.convertRowIndexToModel(selectedRow);
        String facilityId = (String) tableModel.getValueAt(modelRow, 0);
        Facility facility = facilityController.getFacilityById(facilityId);

        if (facility != null) {
            StringBuilder details = new StringBuilder();
            details.append("Facility Details\n\n");
            details.append("ID: ").append(facility.getFacilityId()).append("\n");
            details.append("Name: ").append(facility.getFacilityName()).append("\n");
            details.append("Type: ").append(facility.getFacilityType()).append("\n");
            details.append("Address: ").append(facility.getAddress()).append("\n");
            details.append("Postcode: ").append(facility.getPostcode()).append("\n");
            details.append("Phone: ").append(facility.getPhoneNumber()).append("\n");
            details.append("Email: ").append(facility.getEmail()).append("\n");
            details.append("Opening Hours: ").append(facility.getOpeningHours()).append("\n");
            details.append("Manager: ").append(facility.getManagerName()).append("\n");
            details.append("Capacity: ").append(facility.getCapacity()).append("\n");
            details.append("Specialities: ").append(facility.getSpecialitiesOffered()).append("\n");

            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JOptionPane.showMessageDialog(this,
                    new JScrollPane(textArea),
                    "Facility Details - " + facility.getFacilityName(),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
