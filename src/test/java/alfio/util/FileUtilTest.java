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

import alfio.model.BillingDocument;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileUtilTest {

    @Test
    void getBillingDocumentFileNameUsesInvoiceDateNumberAndIdForInvoices() {
        var document = new BillingDocument(99L, 1, "reservation", "INV-7", BillingDocument.Type.INVOICE,
            "{\"confirmationDate\":\"2024-04-10T12:34:56Z\"}", ZonedDateTime.parse("2024-04-10T12:35:00Z"),
            BillingDocument.Status.VALID, null);

        assertEquals("event-2024-04-10-123456-INV-7-99.pdf", FileUtil.getBillingDocumentFileName("event", "reservation", document));
    }

    @Test
    void getBillingDocumentFileNameUsesReceiptPrefixForReceipts() {
        var document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.RECEIPT);

        assertEquals("receipt-event-reservation.pdf", FileUtil.getBillingDocumentFileName("event", "reservation", document));
    }

    @Test
    void sendPdfWritesBytesAndHeaders() {
        var response = new MockHttpServletResponse();
        var document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.RECEIPT);

        assertTrue(FileUtil.sendPdf(new byte[] {1, 2, 3}, response, "event", "reservation", document));

        assertArrayEquals(new byte[] {1, 2, 3}, response.getContentAsByteArray());
        assertEquals("application/pdf", response.getContentType());
        assertEquals("attachment; filename=\"receipt-event-reservation.pdf\"", response.getHeader("Content-Disposition"));
        assertEquals("noindex", response.getHeader(ExportUtils.X_ROBOTS_TAG));
    }

    @Test
    void sendPdfReturnsFalseWhenContentIsNull() {
        assertFalse(FileUtil.sendPdf(null, new MockHttpServletResponse(), "event", "reservation", mock(BillingDocument.class)));
    }

    @Test
    void sendPdfReturnsFalseWhenOutputStreamFails() throws IOException {
        var response = mock(HttpServletResponse.class);
        var document = mock(BillingDocument.class);
        when(document.getType()).thenReturn(BillingDocument.Type.RECEIPT);
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("write failed");
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }
        });

        assertFalse(FileUtil.sendPdf(new byte[] {1}, response, "event", "reservation", document));
    }
}
