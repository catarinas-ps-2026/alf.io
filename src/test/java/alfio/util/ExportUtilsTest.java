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

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ExportUtilsTest {

    @Test
    void exportCsvWritesHeadersBomNoIndexHeaderAndEscapesFormulaLikeValues() throws Exception {
        var response = new MockHttpServletResponse();

        ExportUtils.exportCsv("report.csv", new String[] {"name", "value"}, Stream.of(
            new String[] {"Alice", "=2+2"},
            new String[] {"Bob", " safe "}
        ), response);

        var bytes = response.getContentAsByteArray();
        assertArrayEquals(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, new byte[] {bytes[0], bytes[1], bytes[2]});
        assertEquals("text/csv;charset=UTF-8", response.getContentType());
        assertEquals("attachment; filename=report.csv", response.getHeader("Content-Disposition"));
        assertEquals("noindex", response.getHeader(ExportUtils.X_ROBOTS_TAG));

        var csv = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(csv.contains("name,value"));
        assertTrue(csv.contains("Alice,\"	=2+2\""));
        assertTrue(csv.contains("Bob,safe"));
    }

    @Test
    void markAsNoIndexSetsRobotsHeader() {
        var response = new MockHttpServletResponse();

        ExportUtils.markAsNoIndex(response);

        assertEquals("noindex", response.getHeader(ExportUtils.X_ROBOTS_TAG));
    }
}
