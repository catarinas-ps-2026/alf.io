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
