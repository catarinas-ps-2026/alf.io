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

package alfio.repository;

import alfio.model.BillingDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceSequencesRepositoryTest {

    private final InvoiceSequencesRepository invoiceSequencesRepository = mock(InvoiceSequencesRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testLockSequenceForUpdateDefault() {
        when(invoiceSequencesRepository.lockSequenceForUpdate(123, BillingDocument.Type.INVOICE)).thenReturn(10);
        int result = invoiceSequencesRepository.lockSequenceForUpdate(123);
        assertEquals(10, result);
        verify(invoiceSequencesRepository).lockSequenceForUpdate(123, BillingDocument.Type.INVOICE);
    }

    @Test
    void testIncrementSequenceForDefault() {
        when(invoiceSequencesRepository.incrementSequenceFor(123, BillingDocument.Type.INVOICE)).thenReturn(1);
        int result = invoiceSequencesRepository.incrementSequenceFor(123);
        assertEquals(1, result);
        verify(invoiceSequencesRepository).incrementSequenceFor(123, BillingDocument.Type.INVOICE);
    }
}
