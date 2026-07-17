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

import alfio.extension.ExtensionUtils;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.util.HashMap;
import java.util.Map;

public class ExtensionUtilsConvertToJsonFuzzTest {

    @FuzzTest
    public void fuzzConvertToJson(FuzzedDataProvider data) {
        try {
            // Test convertToJson with various Java objects
            int objectType = data.consumeInt(0, 4);
            switch (objectType) {
                case 0 -> {
                    String value = data.consumeString(200);
                    ExtensionUtils.convertToJson(value);
                }
                case 1 -> {
                    Integer value = data.consumeInt();
                    ExtensionUtils.convertToJson(value);
                }
                case 2 -> {
                    Double value = data.consumeDouble();
                    ExtensionUtils.convertToJson(value);
                }
                case 3 -> {
                    Map<String, Object> map = new HashMap<>();
                    int entries = data.consumeInt(0, 10);
                    for (int i = 0; i < entries; i++) {
                        map.put(data.consumeString(20), data.consumeString(50));
                    }
                    ExtensionUtils.convertToJson(map);
                }
                case 4 -> {
                    java.util.List<Object> list = new java.util.ArrayList<>();
                    int entries = data.consumeInt(0, 10);
                    for (int i = 0; i < entries; i++) {
                        list.add(data.consumeString(50));
                    }
                    ExtensionUtils.convertToJson(list);
                }
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    // unwrap is package-private, so we test convertToJson which exercises the unwrap path indirectly
}
