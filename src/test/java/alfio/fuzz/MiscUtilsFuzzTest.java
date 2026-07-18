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

import alfio.util.MiscUtils;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.util.ArrayList;
import java.util.List;

public class MiscUtilsFuzzTest {

    @FuzzTest
    public void fuzzRemoveTabsAndNewlines(FuzzedDataProvider data) {
        try {
            String input = data.consumeString(500);
            MiscUtils.removeTabsAndNewlines(input);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzGetAtIndexOrNull(FuzzedDataProvider data) {
        try {
            int size = data.consumeInt(0, 50);
            List<String> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(data.consumeString(20));
            }
            int index = data.consumeInt(-100, 100);
            MiscUtils.getAtIndexOrNull(list, index);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzRemoveTabsAndNewlinesEdgeCases(FuzzedDataProvider data) {
        try {
            String input = data.consumeString(2000);
            String result = MiscUtils.removeTabsAndNewlines(input);
            // result should not contain \n or \r
            if (result != null && (result.contains("\n") || result.contains("\r"))) {
                throw new AssertionError("Result still contains newlines");
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzGetAtIndexOrNullWithDifferentTypes(FuzzedDataProvider data) {
        try {
            int size = data.consumeInt(0, 20);
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                list.add(data.consumeInt());
            }
            int index = data.consumeInt(-100, 100);
            MiscUtils.getAtIndexOrNull(list, index);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
