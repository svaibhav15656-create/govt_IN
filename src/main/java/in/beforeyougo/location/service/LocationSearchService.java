package in.beforeyougo.location.service;

import in.beforeyougo.location.dto.LocationSearchResult;
import in.beforeyougo.location.dto.LocationSearchResult.Level;
import in.beforeyougo.location.entity.*;
import in.beforeyougo.location.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides typeahead search across all location levels.
 * <p>
 * On PostgreSQL, pg_trgm GIN indexes accelerate LIKE '%query%' patterns
 * and provide typo tolerance. On H2 (tests), falls back to sequential LIKE.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class LocationSearchService {

    private static final int MAX_RESULTS_PER_LEVEL = 10;
    private static final int MAX_TOTAL_RESULTS = 20;

    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final SubDistrictRepository subDistrictRepository;
    private final VillageRepository villageRepository;
    private final PincodeRepository pincodeRepository;

    public LocationSearchService(StateRepository stateRepository,
                                  DistrictRepository districtRepository,
                                  SubDistrictRepository subDistrictRepository,
                                  VillageRepository villageRepository,
                                  PincodeRepository pincodeRepository) {
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
        this.subDistrictRepository = subDistrictRepository;
        this.villageRepository = villageRepository;
        this.pincodeRepository = pincodeRepository;
    }

    /**
     * Searches locations by name or pincode across all levels.
     *
     * @param query the search query (minimum 2 characters)
     * @param level optional filter: STATE, DISTRICT, SUB_DISTRICT, VILLAGE, PINCODE.
     *              If null, searches all levels.
     * @return up to {@value MAX_TOTAL_RESULTS} results, prioritized by level hierarchy
     */
    public List<LocationSearchResult> search(String query, String level) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }

        String trimmed = query.trim();
        Level filterLevel = parseLevel(level);

        List<LocationSearchResult> results = new ArrayList<>();

        // If the query looks like a pincode (all digits), search pincodes first
        if (trimmed.matches("\\d+") && (filterLevel == null || filterLevel == Level.PINCODE)) {
            addPincodeResults(trimmed, results);
        }

        if (filterLevel == null || filterLevel == Level.STATE) {
            addStateResults(trimmed, results);
        }
        if (filterLevel == null || filterLevel == Level.DISTRICT) {
            addDistrictResults(trimmed, results);
        }
        if (filterLevel == null || filterLevel == Level.SUB_DISTRICT) {
            addSubDistrictResults(trimmed, results);
        }
        if (filterLevel == null || filterLevel == Level.VILLAGE) {
            addVillageResults(trimmed, results);
        }

        // Limit total results
        if (results.size() > MAX_TOTAL_RESULTS) {
            return results.subList(0, MAX_TOTAL_RESULTS);
        }
        return results;
    }

    /**
     * Returns counts of all location entities for import verification.
     */
    public LocationCounts getCounts() {
        return new LocationCounts(
            stateRepository.count(),
            districtRepository.count(),
            subDistrictRepository.count(),
            villageRepository.count(),
            pincodeRepository.count()
        );
    }

    // --- Private helpers ---

    private void addStateResults(String query, List<LocationSearchResult> results) {
        List<State> states = stateRepository.searchByName(query);
        for (State s : states) {
            if (results.size() >= MAX_TOTAL_RESULTS) break;
            results.add(new LocationSearchResult(
                String.valueOf(s.getLgdCode()),
                s.getNameEn(),
                s.getNameLocal(),
                Level.STATE,
                s.getNameEn()
            ));
        }
    }

    private void addDistrictResults(String query, List<LocationSearchResult> results) {
        List<District> districts = districtRepository.searchByName(query);
        int count = 0;
        for (District d : districts) {
            if (results.size() >= MAX_TOTAL_RESULTS || count >= MAX_RESULTS_PER_LEVEL) break;
            String path = d.getState().getNameEn() + " > " + d.getNameEn();
            results.add(new LocationSearchResult(
                String.valueOf(d.getLgdCode()),
                d.getNameEn(),
                d.getNameLocal(),
                Level.DISTRICT,
                path
            ));
            count++;
        }
    }

    private void addSubDistrictResults(String query, List<LocationSearchResult> results) {
        List<SubDistrict> subDistricts = subDistrictRepository.searchByName(query);
        int count = 0;
        for (SubDistrict sd : subDistricts) {
            if (results.size() >= MAX_TOTAL_RESULTS || count >= MAX_RESULTS_PER_LEVEL) break;
            District d = sd.getDistrict();
            String path = d.getState().getNameEn() + " > " + d.getNameEn() + " > " + sd.getNameEn();
            results.add(new LocationSearchResult(
                String.valueOf(sd.getLgdCode()),
                sd.getNameEn(),
                sd.getNameLocal(),
                Level.SUB_DISTRICT,
                path
            ));
            count++;
        }
    }

    private void addVillageResults(String query, List<LocationSearchResult> results) {
        List<Village> villages = villageRepository.searchByName(query);
        int count = 0;
        for (Village v : villages) {
            if (results.size() >= MAX_TOTAL_RESULTS || count >= MAX_RESULTS_PER_LEVEL) break;
            SubDistrict sd = v.getSubDistrict();
            District d = sd.getDistrict();
            String path = d.getState().getNameEn() + " > " + d.getNameEn()
                          + " > " + sd.getNameEn() + " > " + v.getNameEn();
            results.add(new LocationSearchResult(
                String.valueOf(v.getLgdCode()),
                v.getNameEn(),
                v.getNameLocal(),
                Level.VILLAGE,
                path
            ));
            count++;
        }
    }

    private void addPincodeResults(String query, List<LocationSearchResult> results) {
        List<Pincode> pincodes;
        if (query.length() == 6) {
            pincodes = pincodeRepository.findByPincode(query);
        } else {
            pincodes = pincodeRepository.searchByPrefix(query);
        }
        int count = 0;
        for (Pincode p : pincodes) {
            if (results.size() >= MAX_TOTAL_RESULTS || count >= MAX_RESULTS_PER_LEVEL) break;
            String path = buildPincodePath(p);
            String name = "Pincode " + p.getPincode();
            if (p.getVillage() != null) {
                name = p.getVillage().getNameEn() + " (" + p.getPincode() + ")";
            }
            results.add(new LocationSearchResult(
                p.getPincode(),
                name,
                p.getVillage() != null ? p.getVillage().getNameLocal() : null,
                Level.PINCODE,
                path
            ));
            count++;
        }
    }

    private String buildPincodePath(Pincode p) {
        if (p.getVillage() != null) {
            Village v = p.getVillage();
            SubDistrict sd = v.getSubDistrict();
            District d = sd.getDistrict();
            return d.getState().getNameEn() + " > " + d.getNameEn()
                   + " > " + sd.getNameEn() + " > " + v.getNameEn();
        } else if (p.getDistrict() != null) {
            District d = p.getDistrict();
            return d.getState().getNameEn() + " > " + d.getNameEn();
        }
        return "Pincode " + p.getPincode();
    }

    private Level parseLevel(String level) {
        if (level == null || level.isBlank()) return null;
        try {
            return Level.valueOf(level.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Record holding counts of all location entities.
     */
    public record LocationCounts(long states, long districts, long subDistricts,
                                  long villages, long pincodes) {}
}
