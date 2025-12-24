package controller;

import model.Staff;
import util.CSVReader;
import util.CSVWriter;
import util.FilePathManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller class for managing Staff data.
 * Handles CRUD operations and CSV file persistence.
 */
public class StaffController {

    private List<Staff> staffList;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] CSV_HEADER = {
            "staff_id", "first_name", "last_name", "role", "department",
            "facility_id", "phone_number", "email", "employment_status",
            "start_date", "line_manager", "access_level"
    };

    public StaffController() {
        staffList = new ArrayList<>();
    }

    /**
     * Loads staff from CSV file.
     *
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV() throws IOException {
        String filePath = FilePathManager.getStaffFilePath();
        List<String[]> records = CSVReader.readCSV(filePath);
        staffList.clear();

        for (String[] record : records) {
            if (record.length >= 12) {
                Staff staff = new Staff();
                staff.setStaffId(record[0]);
                staff.setFirstName(record[1]);
                staff.setLastName(record[2]);
                staff.setRole(record[3]);
                staff.setDepartment(record[4]);
                staff.setFacilityId(record[5]);
                staff.setPhoneNumber(record[6]);
                staff.setEmail(record[7]);
                staff.setEmploymentStatus(record[8]);
                staff.setStartDate(parseDate(record[9]));
                staff.setLineManager(record[10]);
                staff.setAccessLevel(record[11]);
                staffList.add(staff);
            }
        }
    }

    /**
     * Saves all staff to CSV file.
     *
     * @throws IOException if file cannot be written
     */
    public void saveToCSV() throws IOException {
        String filePath = FilePathManager.getStaffFilePath();
        List<String[]> data = new ArrayList<>();

        for (Staff staff : staffList) {
            String[] row = {
                    staff.getStaffId(),
                    staff.getFirstName(),
                    staff.getLastName(),
                    staff.getRole(),
                    staff.getDepartment(),
                    staff.getFacilityId(),
                    staff.getPhoneNumber(),
                    staff.getEmail(),
                    staff.getEmploymentStatus(),
                    formatDate(staff.getStartDate()),
                    staff.getLineManager(),
                    staff.getAccessLevel()
            };
            data.add(row);
        }

        CSVWriter.writeCSV(filePath, CSV_HEADER, data);
    }

    /**
     * Gets all staff.
     *
     * @return List of all staff
     */
    public List<Staff> getAllStaff() {
        return new ArrayList<>(staffList);
    }

    /**
     * Gets a staff member by ID.
     *
     * @param staffId The staff ID
     * @return The staff member, or null if not found
     */
    public Staff getStaffById(String staffId) {
        return staffList.stream()
                .filter(s -> s.getStaffId().equals(staffId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets staff by facility.
     *
     * @param facilityId The facility ID
     * @return List of staff at the facility
     */
    public List<Staff> getStaffByFacility(String facilityId) {
        return staffList.stream()
                .filter(s -> s.getFacilityId().equals(facilityId))
                .collect(Collectors.toList());
    }

    /**
     * Gets staff by role.
     *
     * @param role The role
     * @return List of staff with the role
     */
    public List<Staff> getStaffByRole(String role) {
        return staffList.stream()
                .filter(s -> s.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());
    }

    /**
     * Gets staff by department.
     *
     * @param department The department
     * @return List of staff in the department
     */
    public List<Staff> getStaffByDepartment(String department) {
        return staffList.stream()
                .filter(s -> s.getDepartment().equalsIgnoreCase(department))
                .collect(Collectors.toList());
    }

    /**
     * Searches staff by name.
     *
     * @param searchTerm The search term
     * @return List of matching staff
     */
    public List<Staff> searchByName(String searchTerm) {
        String term = searchTerm.toLowerCase();
        return staffList.stream()
                .filter(s -> s.getFirstName().toLowerCase().contains(term) ||
                        s.getLastName().toLowerCase().contains(term) ||
                        s.getFullName().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new staff member.
     *
     * @param staff The staff to add
     */
    public void addStaff(Staff staff) {
        staffList.add(staff);
    }

    /**
     * Updates an existing staff member.
     *
     * @param staff The staff with updated data
     * @return true if updated, false if not found
     */
    public boolean updateStaff(Staff staff) {
        for (int i = 0; i < staffList.size(); i++) {
            if (staffList.get(i).getStaffId().equals(staff.getStaffId())) {
                staffList.set(i, staff);
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a staff member by ID.
     *
     * @param staffId The staff ID
     * @return true if deleted, false if not found
     */
    public boolean deleteStaff(String staffId) {
        return staffList.removeIf(s -> s.getStaffId().equals(staffId));
    }

    /**
     * Gets the next available staff ID.
     *
     * @return The next staff ID
     */
    public String getNextStaffId() {
        int maxId = 0;
        for (Staff s : staffList) {
            String id = s.getStaffId();
            if (id != null && id.startsWith("ST")) {
                try {
                    int num = Integer.parseInt(id.substring(2));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        return String.format("ST%03d", maxId + 1);
    }

    /**
     * Gets the count of staff.
     *
     * @return Number of staff
     */
    public int getStaffCount() {
        return staffList.size();
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

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMAT);
    }
}
