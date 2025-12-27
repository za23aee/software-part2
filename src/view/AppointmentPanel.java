package view;

import controller.AppointmentController;
import controller.ClinicianController;
import controller.FacilityController;
import controller.PatientController;
import model.Appointment;
import model.Clinician;
import model.Facility;
import model.Patient;
import view.dialogs.AppointmentDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Panel for displaying and managing appointments.
 * Provides JTable view with filtering and CRUD functionality.
 */
public class AppointmentPanel extends JPanel {

    private AppointmentController appointmentController;
    private PatientController patientController;
    private ClinicianController clinicianController;
    private FacilityController facilityController;
    private JTable appointmentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private TableRowSorter<DefaultTableModel> sorter;

    private static final String[] COLUMN_NAMES = {
            "ID", "Date", "Time", "Patient", "Clinician",
            "Facility", "Type", "Status", "Reason"
    };

    public AppointmentPanel(AppointmentController appointmentController,
                            PatientController patientController,
                            ClinicianController clinicianController,
                            FacilityController facilityController) {
        this.appointmentController = appointmentController;
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

        JLabel titleLabel = new JLabel("Appointment Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Filter by status
        filterPanel.add(new JLabel("Status:"));
        statusFilterCombo = new JComboBox<>(new String[]{"All", "Scheduled", "Completed", "Cancelled"});
        statusFilterCombo.addActionListener(e -> applyFilter());
        filterPanel.add(statusFilterCombo);

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

        appointmentTable = new JTable(tableModel);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setRowHeight(25);
        appointmentTable.getTableHeader().setReorderingAllowed(false);

        sorter = new TableRowSorter<>(tableModel);
        appointmentTable.setRowSorter(sorter);

        // Double-click to edit
        appointmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editAppointment();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton addButton = new JButton("New Appointment");
        addButton.addActionListener(e -> addAppointment());

        JButton editButton = new JButton("Edit Appointment");
        editButton.addActionListener(e -> editAppointment());

        JButton cancelButton = new JButton("Cancel Appointment");
        cancelButton.addActionListener(e -> cancelAppointment());

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteAppointment());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(cancelButton);
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
                    String status = entry.getStringValue(7);
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
        List<Appointment> appointments = appointmentController.getAllAppointments();

        for (Appointment appointment : appointments) {
            String patientName = "";
            if (appointment.getPatientId() != null) {
                Patient patient = patientController.getPatientById(appointment.getPatientId());
                if (patient != null) {
                    patientName = patient.getFullName();
                }
            }

            String clinicianName = "";
            if (appointment.getClinicianId() != null) {
                Clinician clinician = clinicianController.getClinicianById(appointment.getClinicianId());
                if (clinician != null) {
                    clinicianName = clinician.getTitle() + " " + clinician.getFullName();
                }
            }

            String facilityName = "";
            if (appointment.getFacilityId() != null) {
                Facility facility = facilityController.getFacilityById(appointment.getFacilityId());
                if (facility != null) {
                    facilityName = facility.getFacilityName();
                }
            }

            Object[] row = {
                    appointment.getAppointmentId(),
                    appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "",
                    appointment.getAppointmentTime() != null ? appointment.getAppointmentTime().toString() : "",
                    patientName,
                    clinicianName,
                    facilityName,
                    appointment.getAppointmentType(),
                    appointment.getStatus(),
                    appointment.getReasonForVisit()
            };
            tableModel.addRow(row);
        }
    }

    private void addAppointment() {
        AppointmentDialog dialog = new AppointmentDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "New Appointment",
                null,
                appointmentController,
                patientController,
                clinicianController,
                facilityController
        );
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void editAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
        String appointmentId = (String) tableModel.getValueAt(modelRow, 0);
        Appointment appointment = appointmentController.getAppointmentById(appointmentId);

        if (appointment != null) {
            AppointmentDialog dialog = new AppointmentDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Edit Appointment",
                    appointment,
                    appointmentController,
                    patientController,
                    clinicianController,
                    facilityController
            );
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                refreshData();
            }
        }
    }

    private void cancelAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment to cancel.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
        String appointmentId = (String) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this appointment?",
                "Confirm Cancel",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (appointmentController.cancelAppointment(appointmentId)) {
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "Appointment cancelled successfully.",
                        "Cancel Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void deleteAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
        String appointmentId = (String) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this appointment?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (appointmentController.deleteAppointment(appointmentId)) {
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "Appointment deleted successfully.",
                        "Delete Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
