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
package alfio.util;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class HoursRangeTest {

    @Test
    void includesOnlyTimesStrictlyBetweenStartAndEnd() {
        var range = new HoursRange(LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertFalse(range.includes(LocalTime.of(9, 0)));
        assertTrue(range.includes(LocalTime.of(9, 1)));
        assertTrue(range.includes(LocalTime.of(16, 59)));
        assertFalse(range.includes(LocalTime.of(17, 0)));
    }

    @Test
    void distanceIsZeroForIncludedTimes() {
        var range = new HoursRange(LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertEquals(0, range.getDistanceInHours(LocalTime.of(12, 0)));
    }

    @Test
    void distanceReturnsHoursUntilStartWhenBeforeRange() {
        var range = new HoursRange(LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertEquals(2, range.getDistanceInHours(LocalTime.of(7, 0)));
    }

    @Test
    void distanceWrapsToNextDayWhenAfterRange() {
        var range = new HoursRange(LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertEquals(14, range.getDistanceInHours(LocalTime.of(19, 0)));
    }
}
