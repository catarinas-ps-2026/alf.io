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

import alfio.util.ObjectDiffUtil;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.util.HashMap;
import java.util.Map;

public class ObjectDiffUtilFuzzTest {

    @FuzzTest
    public void fuzzDiffMaps(FuzzedDataProvider data) {
        try {
            int beforeSize = data.consumeInt(0, 20);
            int afterSize = data.consumeInt(0, 20);

            Map<String, Object> before = new HashMap<>();
            for (int i = 0; i < beforeSize; i++) {
                String key = data.consumeString(20);
                String value = data.consumeString(50);
                before.put(key, value);
            }

            Map<String, Object> after = new HashMap<>();
            for (int i = 0; i < afterSize; i++) {
                String key = data.consumeString(20);
                String value = data.consumeString(50);
                after.put(key, value);
            }

            ObjectDiffUtil.diff(before, after);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
