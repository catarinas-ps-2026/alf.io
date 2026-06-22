/**
 * This file is part of alf.io.
 *
 * alf.io is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * alf.io is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with alf.io.  If not, see <http://www.gnu.org/licenses/>.
 */
package alfio.manager;

import static alfio.model.system.ConfigurationKeys.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import alfio.controller.form.ContactAndTicketsForm;
import alfio.controller.support.CustomBindingResult;
import alfio.manager.system.ConfigurationLevel;
import alfio.manager.system.ConfigurationManager;
import alfio.manager.system.ConfigurationManager.MaybeConfiguration;
import alfio.model.*;
import alfio.model.PurchaseContext.PurchaseContextType;
import alfio.model.extension.CustomTaxPolicy;
import alfio.model.system.ConfigurationKeyValuePathLevel;
import alfio.model.system.ConfigurationKeys;
import alfio.model.system.ConfigurationPathLevel;
import alfio.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

public class ReverseChargeManagerTest {

    private PromoCodeDiscountRepository promoCodeDiscountRepository;
    private AdditionalServiceItemRepository additionalServiceItemRepository;
    private AdditionalServiceRepository additionalServiceRepository;
    private TicketCategoryRepository ticketCategoryRepository;
    private NamedParameterJdbcTemplate jdbcTemplate;
    private ConfigurationManager configurationManager;
    private TicketReservationRepository ticketReservationRepository;
    private EuVatChecker vatChecker;
    private TicketReservationManager ticketReservationManager;
    private TicketRepository ticketRepository;
    private SubscriptionRepository subscriptionRepository;
    private AuditingRepository auditingRepository;

    private ReverseChargeManager reverseChargeManager;

    @BeforeEach
    void setUp() {
        promoCodeDiscountRepository = mock(PromoCodeDiscountRepository.class);
        additionalServiceItemRepository = mock(AdditionalServiceItemRepository.class);
        additionalServiceRepository = mock(AdditionalServiceRepository.class);
        ticketCategoryRepository = mock(TicketCategoryRepository.class);
        jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        configurationManager = mock(ConfigurationManager.class);
        ticketReservationRepository = mock(TicketReservationRepository.class);
        vatChecker = mock(EuVatChecker.class);
        ticketReservationManager = mock(TicketReservationManager.class);
        ticketRepository = mock(TicketRepository.class);
        subscriptionRepository = mock(SubscriptionRepository.class);
        auditingRepository = mock(AuditingRepository.class);

        reverseChargeManager = new ReverseChargeManager(
                promoCodeDiscountRepository,
                additionalServiceItemRepository,
                additionalServiceRepository,
                ticketCategoryRepository,
                jdbcTemplate,
                configurationManager,
                ticketReservationRepository,
                vatChecker,
                ticketReservationManager,
                ticketRepository,
                subscriptionRepository,
                auditingRepository);
    }

    private MaybeConfiguration mockConfig(ConfigurationKeys key, String value) {
        ConfigurationKeyValuePathLevel kv =
                new ConfigurationKeyValuePathLevel(key.name(), value, ConfigurationPathLevel.SYSTEM);
        return new MaybeConfiguration(key, kv);
    }

    private void mockReverseChargeConfig(boolean enabled, String countryOfBusiness, boolean inPerson, boolean online) {
        Map<ConfigurationKeys, MaybeConfiguration> configMap = new HashMap<>();
        configMap.put(ENABLE_EU_VAT_DIRECTIVE, mockConfig(ENABLE_EU_VAT_DIRECTIVE, String.valueOf(enabled)));
        configMap.put(
                COUNTRY_OF_BUSINESS,
                countryOfBusiness == null
                        ? new MaybeConfiguration(COUNTRY_OF_BUSINESS)
                        : mockConfig(COUNTRY_OF_BUSINESS, countryOfBusiness));
        configMap.put(
                ENABLE_REVERSE_CHARGE_IN_PERSON, mockConfig(ENABLE_REVERSE_CHARGE_IN_PERSON, String.valueOf(inPerson)));
        configMap.put(ENABLE_REVERSE_CHARGE_ONLINE, mockConfig(ENABLE_REVERSE_CHARGE_ONLINE, String.valueOf(online)));

        when(configurationManager.getFor(
                        eq(Set.of(
                                ENABLE_EU_VAT_DIRECTIVE,
                                COUNTRY_OF_BUSINESS,
                                ENABLE_REVERSE_CHARGE_IN_PERSON,
                                ENABLE_REVERSE_CHARGE_ONLINE)),
                        any()))
                .thenReturn(configMap);
    }

