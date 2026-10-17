package in.beforeyougo.location.importer;

import in.beforeyougo.location.entity.IngestionRun;
import in.beforeyougo.location.repository.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LGD CSV import service.
 * Uses EXAMPLE ONLY test data in src/test/resources/test-data/lgd/.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LgdImportServiceTest {

    @Autowired
    private LgdImportService importService;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private SubDistrictRepository subDistrictRepository;

    @Autowired
    private VillageRepository villageRepository;

    @Autowired
    private PincodeRepository pincodeRepository;

    @Autowired
    private IngestionRunRepository ingestionRunRepository;

    private Path testDataDir() throws URISyntaxException {
        return Paths.get(getClass().getClassLoader()
                .getResource("test-data/lgd/states.csv").toURI()).getParent();
    }

    @Test
    @Order(1)
    void importStates_createsAllStates() throws Exception {
        IngestionRun run = importService.importStates(testDataDir().resolve("states.csv"));

        assertEquals("COMPLETED", run.getStatus());
        assertEquals(5, run.getRecordsProcessed());
        assertEquals(0, run.getRecordsSkipped());
        assertEquals(5, stateRepository.count());

        // Verify Tamil Nadu
        var tn = stateRepository.findById(33);
        assertTrue(tn.isPresent());
        assertEquals("Tamil Nadu", tn.get().getNameEn());
        assertEquals("தமிழ்நாடு", tn.get().getNameLocal());
        assertEquals("STATE", tn.get().getKind());

        // Verify Delhi is UT
        var delhi = stateRepository.findById(7);
        assertTrue(delhi.isPresent());
        assertEquals("UT", delhi.get().getKind());
    }

    @Test
    @Order(2)
    void importStates_isIdempotent() throws Exception {
        // Import again — should update, not duplicate
        IngestionRun run = importService.importStates(testDataDir().resolve("states.csv"));

        assertEquals("COMPLETED", run.getStatus());
        assertEquals(5, stateRepository.count()); // still 5, not 10
    }

    @Test
    @Order(3)
    void importDistricts_createsAllDistricts() throws Exception {
        IngestionRun run = importService.importDistricts(testDataDir().resolve("districts.csv"));

        assertEquals("COMPLETED", run.getStatus());
        assertEquals(5, run.getRecordsProcessed());
        assertEquals(5, districtRepository.count());

        // Verify Chennai
        var chennai = districtRepository.findById(603);
        assertTrue(chennai.isPresent());
        assertEquals("Chennai", chennai.get().getNameEn());
        assertEquals("சென்னை", chennai.get().getNameLocal());
        assertEquals(33, chennai.get().getState().getLgdCode());
    }

    @Test
    @Order(4)
    void importSubDistricts_createsAll() throws Exception {
        IngestionRun run = importService.importSubDistricts(
                testDataDir().resolve("sub_districts.csv"));

        assertEquals("COMPLETED", run.getStatus());
        assertEquals(5, run.getRecordsProcessed());
        assertEquals(5, subDistrictRepository.count());
    }

    @Test
    @Order(5)
    void importVillages_createsAll() throws Exception {
        IngestionRun run = importService.importVillages(testDataDir().resolve("villages.csv"));

        assertEquals("COMPLETED", run.getStatus());
        assertEquals(6, run.getRecordsProcessed());
        assertEquals(6, villageRepository.count());

        // Verify Chromepet
        var chromepet = villageRepository.findById(260001);
        assertTrue(chromepet.isPresent());
        assertEquals("Chromepet", chromepet.get().getNameEn());
        assertEquals("குரோம்பேட்", chromepet.get().getNameLocal());
    }

    @Test
    @Order(6)
    void importPincodes_createsAll() throws Exception {
        IngestionRun run = importService.importPincodes(testDataDir().resolve("pincodes.csv"));

        assertEquals("COMPLETED", run.getStatus());
        assertEquals(6, run.getRecordsProcessed());
        assertEquals(6, pincodeRepository.count());

        // Verify pincode mapping
        var pc = pincodeRepository.findByPincode("600044");
        assertFalse(pc.isEmpty());
        assertEquals(260001, pc.get(0).getVillage().getLgdCode());
    }

    @Test
    @Order(7)
    void importPincodes_isIdempotent() throws Exception {
        long before = pincodeRepository.count();
        importService.importPincodes(testDataDir().resolve("pincodes.csv"));
        assertEquals(before, pincodeRepository.count()); // no duplicates
    }

    @Test
    @Order(8)
    void importAll_importsEverythingInOrder() throws Exception {
        // Clear all data first
        pincodeRepository.deleteAll();
        villageRepository.deleteAll();
        subDistrictRepository.deleteAll();
        districtRepository.deleteAll();
        stateRepository.deleteAll();

        List<IngestionRun> runs = importService.importAll(testDataDir());

        assertEquals(5, runs.size());
        runs.forEach(run -> assertEquals("COMPLETED", run.getStatus()));
        assertEquals(5, stateRepository.count());
        assertEquals(5, districtRepository.count());
        assertEquals(5, subDistrictRepository.count());
        assertEquals(6, villageRepository.count());
        assertEquals(6, pincodeRepository.count());
    }

    @Test
    @Order(9)
    void ingestionRuns_areTracked() {
        List<IngestionRun> stateRuns = ingestionRunRepository
                .findByRunTypeOrderByStartedAtDesc("LGD_STATES");
        assertFalse(stateRuns.isEmpty());
        assertEquals("COMPLETED", stateRuns.get(0).getStatus());
        assertNotNull(stateRuns.get(0).getCompletedAt());
    }
}
