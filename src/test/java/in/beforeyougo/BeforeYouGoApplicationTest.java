package in.beforeyougo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifies the Spring context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class BeforeYouGoApplicationTest {

    @Test
    void contextLoads() {
        // If this test passes, the application context starts without errors.
    }
}
