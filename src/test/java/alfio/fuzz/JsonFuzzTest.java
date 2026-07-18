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

import alfio.model.BillingDocument;
import alfio.model.CustomerName;
import alfio.model.Event;
import alfio.model.PromoCodeDiscount;
import alfio.model.PurchaseContextFieldConfiguration;
import alfio.model.Ticket;
import alfio.model.TicketCategory;
import alfio.model.TicketReservation;
import alfio.model.TicketReservationInvoicingAdditionalInfo;
import alfio.util.Json;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;

public class JsonFuzzTest {

    @FuzzTest
    public void fuzzJsonDeserializeMap(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(500);
            Json.fromJson(json, Map.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzJsonDeserializeString(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(500);
            Json.fromJson(json, String.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzJsonDeserializeTypeReference(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(500);
            Json.fromJson(json, new TypeReference<Map<String, Object>>() {});
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzJsonDeserializeList(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(500);
            Json.fromJson(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzJsonSerializeDeserialize(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(500);
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
    public void fuzzDeserializeEvent(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Json.fromJson(json, Event.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzDeserializeTicket(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Json.fromJson(json, Ticket.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzDeserializeTicketReservation(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Json.fromJson(json, TicketReservation.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzDeserializeTicketCategory(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Json.fromJson(json, TicketCategory.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzDeserializeBillingDocument(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Json.fromJson(json, BillingDocument.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzDeserializePromoCodeDiscount(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Json.fromJson(json, PromoCodeDiscount.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzDeserializeTicketReservationInvoicingAdditionalInfo(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Json.fromJson(json, TicketReservationInvoicingAdditionalInfo.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzDeserializeCustomerName(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Json.fromJson(json, CustomerName.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzDeserializePurchaseContextFieldConfiguration(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Json.fromJson(json, PurchaseContextFieldConfiguration.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzRoundTripEvent(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Event event = Json.fromJson(json, Event.class);
            if (event != null) {
                String serialized = Json.toJson(event);
                Json.fromJson(serialized, Event.class);
            }
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzRoundTripTicket(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Ticket ticket = Json.fromJson(json, Ticket.class);
            if (ticket != null) {
                String serialized = Json.toJson(ticket);
                Json.fromJson(serialized, Ticket.class);
            }
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzJsonFromStringDeserialization(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(2000);
            Json.fromJson(json, Map.class);
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }
}