    private PurchaseContext mockPurchaseContext(
            PurchaseContextType type, String currency, PriceContainer.VatStatus vatStatus, BigDecimal vatPercentage) {
        PurchaseContext context;
        if (type == PurchaseContextType.event) {
            Event event = mock(Event.class);
            when(event.getId()).thenReturn(1);
            when(event.getFormat()).thenReturn(Event.EventFormat.IN_PERSON);
            when(event.event()).thenReturn(Optional.of(event));
            context = event;
        } else {
            context = mock(PurchaseContext.class);
            when(context.event()).thenReturn(Optional.empty());
        }

        when(context.getType()).thenReturn(type);
        when(context.getCurrency()).thenReturn(currency);
        when(context.getVatStatus()).thenReturn(vatStatus);
        when(context.getVat()).thenReturn(vatPercentage);
        when(context.getConfigurationLevel()).thenReturn(mock(ConfigurationLevel.class));
        when(context.ofType(any(PurchaseContextType.class))).thenAnswer(invocation -> {
            PurchaseContextType t = invocation.getArgument(0);
            return t == type;
        });

        return context;
    }

    private TicketReservation mockTicketReservation() {
        TicketReservation reservation = mock(TicketReservation.class);
        when(reservation.getId()).thenReturn("resId");
        when(reservation.getCurrencyCode()).thenReturn("USD");
        when(reservation.getVatStatus()).thenReturn(PriceContainer.VatStatus.INCLUDED);
        when(reservation.getSrcPriceCts()).thenReturn(1000);
        when(reservation.getVatCountryCode()).thenReturn("IT");
        when(reservation.getVatNr()).thenReturn("IT123456789");
        when(reservation.withVatStatus(any())).thenReturn(reservation);
        return reservation;
    }

    @Test
    public void testCheckAndApplyVATRules_ReverseChargeDisabled() {
        mockReverseChargeConfig(false, "IT", true, true);
        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        when(ticketReservationRepository.findOptionalReservationById("resId")).thenReturn(Optional.empty());

        ContactAndTicketsForm form = new ContactAndTicketsForm();
        form.setVatCountryCode("DE");
        form.setVatNr("DE123456");

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        reverseChargeManager.checkAndApplyVATRules(context, "resId", form, bindingResult);
        assertFalse(bindingResult.hasErrors());
        verify(ticketReservationRepository).findOptionalReservationById("resId");
    }

    @Test
    public void testCheckAndApplyVATRules_ReverseChargeEnabled_MissingVatNrForEU() {
        mockReverseChargeConfig(true, "IT", true, true);
        when(configurationManager.getForSystem(EU_COUNTRIES_LIST))
                .thenReturn(mockConfig(EU_COUNTRIES_LIST, "IT,DE,FR"));

        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        when(ticketReservationRepository.findOptionalReservationById("resId")).thenReturn(Optional.empty());

        ContactAndTicketsForm form = new ContactAndTicketsForm();
        form.setVatCountryCode("DE");
        form.setVatNr(""); // empty

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        reverseChargeManager.checkAndApplyVATRules(context, "resId", form, bindingResult);
        assertTrue(bindingResult.hasErrors());
        assertNotNull(bindingResult.getFieldError("vatNr"));
        assertEquals("error.emptyField", bindingResult.getFieldError("vatNr").getCode());
    }

