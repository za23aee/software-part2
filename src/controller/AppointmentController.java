package controller;

import model.Appointment;
import util.CSVReader;
import util.CSVWriter;
import util.FilePathManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller class for managing Appointment data.
 * Handles CRUD operations and CSV file persistence.
 */
public class AppointmentController {

    private List<Appointment> appointments;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] CSV_HEADER = {
            "appointment_id", "patient_id", "clinician_id", "facility_id",
            "appointment_date", "appointment_time", "duration_minutes", "appointment_type",
            "status", "reason_for_visit", "notes", "created_date", "last_modified"
    };

    public AppointmentController() {
        appointments = new ArrayList<>();
    }

    /**
     * Loads appointments from CSV file.
     *
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV() throws IOException {
        String filePath = FilePathManager.getAppointmentsFilePath();
        List<String[]> records = CSVReader.readCSV(filePath);
        appointments.clear();

        for (String[] record : records) {
            if (record.length >= 13) {
                Appointment appointment = new Appointment();
                appointment.setAppointmentId(record[0]);
                appointment.setPatientId(record[1]);
                appointment.setClinicianId(record[2]);
                appointment.setFacilityId(record[3]);
                appointment.setAppointmentDate(parseDate(record[4]));
                appointment.setAppointmentTime(parseTime(record[5]));
                appointment.setDurationMinutes(parseInt(record[6]));
                appointment.setAppointmentType(record[7]);
                appointment.setStatus(record[8]);
                appointment.setReasonForVisit(record[9]);
                appointment.setNotes(record[10]);
                appointment.setCreatedDate(parseDate(record[11]));
                appointment.setLastModified(parseDate(record[12]));
                appointments.add(appointment);
            }
        }
    }

    /**
     * Saves all appointments to CSV file.
     *
     * @throws IOException if file cannot be written
     */
    public void saveToCSV() throws IOException {
        String filePath = FilePathManager.getAppointmentsFilePath();
        List<String[]> data = new ArrayList<>();

        for (Appointment appointment : appointments) {
            String[] row = {
                    appointment.getAppointmentId(),
                    appointment.getPatientId(),
                    appointment.getClinicianId(),
                    appointment.getFacilityId(),
                    formatDate(appointment.getAppointmentDate()),
                    formatTime(appointment.getAppointmentTime()),
                    String.valueOf(appointment.getDurationMinutes()),
                    appointment.getAppointmentType(),
                    appointment.getStatus(),
                    appointment.getReasonForVisit(),
                    appointment.getNotes(),
                    formatDate(appointment.getCreatedDate()),
                    formatDate(appointment.getLastModified())
            };
            data.add(row);
        }

        CSVWriter.writeCSV(filePath, CSV_HEADER, data);
    }

    /**
     * Gets all appointments.
     *
     * @return List of all appointments
     */
    public List<Appointment> getAllAppointments() {
        return new ArrayList<>(appointments);
    }

    /**
     * Gets an appointment by ID.
     *
     * @param appointmentId The appointment ID
     * @return The appointment, or null if not found
     */
    public Appointment getAppointmentById(String appointmentId) {
        return appointments.stream()
                .filter(a -> a.getAppointmentId().equals(appointmentId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets appointments for a patient.
     *
     * @param patientId The patient ID
     * @return List of appointments for the patient
     */
    public List<Appointment> getAppointmentsByPatient(String patientId) {
        return appointments.stream()
                .filter(a -> a.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    /**
     * Gets appointments for a clinician.
     *
     * @param clinicianId The clinician ID
     * @return List of appointments for the clinician
     */
    public List<Appointment> getAppointmentsByClinician(String clinicianId) {
        return appointments.stream()
                .filter(a -> a.getClinicianId().equals(clinicianId))
                .collect(Collectors.toList());
    }

    /**
     * Gets appointments for a specific date.
     *
     * @param date The date
     * @return List of appointments on the date
     */
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointments.stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Gets appointments by status.
     *
     * @param status The status
     * @return List of appointments with the status
     */
    public List<Appointment> getAppointmentsByStatus(String status) {
        return appointments.stream()
                .filter(a -> status.equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * Gets scheduled appointments.
     *
     * @return List of scheduled appointments
     */
    public List<Appointment> getScheduledAppointments() {
        return appointments.stream()
                .filter(Appointment::isScheduled)
                .collect(Collectors.toList());
    }

    /**
     * Adds a new appointment.
     *
     * @param appointment The appointment to add
     */
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    /**
     * Updates an existing appointment.
     *
     * @param appointment The appointment with updated data
     * @return true if updated, false if not found
     */
    public boolean updateAppointment(Appointment appointment) {
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getAppointmentId().equals(appointment.getAppointmentId())) {
                appointments.set(i, appointment);
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes an appointment by ID.
     *
     * @param appointmentId The appointment ID
     * @return true if deleted, false if not found
     */
    public boolean deleteAppointment(String appointmentId) {
        return appointments.removeIf(a -> a.getAppointmentId().equals(appointmentId));
    }

    /**
     * Cancels an appointment.
     *
     * @param appointmentId The appointment ID
     * @return true if cancelled, false if not found
     */
    public boolean cancelAppointment(String appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);
        if (appointment != null) {
            appointment.setStatus("Cancelled");
            appointment.setLastModified(LocalDate.now());
            return true;
        }
        return false;
    }

    /**
     * Gets the next available appointment ID.
     *
     * @return The next appointment ID
     */
    public String getNextAppointmentId() {
        int maxId = 0;
        for (Appointment a : appointments) {
            String id = a.getAppointmentId();
            if (id != null && id.startsWith("A")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        return String.format("A%03d", maxId + 1);
    }

    /**
     * Gets the count of appointments.
     *
     * @return Number of appointments
     */
    public int getAppointmentCount() {
        return appointments.size();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeStr, TIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private int parseInt(String intStr) {
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMAT);
    }

    private String formatTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMAT);
    }
}
