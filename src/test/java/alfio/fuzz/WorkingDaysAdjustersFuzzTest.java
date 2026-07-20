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

import alfio.util.WorkingDaysAdjusters;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.time.LocalDateTime;

public class WorkingDaysAdjustersFuzzTest {

    @FuzzTest
    public void fuzzAddDays(FuzzedDataProvider data) {
        try {
            int year = data.consumeInt(1970, 2100);
            int month = data.consumeInt(1, 12);
            int day = data.consumeInt(1, 28);
            int hour = data.consumeInt(0, 23);
            int minute = data.consumeInt(0, 59);
            int second = data.consumeInt(0, 59);
            int days = data.consumeInt(0, 365);

            LocalDateTime start = LocalDateTime.of(year, month, day, hour, minute, second);
            WorkingDaysAdjusters.addDays(start, days);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
