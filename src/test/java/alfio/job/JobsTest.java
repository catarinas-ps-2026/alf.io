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
package alfio.job;

import alfio.manager.*;
import alfio.manager.system.AdminJobExecutor;
import alfio.manager.system.AdminJobManager;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobsTest {

    private AdminReservationRequestManager adminReservationRequestManager;
    private FileUploadManager fileUploadManager;
    private NotificationManager notificationManager;
    private SpecialPriceTokenGenerator specialPriceTokenGenerator;
    private TicketReservationManager ticketReservationManager;
    private WaitingQueueSubscriptionProcessor waitingQueueSubscriptionProcessor;
    private AdminJobManager adminJobManager;
    private Jobs jobs;

    @BeforeEach
    void setUp() {
        adminReservationRequestManager = mock(AdminReservationRequestManager.class);
        fileUploadManager = mock(FileUploadManager.class);
        notificationManager = mock(NotificationManager.class);
        specialPriceTokenGenerator = mock(SpecialPriceTokenGenerator.class);
        ticketReservationManager = mock(TicketReservationManager.class);
        waitingQueueSubscriptionProcessor = mock(WaitingQueueSubscriptionProcessor.class);
        adminJobManager = mock(AdminJobManager.class);
        jobs = new Jobs(adminReservationRequestManager, fileUploadManager, notificationManager, specialPriceTokenGenerator,
            ticketReservationManager, waitingQueueSubscriptionProcessor, adminJobManager);
    }

    @Test
    void cleanupUnreferencedBlobFilesUsesDateInThePast() {
        var captor = ArgumentCaptor.forClass(Date.class);

        jobs.cleanupUnreferencedBlobFiles();

        verify(fileUploadManager).cleanupUnreferencedBlobFiles(captor.capture());
        assertTrue(captor.getValue().before(new Date()));
    }

    @Test
    void generateSpecialPriceCodesDelegatesToGenerator() {
        jobs.generateSpecialPriceCodes();

        verify(specialPriceTokenGenerator).generatePendingCodes();
    }

    @Test
    void sendOfflinePaymentReminderToEventOrganizersSchedulesOrganizerReminderJob() {
        jobs.sendOfflinePaymentReminderToEventOrganizers();

        verify(adminJobManager).scheduleExecution(AdminJobExecutor.JobName.SEND_OFFLINE_PAYMENT_TO_ORGANIZER, Map.of());
    }

    @Test
    void assignTicketsToSubscribersSchedulesAssignTicketsJob() {
        jobs.assignTicketsToSubscribers();

        verify(adminJobManager).scheduleExecution(AdminJobExecutor.JobName.ASSIGN_TICKETS_TO_SUBSCRIBERS, Map.of());
    }

    @Test
    void sendEmailsDelegatesToNotificationManager() {
        jobs.sendEmails();

        verify(notificationManager).sendWaitingMessages();
    }

    @Test
    void processReservationRequestsDelegatesToAdminReservationRequestManager() {
        when(adminReservationRequestManager.processPendingReservations()).thenReturn(Pair.of(1, 0));

        jobs.processReservationRequests();

        verify(adminReservationRequestManager).processPendingReservations();
    }

    @Test
    void sendOfflinePaymentReminderSchedulesOfflinePaymentReminderJob() {
        jobs.sendOfflinePaymentReminder();

        verify(adminJobManager).scheduleExecution(AdminJobExecutor.JobName.SEND_OFFLINE_PAYMENT_REMINDER, Map.of());
    }

    @Test
    void sendTicketAssignmentReminderSchedulesTicketAssignmentReminderJob() {
        jobs.sendTicketAssignmentReminder();

        verify(adminJobManager).scheduleExecution(AdminJobExecutor.JobName.SEND_TICKET_ASSIGNMENT_REMINDER, Map.of());
    }

    @Test
    void cleanupExpiredPendingReservationCallsAllExpirationCleanupsWithSameDate() {
        var expirationCaptor = ArgumentCaptor.forClass(Date.class);

        jobs.cleanupExpiredPendingReservation();

        verify(ticketReservationManager).cleanupExpiredReservations(expirationCaptor.capture());
        verify(ticketReservationManager).cleanupExpiredOfflineReservations(expirationCaptor.capture());
        verify(ticketReservationManager).markExpiredInPaymentReservationAsStuck(expirationCaptor.capture());
        assertEquals(1, expirationCaptor.getAllValues().stream().distinct().count());
    }

    @Test
    void processReleasedTicketsDelegatesToWaitingQueueProcessor() {
        jobs.processReleasedTickets();

        verify(waitingQueueSubscriptionProcessor).handleWaitingTickets();
    }

    @Test
    void checkOfflinePaymentsStatusSchedulesCheckOfflinePaymentsJob() {
        jobs.checkOfflinePaymentsStatus();

        verify(adminJobManager).scheduleExecution(AdminJobExecutor.JobName.CHECK_OFFLINE_PAYMENTS, Map.of());
    }
}