    @Test
    public void testCheckAndApplyVATRules_ReverseChargeEnabled_NonEUCountry() {
        mockReverseChargeConfig(true, "IT", true, true);
        when(configurationManager.getForSystem(EU_COUNTRIES_LIST))
                .thenReturn(mockConfig(EU_COUNTRIES_LIST, "IT,DE,FR"));

        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        when(ticketReservationRepository.findOptionalReservationById("resId")).thenReturn(Optional.empty());

        ContactAndTicketsForm form = new ContactAndTicketsForm();
        form.setVatCountryCode("US"); // Non-EU
        form.setVatNr(""); // empty but not EU

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        reverseChargeManager.checkAndApplyVATRules(context, "resId", form, bindingResult);
        assertFalse(bindingResult.hasErrors());
    }

    @Test
    public void testCheckAndApplyVATRules_VatCheckerFailure() {
        mockReverseChargeConfig(true, "IT", true, true);
        when(configurationManager.getForSystem(EU_COUNTRIES_LIST))
                .thenReturn(mockConfig(EU_COUNTRIES_LIST, "IT,DE,FR"));

        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        TicketReservation reservation = mockTicketReservation();
        when(ticketReservationRepository.findOptionalReservationById("resId")).thenReturn(Optional.of(reservation));
        when(ticketCategoryRepository.findCategoriesInReservation("resId")).thenReturn(List.of());

        ContactAndTicketsForm form = new ContactAndTicketsForm();
        form.setVatCountryCode("DE");
        form.setVatNr("DE123456");

        when(vatChecker.isReverseChargeEnabledFor(context)).thenReturn(true);
        when(vatChecker.checkVat(anyString(), anyString(), any())).thenThrow(new IllegalStateException("VIES Down"));

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        reverseChargeManager.checkAndApplyVATRules(context, "resId", form, bindingResult);
        assertTrue(bindingResult.hasErrors());
        assertNotNull(bindingResult.getFieldError("vatNr"));
        assertEquals("error.vatVIESDown", bindingResult.getFieldError("vatNr").getCode());
    }

    @Test
    public void testCheckAndApplyVATRules_VatDetailInvalid() {
        mockReverseChargeConfig(true, "IT", true, true);
        when(configurationManager.getForSystem(EU_COUNTRIES_LIST))
                .thenReturn(mockConfig(EU_COUNTRIES_LIST, "IT,DE,FR"));

        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        TicketReservation reservation = mockTicketReservation();
        when(ticketReservationRepository.findOptionalReservationById("resId")).thenReturn(Optional.of(reservation));
        when(ticketCategoryRepository.findCategoriesInReservation("resId")).thenReturn(List.of());

        ContactAndTicketsForm form = new ContactAndTicketsForm();
        form.setVatCountryCode("DE");
        form.setVatNr("DE123456");

        when(vatChecker.isReverseChargeEnabledFor(context)).thenReturn(true);
        VatDetail invalidDetail = new VatDetail("DE123456", "DE", false, null, null, null, false);
        when(vatChecker.checkVat("DE123456", "DE", context)).thenReturn(Optional.of(invalidDetail));

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        reverseChargeManager.checkAndApplyVATRules(context, "resId", form, bindingResult);
        assertTrue(bindingResult.hasErrors());
        assertNotNull(bindingResult.getFieldError("vatNr"));
        assertEquals(
                "error.STEP_2_INVALID_VAT", bindingResult.getFieldError("vatNr").getCode());
    }

