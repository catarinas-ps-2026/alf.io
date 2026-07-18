package alfio.manager.system;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import alfio.model.Configurable;
import alfio.model.Event;
import alfio.model.system.ConfigurationKeys;
import alfio.repository.EventRepository;
import alfio.test.util.AlfioIntegrationTest;
import alfio.test.util.IntegrationTestUtil;
import alfio.util.BaseIntegrationTest;
import java.util.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import alfio.TestConfiguration;
import alfio.config.DataSourceConfiguration;

@AlfioIntegrationTest
@ContextConfiguration(classes = {DataSourceConfiguration.class, TestConfiguration.class})
@ActiveProfiles({
    alfio.config.Initializer.PROFILE_DEV,
    alfio.config.Initializer.PROFILE_DISABLE_JOBS,
    alfio.config.Initializer.PROFILE_INTEGRATION_TEST
})
class SmtpMailIntegrationTest extends BaseIntegrationTest {

    private static final Map<String, String> DESCRIPTION = Collections.singletonMap("en", "desc");

    @Autowired
    private Mailer mailer;

    @Autowired
    private alfio.manager.system.ConfigurationManager configurationManager;

    @Autowired
    private alfio.repository.system.ConfigurationRepository configurationRepository;

    @Autowired
    private alfio.manager.EventManager eventManager;

    @Autowired
    private alfio.manager.user.UserManager userManager;

    @Autowired
    private alfio.repository.user.OrganizationRepository organizationRepository;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void configureSmtp() {
        String smtpUsername = System.getenv("SMTP_USERNAME");
        String smtpPassword = System.getenv("SMTP_PASSWORD");
        Assumptions.assumeTrue(
            smtpUsername != null && smtpPassword != null,
            "Skipping SMTP test: SMTP_USERNAME and SMTP_PASSWORD environment variables not set."
        );

        IntegrationTestUtil.ensureMinimalConfiguration(configurationRepository);

        configurationManager.saveSystemConfiguration(ConfigurationKeys.MAILER_TYPE, "smtp");
        configurationManager.saveSystemConfiguration(ConfigurationKeys.SMTP_HOST, "smtp.gmail.com");
        configurationManager.saveSystemConfiguration(ConfigurationKeys.SMTP_PORT, "465");
        configurationManager.saveSystemConfiguration(ConfigurationKeys.SMTP_PROTOCOL, "smtps");
        configurationManager.saveSystemConfiguration(ConfigurationKeys.SMTP_USERNAME, smtpUsername);
        configurationManager.saveSystemConfiguration(ConfigurationKeys.SMTP_PASSWORD, smtpPassword);
        configurationManager.saveSystemConfiguration(ConfigurationKeys.SMTP_FROM_EMAIL, "tickets@ynoacaminome.me");
        configurationManager.saveSystemConfiguration(ConfigurationKeys.SMTP_PROPERTIES, "mail.smtps.ssl.enable=true");
    }

    @Test
    void sendEmailViaSmtp() {
        String smtpUsername = System.getenv("SMTP_USERNAME");
        Assumptions.assumeTrue(smtpUsername != null, "Skipping: SMTP_USERNAME not set.");

        // 1. Create a minimal event to use as Configurable
        var categories = List.of(
            new alfio.model.modification.TicketCategoryModification(
                null, "Standard",
                alfio.model.TicketCategory.TicketAccessType.INHERIT,
                20,
                new alfio.model.modification.DateTimeModification(
                    java.time.LocalDate.now(alfio.util.ClockProvider.clock()),
                    java.time.LocalTime.now(alfio.util.ClockProvider.clock())),
                new alfio.model.modification.DateTimeModification(
                    java.time.LocalDate.now(alfio.util.ClockProvider.clock()).plusDays(30),
                    java.time.LocalTime.now(alfio.util.ClockProvider.clock())),
                DESCRIPTION,
                java.math.BigDecimal.ZERO,
                false, "", false, null, null, null, null, null,
                0, null, null, alfio.model.metadata.AlfioMetadata.empty()));
        var eventPair = IntegrationTestUtil.initEvent(
            categories, organizationRepository, userManager, eventManager, eventRepository);
        Event event = eventPair.getLeft();

        // 2. Send email via SMTP (directly through Mailer interface)
        assertDoesNotThrow(() ->
            mailer.send(
                event,
                "alf.io Test",
                "christianmz565@gmail.com",
                null,
                "alf.io Integration Test - SMTP Verification",
                "This is an automated test email sent from alf.io integration tests via Gmail SMTP.",
                Optional.of("<html><body><h2>alf.io SMTP Integration Test</h2><p>This email was sent automatically by the <b>SmtpMailIntegrationTest</b> to verify SMTP integration.</p><p>Timestamp: " + java.time.Instant.now() + "</p></body></html>")
            ),
            "Sending email via SMTP should not throw an exception"
        );
    }
}
