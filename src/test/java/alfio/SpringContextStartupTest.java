package alfio;

import static org.junit.jupiter.api.Assertions.*;

import alfio.config.DataSourceConfiguration;
import alfio.config.Initializer;
import alfio.manager.CheckInManager;
import alfio.manager.EventManager;
import alfio.manager.TicketReservationManager;
import alfio.test.util.AlfioIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@AlfioIntegrationTest
@ContextConfiguration(classes = {DataSourceConfiguration.class, TestConfiguration.class})
@ActiveProfiles({Initializer.PROFILE_DEV, Initializer.PROFILE_DISABLE_JOBS, Initializer.PROFILE_INTEGRATION_TEST})
class SpringContextStartupTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }

    @Test
    void coreManagersArePresent() {
        assertNotNull(context.getBean(CheckInManager.class));
        assertNotNull(context.getBean(EventManager.class));
        assertNotNull(context.getBean(TicketReservationManager.class));
    }
}