    @Test
    public void testCheckAndApplyVATRules_Success_Event_ReverseChargeBothEnabled() {
        mockReverseChargeConfig(true, "IT", true, true);
        when(configurationManager.getForSystem(EU_COUNTRIES_LIST))
                .thenReturn(mockConfig(EU_COUNTRIES_LIST, "IT,DE,FR"));

        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        TicketReservation reservation = mockTicketReservation();
        when(ticketReservationRepository.findOptionalReservationById("resId")).thenReturn(Optional.of(reservation));

        TicketCategory category = mock(TicketCategory.class);
        when(category.getId()).thenReturn(101);
        when(category.getSrcPriceCts()).thenReturn(1000);
        when(category.getTicketAccessType()).thenReturn(TicketCategory.TicketAccessType.IN_PERSON);
        when(ticketCategoryRepository.findCategoriesInReservation("resId")).thenReturn(List.of(category));
        when(configurationManager.getCategoriesWithNoTaxes(anyList())).thenReturn(List.of());

        ContactAndTicketsForm form = new ContactAndTicketsForm();
        form.setVatCountryCode("DE");
        form.setVatNr("DE123456");
        form.setInvoiceRequested(true);

        when(vatChecker.isReverseChargeEnabledFor(context)).thenReturn(true);
        VatDetail validDetail = new VatDetail("DE123456", "DE", true, "Company", "Address", VatDetail.Type.VIES, true);
        when(vatChecker.checkVat("DE123456", "DE", context)).thenReturn(Optional.of(validDetail));

        when(ticketReservationManager.findById("resId")).thenReturn(Optional.of(reservation));
        when(ticketReservationManager.findTicketsInReservation("resId")).thenReturn(List.of());
        when(subscriptionRepository.findSubscriptionsByReservationId("resId")).thenReturn(List.of());
        when(subscriptionRepository.findAppliedSubscriptionByReservationId("resId"))
                .thenReturn(Optional.empty());

        when(ticketRepository.updateTicketPriceForCategoryInReservation()).thenReturn("UPDATE...");
        when(jdbcTemplate.batchUpdate(anyString(), any(MapSqlParameterSource[].class)))
                .thenReturn(new int[] {1});

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        reverseChargeManager.checkAndApplyVATRules(context, "resId", form, bindingResult);
        assertFalse(bindingResult.hasErrors());

        verify(ticketRepository).updateVatStatusForReservation("resId", PriceContainer.VatStatus.INCLUDED_EXEMPT);
        verify(jdbcTemplate).batchUpdate(eq("UPDATE..."), any(MapSqlParameterSource[].class));
        verify(ticketReservationRepository)
                .updateBillingData(
                        eq(PriceContainer.VatStatus.INCLUDED_EXEMPT),
                        eq(1000),
                        anyInt(),
                        anyInt(),
                        anyInt(),
                        eq("USD"),
                        eq("DE123456"),
                        eq("DE"),
                        eq(true),
                        eq("resId"));
        verify(vatChecker).logSuccessfulValidation(validDetail, "resId", context);
    }

    @Test
    public void testCheckAndApplyVATRules_Success_Event_SplitReverseCharge() {
        mockReverseChargeConfig(true, "IT", true, false); // inPerson=true, online=false
        when(configurationManager.getForSystem(EU_COUNTRIES_LIST))
                .thenReturn(mockConfig(EU_COUNTRIES_LIST, "IT,DE,FR"));

        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        Event event = (Event) context;
        when(event.getFormat()).thenReturn(Event.EventFormat.HYBRID);

        TicketReservation reservation = mockTicketReservation();
        when(ticketReservationRepository.findOptionalReservationById("resId")).thenReturn(Optional.of(reservation));

        TicketCategory categoryInPerson = mock(TicketCategory.class);
        when(categoryInPerson.getId()).thenReturn(101);
        when(categoryInPerson.getSrcPriceCts()).thenReturn(1000);
        when(categoryInPerson.getTicketAccessType()).thenReturn(TicketCategory.TicketAccessType.IN_PERSON);

        TicketCategory categoryOnline = mock(TicketCategory.class);
        when(categoryOnline.getId()).thenReturn(102);
        when(categoryOnline.getSrcPriceCts()).thenReturn(500);
        when(categoryOnline.getTicketAccessType()).thenReturn(TicketCategory.TicketAccessType.ONLINE);

        when(ticketCategoryRepository.findCategoriesInReservation("resId"))
                .thenReturn(List.of(categoryInPerson, categoryOnline));
        when(configurationManager.getCategoriesWithNoTaxes(anyList())).thenReturn(List.of());

        ContactAndTicketsForm form = new ContactAndTicketsForm();
        form.setVatCountryCode("DE");
        form.setVatNr("DE123456");
        form.setInvoiceRequested(false);

        when(vatChecker.isReverseChargeEnabledFor(context)).thenReturn(true);
        VatDetail validDetail = new VatDetail("DE123456", "DE", true, "Company", "Address", VatDetail.Type.VIES, true);
        when(vatChecker.checkVat("DE123456", "DE", context)).thenReturn(Optional.of(validDetail));

        when(ticketReservationManager.findById("resId")).thenReturn(Optional.of(reservation));
        when(ticketReservationManager.findTicketsInReservation("resId")).thenReturn(List.of());

        when(ticketRepository.updateTicketPriceForCategoryInReservation()).thenReturn("UPDATE...");
        when(jdbcTemplate.batchUpdate(anyString(), any(MapSqlParameterSource[].class)))
                .thenReturn(new int[] {1});

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        reverseChargeManager.checkAndApplyVATRules(context, "resId", form, bindingResult);
        assertFalse(bindingResult.hasErrors());

        // For split reverse charge, ticketRepository.updateVatStatusForReservation is NOT called.
        verify(ticketRepository, never()).updateVatStatusForReservation(anyString(), any());
        verify(jdbcTemplate).batchUpdate(eq("UPDATE..."), any(MapSqlParameterSource[].class));
        verify(ticketReservationRepository)
                .updateBillingData(
                        eq(PriceContainer.VatStatus.INCLUDED), // original vat status used
                        eq(1000),
                        anyInt(),
                        anyInt(),
                        anyInt(),
                        eq("USD"),
                        eq("DE123456"),
                        eq("DE"),
                        eq(false),
                        eq("resId"));
    }

