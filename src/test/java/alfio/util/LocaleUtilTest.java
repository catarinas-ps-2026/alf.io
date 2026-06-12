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
package alfio.util;

import alfio.model.ContentLanguage;
import alfio.model.LocalizedContent;
import alfio.model.Ticket;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocaleUtilTest {

    @Test
    void getTicketLanguageUsesTicketLanguageWhenPresent() {
        var ticket = mock(Ticket.class);
        when(ticket.getUserLanguage()).thenReturn("it");

        assertEquals(Locale.ITALIAN, LocaleUtil.getTicketLanguage(ticket, Locale.ENGLISH));
    }

    @Test
    void getTicketLanguageFallsBackWhenTicketLanguageIsBlank() {
        var ticket = mock(Ticket.class);
        when(ticket.getUserLanguage()).thenReturn(" ");

        assertEquals(Locale.FRENCH, LocaleUtil.getTicketLanguage(ticket, Locale.FRENCH));
    }

    @Test
    void forLanguageTagReturnsEnglishForNullInput() {
        assertEquals(Locale.ENGLISH, LocaleUtil.forLanguageTag(null));
    }

    @Test
    void forLanguageTagUsesRequestedLocalizedContentLanguageWhenAvailable() {
        LocalizedContent localizedContent = () -> List.of(ContentLanguage.ENGLISH, ContentLanguage.SPANISH);

        assertEquals(Locale.forLanguageTag("es"), LocaleUtil.forLanguageTag(" ES ", localizedContent));
    }

    @Test
    void forLanguageTagFallsBackToFirstLocalizedContentLanguage() {
        LocalizedContent localizedContent = () -> List.of(ContentLanguage.ITALIAN, ContentLanguage.SPANISH);

        assertEquals(Locale.ITALIAN, LocaleUtil.forLanguageTag("de", localizedContent));
    }

    @Test
    void forLanguageTagFallsBackToEnglishWhenLocalizedContentHasNoLanguages() {
        LocalizedContent localizedContent = List::of;

        assertEquals(Locale.ENGLISH, LocaleUtil.forLanguageTag("de", localizedContent));
    }

    @Test
    void atZoneConvertsZonedDateTimeAndKeepsNullAsNull() {
        var input = ZonedDateTime.parse("2024-01-01T12:00:00Z");

        assertEquals(ZonedDateTime.parse("2024-01-01T13:00:00+01:00[Europe/Rome]"), LocaleUtil.atZone(input, ZoneId.of("Europe/Rome")));
        assertNull(LocaleUtil.atZone((ZonedDateTime) null, ZoneId.of("Europe/Rome")));
    }

    @Test
    void atZoneAppliesZoneToLocalDateTimeAndKeepsNullAsNull() {
        var input = LocalDateTime.parse("2024-01-01T12:00:00");

        assertEquals(ZonedDateTime.parse("2024-01-01T12:00:00+01:00[Europe/Rome]"), LocaleUtil.atZone(input, ZoneId.of("Europe/Rome")));
        assertNull(LocaleUtil.atZone((LocalDateTime) null, ZoneId.of("Europe/Rome")));
    }

    @Test
    void formatDateReturnsLanguageKeyedFormattedDates() {
        var date = ZonedDateTime.parse("2024-04-10T12:34:56Z");

        assertEquals(Map.of("en", "2024-04-10", "it", "10/04/2024"),
            LocaleUtil.formatDate(date, Map.of(Locale.ENGLISH, "yyyy-MM-dd", Locale.ITALIAN, "dd/MM/yyyy")));
    }

    @Test
    void formatDateReturnsNullForNullDate() {
        assertNull(LocaleUtil.formatDate(null, Map.of(Locale.ENGLISH, "yyyy-MM-dd")));
    }
}
