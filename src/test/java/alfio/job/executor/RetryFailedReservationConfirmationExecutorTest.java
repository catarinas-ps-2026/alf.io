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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import alfio.manager.ReservationFinalizer;
import alfio.manager.support.RetryFinalizeReservation;
import alfio.manager.system.AdminJobExecutor;
import alfio.model.system.AdminJobSchedule;
import alfio.util.Json;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RetryFailedReservationConfirmationExecutorTest {

    @Test
    void processDeserializesPayloadAndRetriesReservationFinalization() {
        var reservationFinalizer = mock(ReservationFinalizer.class);
        var json = mock(Json.class);
        var retryFinalizeReservation = mock(RetryFinalizeReservation.class);
        when(json.fromJsonString("{\"reservationId\":\"abc\"}", RetryFinalizeReservation.class))
                .thenReturn(retryFinalizeReservation);
        var executor = new RetryFailedReservationConfirmationExecutor(reservationFinalizer, json);

        assertNull(executor.process(new AdminJobSchedule(
                1L,
                AdminJobExecutor.JobName.RETRY_RESERVATION_CONFIRMATION.name(),
                null,
                AdminJobSchedule.Status.SCHEDULED,
                null,
                Map.of("payload", "{\"reservationId\":\"abc\"}"),
                0)));

        verify(json).fromJsonString("{\"reservationId\":\"abc\"}", RetryFinalizeReservation.class);
        verify(reservationFinalizer).retryFinalizeReservation(retryFinalizeReservation);
        verifyNoMoreInteractions(json, reservationFinalizer);
    }
}
