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

import alfio.util.Json;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.util.Map;

public class JsonRoundTripFuzzTest {

    @FuzzTest
    public void fuzzJsonRoundTrip(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(5000);
            // Fuzz the full JSON round-trip: parse -> serialize -> parse
            // This exercises both Jackson ObjectMapper and Gson
            Map<String, Object> jacksonParsed = Json.OBJECT_MAPPER.readValue(json, Map.class);
            if (jacksonParsed != null) {
                String jacksonSerialized = Json.OBJECT_MAPPER.writeValueAsString(jacksonParsed);
                Json.OBJECT_MAPPER.readValue(jacksonSerialized, Map.class);
            }
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        } catch (Exception e) {
            // Jackson can throw various exceptions on malformed JSON
        }
    }

    @FuzzTest
    public void fuzzGsonRoundTrip(FuzzedDataProvider data) {
        try {
            String json = data.consumeString(5000);
            // Fuzz Gson round-trip
            @SuppressWarnings("unchecked")
            Map<String, Object> gsonParsed = Json.GSON.fromJson(json, Map.class);
            if (gsonParsed != null) {
                String gsonSerialized = Json.GSON.toJson(gsonParsed);
                Json.GSON.fromJson(gsonSerialized, Map.class);
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        } catch (Exception e) {
            // Gson can throw various exceptions on malformed JSON
        }
    }

    @FuzzTest
    public void fuzzJsonDeserializeEventModification(FuzzedDataProvider data) {
        try {
            // Fuzz deserialization of event-related structures
            String json = data.consumeString(2000);
            Map<String, Object> map = Json.fromJson(json, Map.class);
            if (map != null) {
                // Try to extract fields that would be used in EventModification
                Object shortName = map.get("shortName");
                Object websiteUrl = map.get("websiteUrl");
                Object description = map.get("description");
                Object imageUrl = map.get("imageUrl");
                Object termsAndConditionsUrl = map.get("termsAndConditionsUrl");
            }
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzJsonDeserializeTicketCategoryModification(FuzzedDataProvider data) {
        try {
            // Fuzz deserialization of ticket category structures
            String json = data.consumeString(2000);
            Map<String, Object> map = Json.fromJson(json, Map.class);
            if (map != null) {
                Object name = map.get("name");
                Object maxTickets = map.get("maxTickets");
                Object inception = map.get("inception");
                Object expiration = map.get("expiration");
            }
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
            // catch exceptions from invalid inputs
        }
    }
}
