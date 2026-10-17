package in.beforeyougo.location.importer;

import in.beforeyougo.location.entity.*;
import in.beforeyougo.location.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

/**
 * Imports LGD (Local Government Directory) location data from CSV files.
 * <p>
 * Import is idempotent: existing records are updated, new records are inserted.
 * Each import run is tracked via {@link IngestionRun}.
 * </p>
 * <p>
 * Expected CSV format follows LGD data downloads from data.gov.in.
 * See docs/data-import.md for column mapping details.
 * </p>
 */
@Service
public class LgdImportService {

    private static final Logger log = LoggerFactory.getLogger(LgdImportService.class);
    private static final int BATCH_SIZE = 500;

    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final SubDistrictRepository subDistrictRepository;
    private final VillageRepository villageRepository;
    private final PincodeRepository pincodeRepository;
    private final IngestionRunRepository ingestionRunRepository;

    public LgdImportService(StateRepository stateRepository,
                             DistrictRepository districtRepository,
                             SubDistrictRepository subDistrictRepository,
                             VillageRepository villageRepository,
                             PincodeRepository pincodeRepository,
                             IngestionRunRepository ingestionRunRepository) {
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
        this.subDistrictRepository = subDistrictRepository;
        this.villageRepository = villageRepository;
        this.pincodeRepository = pincodeRepository;
        this.ingestionRunRepository = ingestionRunRepository;
    }

    /**
     * Import states from a CSV file.
     * Expected columns: State Code, State Name (In English), State Name (In Local language), State or UT
     */
    @Transactional
    public IngestionRun importStates(Path csvPath) {
        IngestionRun run = new IngestionRun("LGD_STATES", csvPath.toString());
        ingestionRunRepository.save(run);

        try {
            int processed = 0;
            int skipped = 0;

            try (BufferedReader reader = newReader(csvPath)) {
                String[] headers = parseCsvLine(reader.readLine());
                Map<String, Integer> colMap = buildColumnMap(headers);

                requireColumns(colMap, "State Code", "State Name (In English)");

                String line;
                List<State> batch = new ArrayList<>(BATCH_SIZE);

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] cols = parseCsvLine(line);

                    try {
                        int code = parseInt(cols, colMap, "State Code");
                        String nameEn = getCol(cols, colMap, "State Name (In English)");
                        String nameLocal = getColOrNull(cols, colMap, "State Name (In Local language)");
                        String kind = getColOrDefault(cols, colMap, "State or UT", "STATE");

                        State state = stateRepository.findById(code).orElse(new State());
                        state.setLgdCode(code);
                        state.setNameEn(nameEn);
                        state.setNameLocal(nameLocal);
                        state.setKind(kind.toUpperCase().contains("UT") ? "UT" : "STATE");
                        state.setCensus2011Code(getColOrNull(cols, colMap, "Census 2011 Code"));
                        batch.add(state);
                        processed++;

                        if (batch.size() >= BATCH_SIZE) {
                            stateRepository.saveAll(batch);
                            batch.clear();
                        }
                    } catch (Exception e) {
                        log.warn("Skipping state row: {}", e.getMessage());
                        skipped++;
                    }
                }
                if (!batch.isEmpty()) {
                    stateRepository.saveAll(batch);
                }
            }

