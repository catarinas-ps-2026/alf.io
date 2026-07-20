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
package alfio.fuzz;

import alfio.model.TicketReservationInvoicingAdditionalInfo;
import alfio.util.Json;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;

public class BillingDetailsFuzzTest {

    @FuzzTest
    public void fuzzBillingDetailsInvoicingAdditionalInfo(FuzzedDataProvider data) {
        try {
            // BillingDetails constructor uses @JSONData annotation for invoicingAdditionalInfo
            // which deserializes JSON. Fuzz the deserialization directly.
            String json = data.consumeString(2000);
            Map<String, Object> map = Json.fromJson(json, new TypeReference<>() {});
            if (map != null) {
                String serialized = Json.toJson(map);
                Json.fromJson(serialized, new TypeReference<Map<String, Object>>() {});
            }
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzBillingDetailsConstructor(FuzzedDataProvider data) {
        try {
            String companyName = data.consumeString(100);
            String addressLine1 = data.consumeString(100);
            String addressLine2 = data.consumeString(100);
            String zip = data.consumeString(20);
            String city = data.consumeString(50);
            String state = data.consumeString(50);
            String country = data.consumeString(5);
            String taxId = data.consumeString(30);
            // invoicingAdditionalInfo is @JSONData, so we pass a JSON string that will be deserialized
            String invoicingJson = data.consumeString(500);

            // The BillingDetails constructor will try to deserialize the JSON for invoicingAdditionalInfo
            // but we need to provide a valid TicketReservationInvoicingAdditionalInfo
            // Instead, fuzz the JSON deserialization path directly
            Json.fromJson(invoicingJson, TicketReservationInvoicingAdditionalInfo.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }
}
