package controller;

import model.Facility;
import util.CSVReader;
import util.CSVWriter;
import util.FilePathManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller class for managing Facility data.
 * Handles CRUD operations and CSV file persistence.
 */
public class FacilityController {

    private List<Facility> facilities;
    private static final String[] CSV_HEADER = {
            "facility_id", "facility_name", "facility_type", "address", "postcode",
            "phone_number", "email", "opening_hours", "manager_name", "capacity", "specialities_offered"
    };

    public FacilityController() {
        facilities = new ArrayList<>();
    }

    /**
     * Loads facilities from CSV file.
     *
     * @throws IOException if file cannot be read
     */
    public void loadFromCSV() throws IOException {
        String filePath = FilePathManager.getFacilitiesFilePath();
        List<String[]> records = CSVReader.readCSV(filePath);
        facilities.clear();

        for (String[] record : records) {
            if (record.length >= 11) {
                Facility facility = new Facility();
                facility.setFacilityId(record[0]);
                facility.setFacilityName(record[1]);
                facility.setFacilityType(record[2]);
                facility.setAddress(record[3]);
                facility.setPostcode(record[4]);
                facility.setPhoneNumber(record[5]);
                facility.setEmail(record[6]);
                facility.setOpeningHours(record[7]);
                facility.setManagerName(record[8]);
                facility.setCapacity(parseCapacity(record[9]));
                facility.setSpecialitiesOffered(record[10]);
                facilities.add(facility);
            }
        }
    }

    /**
     * Saves all facilities to CSV file.
     *
     * @throws IOException if file cannot be written
     */
    public void saveToCSV() throws IOException {
        String filePath = FilePathManager.getFacilitiesFilePath();
        List<String[]> data = new ArrayList<>();

        for (Facility facility : facilities) {
            String[] row = {
                    facility.getFacilityId(),
                    facility.getFacilityName(),
                    facility.getFacilityType(),
                    facility.getAddress(),
                    facility.getPostcode(),
                    facility.getPhoneNumber(),
                    facility.getEmail(),
                    facility.getOpeningHours(),
                    facility.getManagerName(),
                    String.valueOf(facility.getCapacity()),
                    facility.getSpecialitiesOffered()
            };
            data.add(row);
        }

        CSVWriter.writeCSV(filePath, CSV_HEADER, data);
    }

    /**
     * Gets all facilities.
     *
     * @return List of all facilities
     */
    public List<Facility> getAllFacilities() {
        return new ArrayList<>(facilities);
    }

    /**
     * Gets a facility by ID.
     *
     * @param facilityId The facility ID
     * @return The facility, or null if not found
     */
    public Facility getFacilityById(String facilityId) {
        return facilities.stream()
                .filter(f -> f.getFacilityId().equals(facilityId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all GP surgeries.
     *
     * @return List of GP surgeries
     */
    public List<Facility> getGPSurgeries() {
        return facilities.stream()
                .filter(Facility::isGPSurgery)
                .collect(Collectors.toList());
    }

    /**
     * Gets all hospitals.
     *
     * @return List of hospitals
     */
    public List<Facility> getHospitals() {
        return facilities.stream()
                .filter(Facility::isHospital)
                .collect(Collectors.toList());
    }

    /**
     * Searches facilities by name.
     *
     * @param searchTerm The search term
     * @return List of matching facilities
     */
    public List<Facility> searchByName(String searchTerm) {
        String term = searchTerm.toLowerCase();
        return facilities.stream()
                .filter(f -> f.getFacilityName().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new facility.
     *
     * @param facility The facility to add
     */
    public void addFacility(Facility facility) {
        facilities.add(facility);
    }

    /**
     * Updates an existing facility.
     *
     * @param facility The facility with updated data
     * @return true if updated, false if not found
     */
    public boolean updateFacility(Facility facility) {
        for (int i = 0; i < facilities.size(); i++) {
            if (facilities.get(i).getFacilityId().equals(facility.getFacilityId())) {
                facilities.set(i, facility);
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a facility by ID.
     *
     * @param facilityId The facility ID
     * @return true if deleted, false if not found
     */
    public boolean deleteFacility(String facilityId) {
        return facilities.removeIf(f -> f.getFacilityId().equals(facilityId));
    }

    /**
     * Gets the count of facilities.
     *
     * @return Number of facilities
     */
    public int getFacilityCount() {
        return facilities.size();
    }

    private int parseCapacity(String capacityStr) {
        try {
            return Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