            run.markCompleted(processed, skipped);
            log.info("States import completed: {} processed, {} skipped", processed, skipped);
        } catch (Exception e) {
            run.markFailed(e.getMessage());
            log.error("States import failed", e);
        }

        ingestionRunRepository.save(run);
        return run;
    }

    /**
     * Import districts from a CSV file.
     * Expected columns: State Code, District Code, District Name (In English),
     *                   District Name (In Local language)
     */
    @Transactional
    public IngestionRun importDistricts(Path csvPath) {
        IngestionRun run = new IngestionRun("LGD_DISTRICTS", csvPath.toString());
        ingestionRunRepository.save(run);

        try {
            int processed = 0;
            int skipped = 0;

            try (BufferedReader reader = newReader(csvPath)) {
                String[] headers = parseCsvLine(reader.readLine());
                Map<String, Integer> colMap = buildColumnMap(headers);

                requireColumns(colMap, "State Code", "District Code", "District Name (In English)");

                String line;
                List<District> batch = new ArrayList<>(BATCH_SIZE);

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] cols = parseCsvLine(line);

                    try {
                        int stateCode = parseInt(cols, colMap, "State Code");
                        int code = parseInt(cols, colMap, "District Code");
                        String nameEn = getCol(cols, colMap, "District Name (In English)");
                        String nameLocal = getColOrNull(cols, colMap, "District Name (In Local language)");

                        State state = stateRepository.findById(stateCode)
                            .orElseThrow(() -> new IllegalStateException(
                                "State " + stateCode + " not found. Import states first."));

                        District district = districtRepository.findById(code).orElse(new District());
                        district.setLgdCode(code);
                        district.setState(state);
                        district.setNameEn(nameEn);
                        district.setNameLocal(nameLocal);
                        district.setCensus2011Code(getColOrNull(cols, colMap, "Census 2011 Code"));
                        batch.add(district);
                        processed++;

                        if (batch.size() >= BATCH_SIZE) {
                            districtRepository.saveAll(batch);
                            batch.clear();
                        }
                    } catch (Exception e) {
                        log.warn("Skipping district row: {}", e.getMessage());
                        skipped++;
                    }
                }
                if (!batch.isEmpty()) {
                    districtRepository.saveAll(batch);
                }
            }

            run.markCompleted(processed, skipped);
            log.info("Districts import completed: {} processed, {} skipped", processed, skipped);
        } catch (Exception e) {
            run.markFailed(e.getMessage());
            log.error("Districts import failed", e);
        }

        ingestionRunRepository.save(run);
        return run;
    }

    /**
     * Import sub-districts from a CSV file.
     * Expected columns: District Code, Sub-district Code,
     *                   Sub-district Name (In English), Sub-district Name (In Local language)
     */
    @Transactional
    public IngestionRun importSubDistricts(Path csvPath) {
        IngestionRun run = new IngestionRun("LGD_SUB_DISTRICTS", csvPath.toString());
        ingestionRunRepository.save(run);

        try {
            int processed = 0;
            int skipped = 0;

            try (BufferedReader reader = newReader(csvPath)) {
                String[] headers = parseCsvLine(reader.readLine());
                Map<String, Integer> colMap = buildColumnMap(headers);

                requireColumns(colMap, "District Code", "Sub-district Code",
                               "Sub-district Name (In English)");

                String line;
                List<SubDistrict> batch = new ArrayList<>(BATCH_SIZE);

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] cols = parseCsvLine(line);

                    try {
                        int districtCode = parseInt(cols, colMap, "District Code");
                        int code = parseInt(cols, colMap, "Sub-district Code");
                        String nameEn = getCol(cols, colMap, "Sub-district Name (In English)");
                        String nameLocal = getColOrNull(cols, colMap,
                                                        "Sub-district Name (In Local language)");

                        District district = districtRepository.findById(districtCode)
                            .orElseThrow(() -> new IllegalStateException(
                                "District " + districtCode + " not found. Import districts first."));

                        SubDistrict subDistrict = subDistrictRepository.findById(code)
                            .orElse(new SubDistrict());
                        subDistrict.setLgdCode(code);
                        subDistrict.setDistrict(district);
                        subDistrict.setNameEn(nameEn);
                        subDistrict.setNameLocal(nameLocal);
                        subDistrict.setCensus2011Code(getColOrNull(cols, colMap, "Census 2011 Code"));
                        batch.add(subDistrict);
                        processed++;

                        if (batch.size() >= BATCH_SIZE) {
                            subDistrictRepository.saveAll(batch);
                            batch.clear();
                        }
                    } catch (Exception e) {
                        log.warn("Skipping sub-district row: {}", e.getMessage());
                        skipped++;
                    }
                }
                if (!batch.isEmpty()) {
                    subDistrictRepository.saveAll(batch);
                }
            }

            run.markCompleted(processed, skipped);
            log.info("Sub-districts import completed: {} processed, {} skipped", processed, skipped);
        } catch (Exception e) {
            run.markFailed(e.getMessage());
            log.error("Sub-districts import failed", e);
        }

        ingestionRunRepository.save(run);
        return run;
    }

    /**
     * Import villages from a CSV file.
     * Expected columns: Sub-district Code, Village Code,
     *                   Village Name (In English), Village Name (In Local language)
     */
    @Transactional
    public IngestionRun importVillages(Path csvPath) {
        IngestionRun run = new IngestionRun("LGD_VILLAGES", csvPath.toString());
        ingestionRunRepository.save(run);

        try {
            int processed = 0;
            int skipped = 0;

            try (BufferedReader reader = newReader(csvPath)) {
                String[] headers = parseCsvLine(reader.readLine());
                Map<String, Integer> colMap = buildColumnMap(headers);

                requireColumns(colMap, "Sub-district Code", "Village Code",
                               "Village Name (In English)");

                String line;
                List<Village> batch = new ArrayList<>(BATCH_SIZE);

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] cols = parseCsvLine(line);

                    try {
                        int subDistrictCode = parseInt(cols, colMap, "Sub-district Code");
                        int code = parseInt(cols, colMap, "Village Code");
                        String nameEn = getCol(cols, colMap, "Village Name (In English)");
                        String nameLocal = getColOrNull(cols, colMap,
                                                        "Village Name (In Local language)");

                        SubDistrict subDistrict = subDistrictRepository.findById(subDistrictCode)
                            .orElseThrow(() -> new IllegalStateException(
                                "Sub-district " + subDistrictCode
                                + " not found. Import sub-districts first."));

                        Village village = villageRepository.findById(code).orElse(new Village());
                        village.setLgdCode(code);
                        village.setSubDistrict(subDistrict);
                        village.setNameEn(nameEn);
                        village.setNameLocal(nameLocal);
                        village.setCensus2011Code(getColOrNull(cols, colMap, "Census 2011 Code"));
                        batch.add(village);
                        processed++;

                        if (batch.size() >= BATCH_SIZE) {
                            villageRepository.saveAll(batch);
                            batch.clear();
                        }
                    } catch (Exception e) {
                        log.warn("Skipping village row: {}", e.getMessage());
                        skipped++;
                    }
                }
                if (!batch.isEmpty()) {
                    villageRepository.saveAll(batch);
                }
            }

            run.markCompleted(processed, skipped);
            log.info("Villages import completed: {} processed, {} skipped", processed, skipped);
        } catch (Exception e) {
            run.markFailed(e.getMessage());
            log.error("Villages import failed", e);
        }

        ingestionRunRepository.save(run);
        return run;
    }

    /**
     * Import pincode-to-village mappings from a CSV file.
     * Expected columns: Village Code, Pincode
     */
    @Transactional
    public IngestionRun importPincodes(Path csvPath) {
        IngestionRun run = new IngestionRun("LGD_PINCODES", csvPath.toString());
        ingestionRunRepository.save(run);

        try {
            int processed = 0;
            int skipped = 0;

            try (BufferedReader reader = newReader(csvPath)) {
                String[] headers = parseCsvLine(reader.readLine());
                Map<String, Integer> colMap = buildColumnMap(headers);

                requireColumns(colMap, "Village Code", "Pincode");

                String line;
                List<Pincode> batch = new ArrayList<>(BATCH_SIZE);

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] cols = parseCsvLine(line);

                    try {
                        int villageCode = parseInt(cols, colMap, "Village Code");
                        String pincode = getCol(cols, colMap, "Pincode").trim();

                        if (pincode.length() != 6) {
                            log.warn("Invalid pincode '{}' for village {}", pincode, villageCode);
                            skipped++;
                            continue;
                        }

                        Village village = villageRepository.findById(villageCode).orElse(null);
                        if (village == null) {
                            log.warn("Village {} not found for pincode {}", villageCode, pincode);
                            skipped++;
                            continue;
                        }

                        // Idempotent: check if mapping already exists
                        Optional<Pincode> existing = pincodeRepository
                            .findByPincodeAndVillageCode(pincode, villageCode);
                        if (existing.isPresent()) {
                            processed++;
                            continue;
                        }

                        Pincode pc = new Pincode(pincode, village, "LGD");
                        batch.add(pc);
                        processed++;

                        if (batch.size() >= BATCH_SIZE) {
                            pincodeRepository.saveAll(batch);
                            batch.clear();
                        }
                    } catch (Exception e) {
                        log.warn("Skipping pincode row: {}", e.getMessage());
                        skipped++;
                    }
                }
                if (!batch.isEmpty()) {
                    pincodeRepository.saveAll(batch);
                }
            }

            run.markCompleted(processed, skipped);
            log.info("Pincodes import completed: {} processed, {} skipped", processed, skipped);
        } catch (Exception e) {
            run.markFailed(e.getMessage());
            log.error("Pincodes import failed", e);
        }

        ingestionRunRepository.save(run);
        return run;
    }

    /**
     * Run the full import in order: states → districts → sub-districts → villages → pincodes.
     *
     * @param dataDir directory containing the CSV files
     */
    @Transactional
    public List<IngestionRun> importAll(Path dataDir) {
        List<IngestionRun> runs = new ArrayList<>();

        Path statesFile = dataDir.resolve("states.csv");
        Path districtsFile = dataDir.resolve("districts.csv");
        Path subDistrictsFile = dataDir.resolve("sub_districts.csv");
        Path villagesFile = dataDir.resolve("villages.csv");
        Path pincodesFile = dataDir.resolve("pincodes.csv");

        if (statesFile.toFile().exists()) {
            runs.add(importStates(statesFile));
        }
        if (districtsFile.toFile().exists()) {
            runs.add(importDistricts(districtsFile));
        }
        if (subDistrictsFile.toFile().exists()) {
            runs.add(importSubDistricts(subDistrictsFile));
        }
        if (villagesFile.toFile().exists()) {
            runs.add(importVillages(villagesFile));
        }
        if (pincodesFile.toFile().exists()) {
            runs.add(importPincodes(pincodesFile));
        }

        return runs;
    }

    // ============================
    // CSV parsing helpers
    // ============================

    private BufferedReader newReader(Path path) throws IOException {
        return new BufferedReader(
            new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8));
    }

    /**
     * Parses a CSV line, handling quoted fields with commas.
     */
    static String[] parseCsvLine(String line) {
        if (line == null) return new String[0];
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());
        return fields.toArray(new String[0]);
    }

    private Map<String, Integer> buildColumnMap(String[] headers) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            map.put(headers[i].trim(), i);
        }
        return map;
    }

    private void requireColumns(Map<String, Integer> colMap, String... required) {
        for (String col : required) {
            if (!colMap.containsKey(col)) {
                throw new IllegalArgumentException("Required column '" + col
                    + "' not found. Available: " + colMap.keySet());
            }
        }
    }

    private String getCol(String[] cols, Map<String, Integer> colMap, String name) {
        Integer idx = colMap.get(name);
        if (idx == null || idx >= cols.length) {
            throw new IllegalArgumentException("Column '" + name + "' not available");
        }
        String val = cols[idx].trim();
        if (val.isEmpty()) {
            throw new IllegalArgumentException("Column '" + name + "' is empty");
        }
        return val;
    }

    private String getColOrNull(String[] cols, Map<String, Integer> colMap, String name) {
        Integer idx = colMap.get(name);
        if (idx == null || idx >= cols.length) return null;
        String val = cols[idx].trim();
        return val.isEmpty() ? null : val;
    }

    private String getColOrDefault(String[] cols, Map<String, Integer> colMap,
                                    String name, String defaultVal) {
        String val = getColOrNull(cols, colMap, name);
        return val != null ? val : defaultVal;
    }

    private int parseInt(String[] cols, Map<String, Integer> colMap, String name) {
        String val = getCol(cols, colMap, name);
        return Integer.parseInt(val);
    }
}
