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

import alfio.util.HoursRange;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.time.LocalTime;

public class HoursRangeFuzzTest {

    @FuzzTest
    public void fuzzHoursRange(FuzzedDataProvider data) {
        try {
            int startHour = data.consumeInt(0, 23);
            int startMinute = data.consumeInt(0, 59);
            int startSecond = data.consumeInt(0, 59);
            int endHour = data.consumeInt(0, 23);
            int endMinute = data.consumeInt(0, 59);
            int endSecond = data.consumeInt(0, 59);

            LocalTime start = LocalTime.of(startHour, startMinute, startSecond);
            LocalTime end = LocalTime.of(endHour, endMinute, endSecond);
            HoursRange range = new HoursRange(start, end);

            int testHour = data.consumeInt(0, 23);
            int testMinute = data.consumeInt(0, 59);
            int testSecond = data.consumeInt(0, 59);
            LocalTime testTime = LocalTime.of(testHour, testMinute, testSecond);

            range.includes(testTime);
            range.getDistanceInHours(testTime);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
