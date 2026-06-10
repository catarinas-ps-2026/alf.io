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

package alfio.controller.api.admin;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import alfio.manager.AccessService;
import alfio.manager.EventManager;
import alfio.manager.PromoCodeRequestManager;
import alfio.model.EventAndOrganizationId;
import alfio.model.PromoCodeDiscount;
import alfio.model.PromoCodeUsageResult;
import alfio.model.modification.DateTimeModification;
import alfio.model.modification.PromoCodeDiscountModification;
import alfio.model.modification.PromoCodeDiscountWithFormattedTimeAndAmount;
import alfio.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class PromoCodeDiscountApiControllerTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventManager eventManager;

    @Mock
    private PromoCodeRequestManager promoCodeRequestManager;

    @Mock
    private AccessService accessService;

    @Mock
    private Principal principal;

    private PromoCodeDiscountApiController controller;

    @BeforeEach
    void setUp() {
        controller = new PromoCodeDiscountApiController(
            eventRepository,
            eventManager,
            promoCodeRequestManager,
            accessService
        );
    }

    @Test
    void addPromoCode_withEventIdAndValidData_addsPromoCode() {
        PromoCodeDiscountModification modification =
            new PromoCodeDiscountModification(
                1,
                1,
                "CODE123",
                new DateTimeModification(
                    LocalDate.of(2024, 1, 1),
                    LocalTime.of(0, 0)
                ),
                new DateTimeModification(
                    LocalDate.of(2024, 12, 31),
                    LocalTime.of(23, 59)
                ),
                BigDecimal.TEN,
                "EUR",
                PromoCodeDiscount.DiscountType.PERCENTAGE,
                List.of(1, 2),
                0,
                100,
                "Test promo",
                null,
                PromoCodeDiscount.CodeType.DISCOUNT,
                null
            );
        when(eventRepository.getZoneIdByEventId(1)).thenReturn(ZoneId.of("UTC"));

        controller.addPromoCode(modification, principal);

        verify(accessService).checkAccessToPromoCodeEventOrganization(principal, 1, 1);
        verify(eventManager).addPromoCode(
            eq("CODE123"),
            eq(1),
            eq(1),
            any(ZonedDateTime.class),
            any(ZonedDateTime.class),
            eq(10),
            eq(PromoCodeDiscount.DiscountType.PERCENTAGE),
            eq(List.of(1,2)),
            eq(100),
            eq("Test promo"),
            isNull(),
            eq(PromoCodeDiscount.CodeType.DISCOUNT),
            isNull(),
            eq("EUR")
        );
    }

    @Test
    void addPromoCode_withOrganizationIdOnly_usesUtcOffset() {
        PromoCodeDiscountModification modification =
            new PromoCodeDiscountModification(
                1, // organizationId
                null, // eventId
                "CODE123",
                new DateTimeModification(
                    LocalDate.of(2024, 1, 1),
                    LocalTime.of(0, 0)
                ),
                new DateTimeModification(
                    LocalDate.of(2024, 12, 31),
                    LocalTime.of(23, 59)
                ),
                BigDecimal.valueOf(50),
                "EUR", // currencyCode
                PromoCodeDiscount.DiscountType.FIXED_AMOUNT,
                List.of(),
                3600,
                50,
                null,
                null,
                PromoCodeDiscount.CodeType.DISCOUNT,
                null
            );
        
        controller.addPromoCode(modification, principal);

        verify(accessService).checkAccessToPromoCodeEventOrganization(principal, null, 1);
        verify(eventManager).addPromoCode(
            eq("CODE123"),
            isNull(),
            eq(1),
            any(ZonedDateTime.class),
            any(ZonedDateTime.class),
            eq(5000),
            eq(PromoCodeDiscount.DiscountType.FIXED_AMOUNT),
            anyList(),
            anyInt(),
            isNull(),
            isNull(),
            eq(PromoCodeDiscount.CodeType.DISCOUNT),
            isNull(),
            eq("EUR")
        );
    }

    @Test
    void addPromoCode_withCurrencySupportedAndMismatchingCurrency_throwsValidationException() {
        PromoCodeDiscountModification modification =
            new PromoCodeDiscountModification(
                1, // organizationId
                1, // eventId
                "CODE123",
                new DateTimeModification(
                    LocalDate.of(2024, 1, 1),
                    LocalTime.of(0, 0)
                ),
                new DateTimeModification(
                    LocalDate.of(2024, 12, 31),
                    LocalTime.of(23, 59)
                ),
                BigDecimal.TEN,
                "USD",
                PromoCodeDiscount.DiscountType.FIXED_AMOUNT,
                List.of(),
                0,
                100,
                null,
                null,
                PromoCodeDiscount.CodeType.DISCOUNT,
                null
            );

        when(eventRepository.getEventCurrencyCode(1)).thenReturn("EUR");
        when(eventRepository.getZoneIdByEventId(1)).thenReturn(ZoneId.of("UTC"));

        assertThrows(IllegalArgumentException.class, () ->
            controller.addPromoCode(modification, principal)
        );
    }

    @Test
    void addPromoCode_withCurrencySupportedAndMatchingCurrency_succeeds() {
        PromoCodeDiscountModification modification =
            new PromoCodeDiscountModification(
                1, // organizationId
                1, // eventId
                "CODE123",
                new DateTimeModification(
                    LocalDate.of(2024, 1, 1),
                    LocalTime.of(0, 0)
                ),
                new DateTimeModification(
                    LocalDate.of(2024, 12, 31),
                    LocalTime.of(23, 59)
                ),
                BigDecimal.TEN,
                "EUR",
                PromoCodeDiscount.DiscountType.PERCENTAGE,
                List.of(),
                0,
                100,
                null,
                null,
                PromoCodeDiscount.CodeType.DISCOUNT,
                null
            );

        when(eventRepository.getZoneIdByEventId(1)).thenReturn(ZoneId.of("UTC"));

        controller.addPromoCode(modification, principal);

        verify(eventManager).addPromoCode(
            anyString(),
            any(),
            any(),
            any(ZonedDateTime.class),
            any(ZonedDateTime.class),
            anyInt(),
            any(),
            anyList(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void addPromoCode_withAllFields_passesAllToEventManager() {
        PromoCodeDiscountModification modification =
            new PromoCodeDiscountModification(
                1, // organizationId
                1, // eventId
                "CODE123",
                new DateTimeModification(
                    LocalDate.of(2024, 1, 1),
                    LocalTime.of(0, 0)
                ),
                new DateTimeModification(
                    LocalDate.of(2024, 12, 31),
                    LocalTime.of(23, 59)
                ),
                BigDecimal.valueOf(15),
                "EUR",
                PromoCodeDiscount.DiscountType.PERCENTAGE,
                List.of(1, 2, 3),
                0,
                200,
                "Test promo",
                "ref@example.com",
                PromoCodeDiscount.CodeType.DISCOUNT,
                4
            );

        when(eventRepository.getZoneIdByEventId(1)).thenReturn(ZoneId.of("UTC"));

        controller.addPromoCode(modification, principal);

        verify(eventManager).addPromoCode(
            eq("CODE123"),
            eq(1),
            eq(1),
            any(ZonedDateTime.class),
            any(ZonedDateTime.class),
            eq(15),
            eq(PromoCodeDiscount.DiscountType.PERCENTAGE),
            eq(List.of(1,2,3)),
            eq(200),
            eq("Test promo"),
            eq("ref@example.com"),
            eq(PromoCodeDiscount.CodeType.DISCOUNT),
            eq(4),
            anyString()
        );
    }

    @Test
    void updatePromoCode_withValidData_updates() {
        PromoCodeDiscountModification modification =
            new PromoCodeDiscountModification(
                1, // organizationId
                1, // eventId
                "CODE123",
                new DateTimeModification(
                    LocalDate.of(2024, 1, 1),
                    LocalTime.of(0, 0)
                ),
                new DateTimeModification(
                    LocalDate.of(2024, 12, 31),
                    LocalTime.of(23, 59)
                ),
                BigDecimal.TEN,
                null, // currencyCode
                PromoCodeDiscount.DiscountType.PERCENTAGE,
                List.of(),
                0,
                100,
                null, // description
                null, // emailReference
                PromoCodeDiscount.CodeType.DISCOUNT,
                null // hiddenCategoryId
            );

        PromoCodeDiscount existingCode = mock(PromoCodeDiscount.class);
        when(existingCode.getEventId()).thenReturn(1);
        when(promoCodeRequestManager.findById(1)).thenReturn(Optional.of(existingCode));
        when(eventRepository.getZoneIdByEventId(1)).thenReturn(ZoneId.of("UTC"));

        controller.updatePromoCode(1, modification, principal);

        verify(accessService).checkAccessToPromoCodeEventOrganization(principal, 1, 1);
        verify(eventManager).updatePromoCode(
            eq(1),
            any(ZonedDateTime.class),
            any(ZonedDateTime.class),
            eq(100),
            eq(List.of()),
            isNull(),
            isNull(),
            isNull()
        );
    }

    @Test
    void updatePromoCode_withNonExistentPromoCode_throws() {
        PromoCodeDiscountModification modification =
            new PromoCodeDiscountModification(
                1, // organizationId
                1, // eventId
                "CODE123",
                new DateTimeModification(
                    LocalDate.of(2024, 1, 1),
                    LocalTime.of(0, 0)
                ),
                new DateTimeModification(
                    LocalDate.of(2024, 12, 31),
                    LocalTime.of(23, 59)
                ),
                BigDecimal.TEN,
                null, // currencyCode
                PromoCodeDiscount.DiscountType.PERCENTAGE,
                List.of(),
                0,
                100,
                null, // description
                null, // emailReference
                PromoCodeDiscount.CodeType.DISCOUNT,
                null // hiddenCategoryId
            );

        when(promoCodeRequestManager.findById(1)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () ->
            controller.updatePromoCode(1, modification, principal)
        );
    }

    @Test
    void updatePromoCode_withAllFields_updatesAll() {
        PromoCodeDiscountModification modification = 
        new PromoCodeDiscountModification(
            1, // organizationId
            1, // eventId
            "CODE123",
            new DateTimeModification(
                LocalDate.of(2024, 1, 1),
                LocalTime.of(0, 0)
            ),
            new DateTimeModification(
                LocalDate.of(2024, 12, 31),
                LocalTime.of(23, 59)
            ),
            BigDecimal.TEN,
            "EUR",
            PromoCodeDiscount.DiscountType.PERCENTAGE,
            List.of(1, 2),
            0,
            200,
            "Updated promo",
            "ref@example.com",
            PromoCodeDiscount.CodeType.DISCOUNT,
            5
        );

        PromoCodeDiscount existingCode = mock(PromoCodeDiscount.class);
        when(existingCode.getEventId()).thenReturn(1);
        when(promoCodeRequestManager.findById(1)).thenReturn(Optional.of(existingCode));
        when(eventRepository.getZoneIdByEventId(1)).thenReturn(ZoneId.of("UTC"));

        controller.updatePromoCode(1, modification, principal);

        verify(eventManager).updatePromoCode(
            eq(1),
            any(ZonedDateTime.class),
            any(ZonedDateTime.class),
            eq(200),
            eq(List.of(1, 2)),
            eq("Updated promo"),
            eq("ref@example.com"),
            eq(5)
        );
    }

    @Test
    void listPromoCodeInEvent_withValidEventId_returnsCodes() {
        List<PromoCodeDiscountWithFormattedTimeAndAmount> codes = new ArrayList<>();
        when(eventManager.findPromoCodesInEvent(1)).thenReturn(codes);

        List<PromoCodeDiscountWithFormattedTimeAndAmount> result = controller.listPromoCodeInEvent(1, principal);

        verify(accessService).checkEventOwnership(principal, 1);
        verify(eventManager).findPromoCodesInEvent(1);
        assertNotNull(result);
    }

    @Test
    void listPromoCodeInEvent_withEmptyResult_returnsEmpty() {
        when(eventManager.findPromoCodesInEvent(1)).thenReturn(new ArrayList<>());

        List<PromoCodeDiscountWithFormattedTimeAndAmount> result = controller.listPromoCodeInEvent(1, principal);

        verify(eventManager).findPromoCodesInEvent(1);
        assertTrue(result.isEmpty());
    }

    @Test
    void listPromoCodeInOrganization_withValidOrgId_returnsCodes() {
        List<PromoCodeDiscountWithFormattedTimeAndAmount> codes = new ArrayList<>();
        when(eventManager.findPromoCodesInOrganization(1)).thenReturn(codes);

        List<PromoCodeDiscountWithFormattedTimeAndAmount> result = controller.listPromoCodeInOrganization(1, principal);

        verify(accessService).checkOrganizationOwnership(principal, 1);
        verify(eventManager).findPromoCodesInOrganization(1);
        assertNotNull(result);
    }

    @Test
    void listPromoCodeInOrganization_withEmptyResult_returnsEmpty() {
        when(eventManager.findPromoCodesInOrganization(1)).thenReturn(new ArrayList<>());

        List<PromoCodeDiscountWithFormattedTimeAndAmount> result = controller.listPromoCodeInOrganization(1, principal);

        verify(eventManager).findPromoCodesInOrganization(1);
        assertTrue(result.isEmpty());
    }

    @Test
    void removePromoCode_withValidId_deletesCode() {

        controller.removePromoCode(1, principal);

        verify(accessService).checkAccessToPromoCode(principal, 1);
        verify(eventManager).deletePromoCode(1);
    }

    @Test
    void disablePromoCode_withValidId_disablesCode() {

        controller.disablePromoCode(1, principal);

        verify(accessService).checkAccessToPromoCode(principal, 1);
        verify(promoCodeRequestManager).disablePromoCode(1);
    }

    @Test
    void countPromoCodeUse_withValidId_returnsCount() {
        when(promoCodeRequestManager.countUsage(1)).thenReturn(42);

        int result = controller.countPromoCodeUse(1, principal);

        verify(accessService).checkAccessToPromoCode(principal, 1);
        verify(promoCodeRequestManager).countUsage(1);
        assertEquals(42, result);
    }

    @Test
    void countPromoCodeUse_withZeroUsage_returnsZero() {
        when(promoCodeRequestManager.countUsage(1)).thenReturn(0);

        int result = controller.countPromoCodeUse(1, principal);
        assertEquals(0, result);
    }

    @Test
    void retrieveDetailedUsage_withValidIdAndEventShortName_returnsUsage() {
        EventAndOrganizationId event = new EventAndOrganizationId(5, 1);
        when(eventManager.getEventAndOrganizationId("event-name", null)).thenReturn(event);
        List<PromoCodeUsageResult> usage = new ArrayList<>();
        when(promoCodeRequestManager.retrieveDetailedUsage(1, 5)).thenReturn(usage);

        List<PromoCodeUsageResult> result = controller.retrieveDetailedUsage(1, "event-name", principal);

        verify(accessService).checkAccessToPromoCode(principal, 1);
        verify(promoCodeRequestManager).retrieveDetailedUsage(1, 5);
        assertNotNull(result);
    }

    @Test
    void retrieveDetailedUsage_withoutEventShortName_returnsAllUsage() {
        List<PromoCodeUsageResult> usage = new ArrayList<>();
        when(promoCodeRequestManager.retrieveDetailedUsage(1, null)).thenReturn(usage);

        List<PromoCodeUsageResult> result = controller.retrieveDetailedUsage(1, null, principal);

        verify(accessService).checkAccessToPromoCode(principal, 1);
        verify(promoCodeRequestManager).retrieveDetailedUsage(1, null);
        assertNotNull(result);
    }

    @Test
    void retrieveDetailedUsage_withBlankEventShortName_returnsAllUsage() {
        List<PromoCodeUsageResult> usage = new ArrayList<>();
        when(promoCodeRequestManager.retrieveDetailedUsage(1, null)).thenReturn(usage);

        List<PromoCodeUsageResult> result = controller.retrieveDetailedUsage(1, "   ", principal);

        verify(accessService).checkAccessToPromoCode(principal, 1);
        verify(promoCodeRequestManager).retrieveDetailedUsage(1, null);
        assertNotNull(result);
    }

    @Test
    void retrieveDetailedUsage_withEmptyResult_returnsEmpty() {
        when(promoCodeRequestManager.retrieveDetailedUsage(1, null)).thenReturn(new ArrayList<>());

        List<PromoCodeUsageResult> result = controller.retrieveDetailedUsage(1, null, principal);

        assertTrue(result.isEmpty());
    }

    @Test
    void zoneIdFromEventId_withEventId_returnsEventZone() {
        when(eventRepository.getZoneIdByEventId(1)).thenReturn(ZoneId.of("Europe/Rome"));

        PromoCodeDiscountModification modification =
            new PromoCodeDiscountModification(
            null,
            1,
            "CODE123",
            new DateTimeModification(
                LocalDate.of(2024, 1, 1),
                LocalTime.of(0, 0)
            ),
            new DateTimeModification(
                LocalDate.of(2024, 12, 31),
                LocalTime.of(23, 59)
            ),
            BigDecimal.TEN,
            "EUR",
            PromoCodeDiscount.DiscountType.PERCENTAGE,
            List.of(),
            0,
            100,
            null,
            null,
            PromoCodeDiscount.CodeType.DISCOUNT,
            null
        );

        controller.addPromoCode(modification, principal);

        verify(eventRepository, atLeastOnce()).getZoneIdByEventId(1);
        verify(eventManager).addPromoCode(
            any(),
            eq(1),
            isNull(),
            any(),
            any(),
            anyInt(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void zoneIdFromEventId_withUtcOffset_usesOffset() {
        PromoCodeDiscountModification modification =
            new PromoCodeDiscountModification(
                1, // organizationId
                null, // eventId
                "CODE",
                new DateTimeModification(
                    LocalDate.of(2024, 1, 1),
                    LocalTime.of(0, 0)
                ),
                new DateTimeModification(
                    LocalDate.of(2024, 12, 31),
                    LocalTime.of(23, 59)
                ),
                BigDecimal.TEN,
                null, // currencyCode
                PromoCodeDiscount.DiscountType.PERCENTAGE,
                List.of(),
                7200,
                null, // maxUsage
                null, // description
                null, // emailReference
                PromoCodeDiscount.CodeType.DISCOUNT,
                null // hiddenCategoryId
            );

        controller.addPromoCode(modification, principal);

        verify(eventManager).addPromoCode(
            eq("CODE"),
            isNull(),
            eq(1),
            any(),
            any(),
            anyInt(),
            any(),
            anyList(),
            isNull(),
            isNull(),
            isNull(),
            eq(PromoCodeDiscount.CodeType.DISCOUNT),
            isNull(),
            isNull()
        );
    }

    @Test
    void zoneIdFromEventId_withNullUtcOffset_usesZeroOffset() {
        PromoCodeDiscountModification modification =
            new PromoCodeDiscountModification(
                1, // organizationId
                null, // eventId
                "CODE",
                new DateTimeModification(
                    LocalDate.of(2024, 1, 1),
                    LocalTime.of(0, 0)
                ),
                new DateTimeModification(
                    LocalDate.of(2024, 12, 31),
                    LocalTime.of(23, 59)
                ),
                BigDecimal.TEN,
                null, // currencyCode
                PromoCodeDiscount.DiscountType.PERCENTAGE,
                List.of(),
                null, // utcOffset
                null, // maxUsage
                null, // description
                null, // emailReference
                PromoCodeDiscount.CodeType.DISCOUNT,
                null // hiddenCategoryId
            );

        controller.addPromoCode(modification, principal);

        verify(eventManager).addPromoCode(any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
