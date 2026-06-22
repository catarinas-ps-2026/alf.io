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
package alfio.model.api.v1.admin;

import alfio.model.modification.AdminReservationModification.Notification;
import alfio.model.modification.AdminReservationModification.TransactionDetails;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ReservationConfirmationRequest(
        @JsonProperty("transaction") TransactionDetails transaction,
        @JsonProperty("notification") Notification notification,
        @JsonProperty("billingData") ReservationBillingData reservationBillingData) {
    @Override
    public Notification notification() {
        return Notification.orEmpty(notification);
    }

    public boolean isValid() {
        return transaction != null && transaction.getPaymentProvider() != null;
    }
}