    @Test
    public void testCheckAndApplyVATRules_Success_Subscription() {
        mockReverseChargeConfig(true, "IT", true, true);
        when(configurationManager.getForSystem(EU_COUNTRIES_LIST))
                .thenReturn(mockConfig(EU_COUNTRIES_LIST, "IT,DE,FR"));

        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.subscription, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        TicketReservation reservation = mockTicketReservation();
        when(ticketReservationRepository.findOptionalReservationById("resId")).thenReturn(Optional.of(reservation));

        ContactAndTicketsForm form = new ContactAndTicketsForm();
        form.setVatCountryCode("DE");
        form.setVatNr("DE123456");

        when(vatChecker.isReverseChargeEnabledFor(context)).thenReturn(true);
        VatDetail validDetail = new VatDetail("DE123456", "DE", true, "Company", "Address", VatDetail.Type.VIES, true);
        when(vatChecker.checkVat("DE123456", "DE", context)).thenReturn(Optional.of(validDetail));

        when(ticketReservationManager.findById("resId")).thenReturn(Optional.of(reservation));
        when(ticketReservationManager.findTicketsInReservation("resId")).thenReturn(List.of());

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        reverseChargeManager.checkAndApplyVATRules(context, "resId", form, bindingResult);
        assertFalse(bindingResult.hasErrors());

        verify(ticketRepository, never()).updateVatStatusForReservation(anyString(), any());
        verify(jdbcTemplate, never()).batchUpdate(anyString(), any(MapSqlParameterSource[].class));
        verify(ticketReservationRepository)
                .updateBillingData(
                        eq(PriceContainer.VatStatus.INCLUDED_EXEMPT),
                        eq(1000),
                        anyInt(),
                        anyInt(),
                        anyInt(),
                        eq("USD"),
                        eq("DE123456"),
                        eq("DE"),
                        eq(false),
                        eq("resId"));
    }

    @Test
    public void testCheckAndApplyVATRules_ItalySplitPayment() {
        mockReverseChargeConfig(true, "IT", true, true);
        when(configurationManager.getForSystem(EU_COUNTRIES_LIST))
                .thenReturn(mockConfig(EU_COUNTRIES_LIST, "IT,DE,FR"));

        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        TicketReservation reservation = mockTicketReservation();
        when(ticketReservationRepository.findOptionalReservationById("resId")).thenReturn(Optional.of(reservation));
        when(ticketCategoryRepository.findCategoriesInReservation("resId")).thenReturn(List.of());

        ContactAndTicketsForm form = new ContactAndTicketsForm();
        form.setVatCountryCode("IT");
        form.setVatNr("IT123456789");
        form.setItalyEInvoicingSplitPayment(true);

        when(vatChecker.isReverseChargeEnabledFor(context)).thenReturn(false); // returns empty vatDetail

        when(ticketReservationManager.findTicketsInReservation("resId")).thenReturn(List.of());

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        reverseChargeManager.checkAndApplyVATRules(context, "resId", form, bindingResult);
        assertFalse(bindingResult.hasErrors());

        verify(ticketReservationRepository)
                .updateBillingData(
                        eq(PriceContainer.VatStatus.INCLUDED_NOT_CHARGED),
                        eq(1000),
                        anyInt(),
                        anyInt(),
                        anyInt(),
                        eq("USD"),
                        eq("IT123456789"),
                        eq("IT"),
                        eq(false),
                        eq("resId"));
    }

