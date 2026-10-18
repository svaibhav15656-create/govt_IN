package in.beforeyougo.location.controller;

import in.beforeyougo.location.entity.*;
import in.beforeyougo.location.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the location search API.
 * Uses EXAMPLE ONLY data seeded in setUp.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private StateRepository stateRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private SubDistrictRepository subDistrictRepository;
    @Autowired private VillageRepository villageRepository;
    @Autowired private PincodeRepository pincodeRepository;

    private boolean seeded = false;

    @BeforeEach
    void seedData() {
        if (seeded || stateRepository.count() > 0) {
            seeded = true;
            return;
        }

        // EXAMPLE ONLY data — not real LGD codes
        State tn = new State(33, "Tamil Nadu", "தமிழ்நாடு", "STATE");
        State kl = new State(32, "Kerala", "കേരളം", "STATE");
        stateRepository.save(tn);
        stateRepository.save(kl);

        District chennai = new District(603, tn, "Chennai", "சென்னை");
        District cbe = new District(604, tn, "Coimbatore", "கோயம்பத்தூர்");
        District tvm = new District(301, kl, "Thiruvananthapuram", "തിരുവനന്തപുരം");
        districtRepository.save(chennai);
        districtRepository.save(cbe);
        districtRepository.save(tvm);

        SubDistrict tambaram = new SubDistrict(6031, chennai, "Tambaram", "தாம்பரம்");
        SubDistrict cbeNorth = new SubDistrict(6041, cbe, "Coimbatore North", "கோயம்பத்தூர் வடக்கு");
        subDistrictRepository.save(tambaram);
        subDistrictRepository.save(cbeNorth);

        Village chromepet = new Village(260001, tambaram, "Chromepet", "குரோம்பேட்");
        Village pallavaram = new Village(260002, tambaram, "Pallavaram", "பல்லாவரம்");
        Village singanallur = new Village(260004, cbeNorth, "Singanallur", "சிங்கநல்லூர்");
        villageRepository.save(chromepet);
        villageRepository.save(pallavaram);
        villageRepository.save(singanallur);

        pincodeRepository.save(new Pincode("600044", chromepet, "LGD"));
        pincodeRepository.save(new Pincode("600043", pallavaram, "LGD"));
        pincodeRepository.save(new Pincode("641005", singanallur, "LGD"));

        seeded = true;
    }

    // --- Search by name ---

    @Test
    void search_queryTooShort_returnsEmpty() throws Exception {
        mockMvc.perform(get("/api/locations/search").param("q", "a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void search_stateByEnglishName_returnsMatch() throws Exception {
        mockMvc.perform(get("/api/locations/search").param("q", "Tamil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.level=='STATE')].nameEn",
                        hasItem("Tamil Nadu")));
    }

    @Test
    void search_stateByLocalScript_returnsMatch() throws Exception {
        mockMvc.perform(get("/api/locations/search").param("q", "தமிழ்"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.level=='STATE')].nameLocal",
                        hasItem("தமிழ்நாடு")));
    }

    @Test
    void search_districtByName_returnsWithPath() throws Exception {
        mockMvc.perform(get("/api/locations/search").param("q", "Chennai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.level=='DISTRICT')].path",
                        hasItem("Tamil Nadu > Chennai")));
    }

    @Test
    void search_villageByName_returnsWithFullPath() throws Exception {
        mockMvc.perform(get("/api/locations/search").param("q", "Chromepet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.level=='VILLAGE')].path",
                        hasItem("Tamil Nadu > Chennai > Tambaram > Chromepet")));
    }

    // --- Filter by level ---

    @Test
    void search_filterByLevel_returnsOnlyThatLevel() throws Exception {
        mockMvc.perform(get("/api/locations/search")
                        .param("q", "Tamil")
                        .param("level", "STATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].level", everyItem(is("STATE"))));
    }

    @Test
    void search_filterByDistrictLevel_excludesStates() throws Exception {
        mockMvc.perform(get("/api/locations/search")
                        .param("q", "Chennai")
                        .param("level", "DISTRICT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].level", everyItem(is("DISTRICT"))));
    }

    // --- Pincode search ---

    @Test
    void search_byExactPincode_returnsMapping() throws Exception {
        mockMvc.perform(get("/api/locations/search").param("q", "600044"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.level=='PINCODE')]", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.level=='PINCODE')].code", hasItem("600044")));
    }

    @Test
    void search_byPincodePrefix_returnsMatches() throws Exception {
        mockMvc.perform(get("/api/locations/search").param("q", "6000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.level=='PINCODE')]",
                        hasSize(greaterThanOrEqualTo(1))));
    }

    // --- Counts endpoint ---

    @Test
    void counts_returnsAllCounts() throws Exception {
        mockMvc.perform(get("/api/locations/counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.states").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.districts").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.villages").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.pincodes").value(greaterThanOrEqualTo(3)));
    }
}
