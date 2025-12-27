package view;

import controller.FacilityController;
import controller.StaffController;
import model.Facility;
import model.Staff;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Panel for displaying and managing non-clinical staff.
 * Provides JTable view with search and filtering functionality.
 */
public class StaffPanel extends JPanel {

    private StaffController staffController;
    private FacilityController facilityController;
    private JTable staffTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> roleFilterCombo;
    private TableRowSorter<DefaultTableModel> sorter;

    private static final String[] COLUMN_NAMES = {
            "ID", "First Name", "Last Name", "Role", "Department",
            "Facility", "Phone", "Email", "Status"
    };

    public StaffPanel(StaffController staffController, FacilityController facilityController) {
        this.staffController = staffController;
        this.facilityController = facilityController;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createTopPanel();
        createTablePanel();
        createButtonPanel();
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));

        JLabel titleLabel = new JLabel("Staff Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Filter by role
        filterPanel.add(new JLabel("Role:"));
        roleFilterCombo = new JComboBox<>(new String[]{
                "All", "Receptionist", "Administrator", "Practice Manager",
                "Healthcare Assistant", "IT Support", "Finance"
        });
        roleFilterCombo.addActionListener(e -> applyFilter());
        filterPanel.add(roleFilterCombo);

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
            roleFilterCombo.setSelectedIndex(0);
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

        staffTable = new JTable(tableModel);
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        staffTable.setRowHeight(25);
        staffTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        staffTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        staffTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        staffTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        staffTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        staffTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        staffTable.getColumnModel().getColumn(5).setPreferredWidth(180);
        staffTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        staffTable.getColumnModel().getColumn(7).setPreferredWidth(180);
        staffTable.getColumnModel().getColumn(8).setPreferredWidth(80);

        sorter = new TableRowSorter<>(tableModel);
        staffTable.setRowSorter(sorter);

        // Double-click to view details
        staffTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewStaffDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(staffTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton viewButton = new JButton("View Details");
        viewButton.addActionListener(e -> viewStaffDetails());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        buttonPanel.add(viewButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void applyFilter() {
        String searchText = searchField.getText().trim().toLowerCase();
        String filterRole = (String) roleFilterCombo.getSelectedItem();

        RowFilter<DefaultTableModel, Object> searchFilter = null;
        RowFilter<DefaultTableModel, Object> roleFilter = null;

        if (!searchText.isEmpty()) {
            searchFilter = RowFilter.regexFilter("(?i)" + searchText);
        }

        if (!"All".equals(filterRole)) {
            final String role = filterRole;
            roleFilter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    String entryRole = entry.getStringValue(3);
                    return entryRole.equalsIgnoreCase(role);
                }
            };
        }

        if (searchFilter != null && roleFilter != null) {
            sorter.setRowFilter(RowFilter.andFilter(java.util.Arrays.asList(searchFilter, roleFilter)));
        } else if (searchFilter != null) {
            sorter.setRowFilter(searchFilter);
        } else if (roleFilter != null) {
            sorter.setRowFilter(roleFilter);
        } else {
            sorter.setRowFilter(null);
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Staff> staffList = staffController.getAllStaff();

        for (Staff staff : staffList) {
            String facilityName = "";
            if (staff.getFacilityId() != null) {
                Facility facility = facilityController.getFacilityById(staff.getFacilityId());
                if (facility != null) {
                    facilityName = facility.getFacilityName();
                }
            }

            Object[] row = {
                    staff.getStaffId(),
                    staff.getFirstName(),
                    staff.getLastName(),
                    staff.getRole(),
                    staff.getDepartment(),
                    facilityName,
                    staff.getPhoneNumber(),
                    staff.getEmail(),
                    staff.getEmploymentStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void viewStaffDetails() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a staff member to view.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = staffTable.convertRowIndexToModel(selectedRow);
        String staffId = (String) tableModel.getValueAt(modelRow, 0);
        Staff staff = staffController.getStaffById(staffId);

        if (staff != null) {
            String facilityName = "";
            if (staff.getFacilityId() != null) {
                Facility facility = facilityController.getFacilityById(staff.getFacilityId());
                if (facility != null) {
                    facilityName = facility.getFacilityName();
                }
            }

            StringBuilder details = new StringBuilder();
            details.append("Staff Details\n\n");
            details.append("ID: ").append(staff.getStaffId()).append("\n");
            details.append("Name: ").append(staff.getFullName()).append("\n");
            details.append("Role: ").append(staff.getRole()).append("\n");
            details.append("Department: ").append(staff.getDepartment()).append("\n");
            details.append("Facility: ").append(facilityName).append("\n");
            details.append("Phone: ").append(staff.getPhoneNumber()).append("\n");
            details.append("Email: ").append(staff.getEmail()).append("\n");
            details.append("Employment Status: ").append(staff.getEmploymentStatus()).append("\n");
            details.append("Start Date: ").append(staff.getStartDate()).append("\n");
            details.append("Line Manager: ").append(staff.getLineManager()).append("\n");
            details.append("Access Level: ").append(staff.getAccessLevel()).append("\n");

            JTextArea textArea = new JTextArea(details.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JOptionPane.showMessageDialog(this,
                    new JScrollPane(textArea),
                    "Staff Details - " + staff.getFullName(),
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
