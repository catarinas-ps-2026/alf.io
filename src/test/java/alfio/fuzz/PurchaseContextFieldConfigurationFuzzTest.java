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

import alfio.model.PurchaseContextFieldConfiguration;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

public class PurchaseContextFieldConfigurationFuzzTest {

    @FuzzTest
    public void fuzzPurchaseContextFieldConfiguration(FuzzedDataProvider data) {
        try {
            // Fuzz the constructor which deserializes JSON from multiple columns
            String name = data.consumeString(50);
            int order = data.consumeInt();
            String type = data.consumeString(50);
            Integer maxLength = data.consumeBoolean() ? null : data.consumeInt(0, 1000);
            Integer minLength = data.consumeBoolean() ? null : data.consumeInt(0, 1000);
            boolean required = data.consumeBoolean();
            boolean editable = data.consumeBoolean();
            String restrictedValuesJson = data.consumeString(500);
            String disabledValuesJson = data.consumeString(500);
            String ticketCategoryIdsJson = data.consumeString(500);
            boolean displayAtCheckIn = data.consumeBoolean();

            // This tests the JSON deserialization of restrictedValues, disabledValues, and ticketCategoryIds
            new PurchaseContextFieldConfiguration(
                    1, // id
                    null, // eventId
                    null, // subscriptionDescriptorId
                    name,
                    order,
                    type,
                    maxLength,
                    minLength,
                    required,
                    editable,
                    restrictedValuesJson,
                    PurchaseContextFieldConfiguration.Context.ATTENDEE,
                    null, // additionalServiceId
                    ticketCategoryIdsJson,
                    disabledValuesJson,
                    displayAtCheckIn);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzRestrictedValuesDeserialization(FuzzedDataProvider data) {
        try {
            String restrictedValuesJson = data.consumeString(500);
            // Directly test the JSON deserialization path used in PurchaseContextFieldConfiguration
            // using Gson (same as the production code)
            alfio.util.Json.GSON.fromJson(restrictedValuesJson, new com.google.gson.reflect.TypeToken<java.util.List<String>>() {}.getType());
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzCategoryIdsDeserialization(FuzzedDataProvider data) {
        try {
            String categoryIdsJson = data.consumeString(500);
            // Directly test the JSON deserialization path used in PurchaseContextFieldConfiguration
            // using Gson (same as the production code)
            alfio.util.Json.GSON.fromJson(categoryIdsJson, new com.google.gson.reflect.TypeToken<java.util.List<Integer>>() {}.getType());
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzDisabledValuesDeserialization(FuzzedDataProvider data) {
        try {
            String disabledValuesJson = data.consumeString(500);
            // Directly test the JSON deserialization path
            // using Gson (same as the production code)
            alfio.util.Json.GSON.fromJson(disabledValuesJson, new com.google.gson.reflect.TypeToken<java.util.List<String>>() {}.getType());
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
