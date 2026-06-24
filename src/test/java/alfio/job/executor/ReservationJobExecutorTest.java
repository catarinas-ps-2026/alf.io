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
package alfio.job.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import alfio.manager.TicketReservationManager;
import alfio.manager.system.AdminJobExecutor;
import alfio.model.system.AdminJobSchedule;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservationJobExecutorTest {

    private TicketReservationManager ticketReservationManager;
    private ReservationJobExecutor executor;

    @BeforeEach
    void setUp() {
        ticketReservationManager = mock(TicketReservationManager.class);
        executor = new ReservationJobExecutor(ticketReservationManager);
    }

    @Test
    void checkOfflinePaymentsDelegatesToReservationManager() {
        assertEquals("OK", executor.process(schedule(AdminJobExecutor.JobName.CHECK_OFFLINE_PAYMENTS)));

        verify(ticketReservationManager).checkOfflinePaymentsStatus();
        verifyNoMoreInteractions(ticketReservationManager);
    }

    @Test
    void ticketAssignmentReminderSendsBothAssignmentAndOptionalDataReminders() {
        assertEquals("OK", executor.process(schedule(AdminJobExecutor.JobName.SEND_TICKET_ASSIGNMENT_REMINDER)));

        verify(ticketReservationManager).sendReminderForTicketAssignment();
        verify(ticketReservationManager).sendReminderForOptionalData();
        verifyNoMoreInteractions(ticketReservationManager);
    }

    @Test
    void offlinePaymentReminderDelegatesToReservationManager() {
        assertEquals("OK", executor.process(schedule(AdminJobExecutor.JobName.SEND_OFFLINE_PAYMENT_REMINDER)));

        verify(ticketReservationManager).sendReminderForOfflinePayments();
        verifyNoMoreInteractions(ticketReservationManager);
    }

    @Test
    void offlinePaymentOrganizerReminderDelegatesToReservationManager() {
        assertEquals("OK", executor.process(schedule(AdminJobExecutor.JobName.SEND_OFFLINE_PAYMENT_TO_ORGANIZER)));

        verify(ticketReservationManager).sendReminderForOfflinePaymentsToEventManagers();
        verifyNoMoreInteractions(ticketReservationManager);
    }

    @Test
    void unknownJobReturnsNullAndDoesNotCallReservationManager() {
        assertNull(executor.process(schedule(AdminJobExecutor.JobName.UNKNOWN)));

        verifyNoInteractions(ticketReservationManager);
    }

    private static AdminJobSchedule schedule(AdminJobExecutor.JobName jobName) {
        return new AdminJobSchedule(1L, jobName.name(), null, AdminJobSchedule.Status.SCHEDULED, null, Map.of(), 0);
    }
}