    @Test
    public void testCheckAndApplyVATRules_NoTaxCategories() {
        mockReverseChargeConfig(true, "IT", true, true);
        when(configurationManager.getForSystem(EU_COUNTRIES_LIST))
                .thenReturn(mockConfig(EU_COUNTRIES_LIST, "IT,DE,FR"));

        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        TicketReservation reservation = mockTicketReservation();
        when(ticketReservationRepository.findOptionalReservationById("resId")).thenReturn(Optional.of(reservation));

        TicketCategory category = mock(TicketCategory.class);
        when(category.getId()).thenReturn(101);
        when(category.getSrcPriceCts()).thenReturn(1000);
        when(ticketCategoryRepository.findCategoriesInReservation("resId")).thenReturn(List.of(category));

        when(configurationManager.getCategoriesWithNoTaxes(List.of(101))).thenReturn(List.of(101));

        ContactAndTicketsForm form = new ContactAndTicketsForm();
        form.setVatCountryCode("DE");
        form.setVatNr("DE123456");

        when(vatChecker.isReverseChargeEnabledFor(context)).thenReturn(false); // returns empty vatDetail

        when(ticketReservationManager.findTicketsInReservation("resId")).thenReturn(List.of());

        when(ticketRepository.updateTicketPriceForCategoryInReservation()).thenReturn("UPDATE...");
        when(jdbcTemplate.batchUpdate(anyString(), any(MapSqlParameterSource[].class)))
                .thenReturn(new int[] {1});

        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        reverseChargeManager.checkAndApplyVATRules(context, "resId", form, bindingResult);
        assertFalse(bindingResult.hasErrors());

        verify(jdbcTemplate).batchUpdate(eq("UPDATE..."), any(MapSqlParameterSource[].class));
        verify(ticketReservationRepository)
                .updateBillingData(
                        eq(PriceContainer.VatStatus.INCLUDED),
                        eq(1000),
                        anyInt(),
                        anyInt(),
                        anyInt(),
                        eq("USD"),
                        eq("DE123456"),
                        eq("DE"),
                        eq(false),
                        eq("resId"));
    }

    @Test
    public void testApplyCustomTaxPolicy_ThrowsIfNotEvent() {
        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.subscription, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        CustomTaxPolicy policy = new CustomTaxPolicy(List.of());
        ContactAndTicketsForm form = new ContactAndTicketsForm();
        CustomBindingResult bindingResult = new CustomBindingResult(new BeanPropertyBindingResult(form, "form"));

        assertThrows(IllegalStateException.class, () -> {
            reverseChargeManager.applyCustomTaxPolicy(context, policy, "resId", form, bindingResult);
        });
    }

    @Test
    public void testApplyCustomTaxPolicy_TicketNotInReservation() {
        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        TicketReservation reservation = mockTicketReservation();
        when(ticketReservationManager.findById("resId")).thenReturn(Optional.of(reservation));
        when(ticketRepository.findTicketsInReservation("resId")).thenReturn(List.of()); // empty reservation tickets

        CustomTaxPolicy policy = new CustomTaxPolicy(
                List.of(new CustomTaxPolicy.TicketTaxPolicy("uuid-1", PriceContainer.VatStatus.INCLUDED_EXEMPT)));
        ContactAndTicketsForm form = new ContactAndTicketsForm();
        CustomBindingResult bindingResult = new CustomBindingResult(new BeanPropertyBindingResult(form, "form"));

        reverseChargeManager.applyCustomTaxPolicy(context, policy, "resId", form, bindingResult);
        assertTrue(bindingResult.hasErrors());
        assertEquals("error.generic", bindingResult.getGlobalError().getCode());
    }

