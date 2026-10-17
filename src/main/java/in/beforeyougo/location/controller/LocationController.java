package in.beforeyougo.location.controller;

import in.beforeyougo.location.dto.LocationSearchResult;
import in.beforeyougo.location.service.LocationSearchService;
import in.beforeyougo.location.service.LocationSearchService.LocationCounts;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for location search (typeahead).
 * <p>
 * GET /api/locations/search?q={query}&level={level}
 * </p>
 * No user data is logged or stored.
 */
@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationSearchService searchService;

    public LocationController(LocationSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Typeahead search over states, districts, sub-districts, villages and pincodes.
     *
     * @param q     search query (minimum 2 characters)
     * @param level optional filter: STATE, DISTRICT, SUB_DISTRICT, VILLAGE, PINCODE
     * @return list of matching locations with hierarchy paths
     */
    @GetMapping("/search")
    public ResponseEntity<List<LocationSearchResult>> search(
            @RequestParam("q") String q,
            @RequestParam(value = "level", required = false) String level) {

        List<LocationSearchResult> results = searchService.search(q, level);
        return ResponseEntity.ok(results);
    }

    /**
     * Returns the count of all location entities.
     * Useful for verifying import completeness.
     */
    @GetMapping("/counts")
    public ResponseEntity<LocationCounts> counts() {
        return ResponseEntity.ok(searchService.getCounts());
    }
}