    @Test
    public void testApplyCustomTaxPolicy_Success() {
        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        TicketReservation reservation = mockTicketReservation();
        when(ticketReservationManager.findById("resId")).thenReturn(Optional.of(reservation));

        Ticket ticket = mock(Ticket.class);
        UUID publicUuid = UUID.randomUUID();
        when(ticket.getPublicUuid()).thenReturn(publicUuid);
        when(ticket.getUuid()).thenReturn(publicUuid.toString());
        when(ticket.getCategoryId()).thenReturn(101);
        when(ticket.getSrcPriceCts()).thenReturn(1000);
        when(ticket.getVatStatus()).thenReturn(PriceContainer.VatStatus.INCLUDED);
        when(ticket.withVatStatus(any())).thenReturn(ticket);

        when(ticketRepository.findTicketsInReservation("resId")).thenReturn(List.of(ticket));
        when(ticketReservationManager.findTicketsInReservation("resId")).thenReturn(List.of(ticket));

        CustomTaxPolicy policy = new CustomTaxPolicy(List.of(
                new CustomTaxPolicy.TicketTaxPolicy(publicUuid.toString(), PriceContainer.VatStatus.INCLUDED_EXEMPT)));
        ContactAndTicketsForm form = new ContactAndTicketsForm();
        CustomBindingResult bindingResult = new CustomBindingResult(new BeanPropertyBindingResult(form, "form"));

        when(ticketRepository.bulkUpdateTicketPrice()).thenReturn("UPDATE...");
        when(jdbcTemplate.batchUpdate(anyString(), any(MapSqlParameterSource[].class)))
                .thenReturn(new int[] {1});

        reverseChargeManager.applyCustomTaxPolicy(context, policy, "resId", form, bindingResult);
        assertFalse(bindingResult.hasErrors());

        verify(auditingRepository)
                .insert(
                        eq("resId"),
                        any(),
                        eq(context),
                        eq(Audit.EventType.VAT_CUSTOM_CONFIGURATION_APPLIED),
                        any(),
                        any(),
                        eq("resId"),
                        any());
        verify(jdbcTemplate).batchUpdate(eq("UPDATE..."), any(MapSqlParameterSource[].class));
        verify(ticketReservationRepository)
                .updateBillingData(
                        eq(PriceContainer.VatStatus.INCLUDED),
                        eq(1000),
                        anyInt(),
                        anyInt(),
                        anyInt(),
                        eq("USD"),
                        any(),
                        any(),
                        eq(false),
                        eq("resId"));
    }

    @Test
    public void testResetVat_Event() {
        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.event, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);
        TicketReservation reservation = mockTicketReservation();
        when(ticketReservationRepository.findReservationById("resId")).thenReturn(reservation);

        TicketCategory category = mock(TicketCategory.class);
        when(category.getId()).thenReturn(101);
        when(category.getSrcPriceCts()).thenReturn(1000);
        when(ticketCategoryRepository.findCategoriesInReservation("resId")).thenReturn(List.of(category));

        when(ticketRepository.updateTicketPriceForCategoryInReservation()).thenReturn("UPDATE...");
        when(jdbcTemplate.batchUpdate(anyString(), any(MapSqlParameterSource[].class)))
                .thenReturn(new int[] {1});

        reverseChargeManager.resetVat(context, "resId");

        verify(ticketRepository).updateVatStatusForReservation("resId", PriceContainer.VatStatus.INCLUDED);
        verify(jdbcTemplate).batchUpdate(eq("UPDATE..."), any(MapSqlParameterSource[].class));
    }

    @Test
    public void testResetVat_Subscription() {
        PurchaseContext context = mockPurchaseContext(
                PurchaseContextType.subscription, "USD", PriceContainer.VatStatus.INCLUDED, BigDecimal.TEN);

        reverseChargeManager.resetVat(context, "resId");

        verifyNoInteractions(ticketReservationRepository);
        verifyNoInteractions(ticketRepository);
    }
}
