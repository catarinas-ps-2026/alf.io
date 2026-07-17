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

import alfio.util.MonetaryUtil;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.math.BigDecimal;

public class MonetaryUtilFuzzTest {

    @FuzzTest
    public void fuzzFormatCents(FuzzedDataProvider data) {
        try {
            long cents = data.consumeLong();
            String currencyCode = data.consumeString(10);
            MonetaryUtil.formatCents(cents, currencyCode);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzCentsToUnit(FuzzedDataProvider data) {
        try {
            long cents = data.consumeLong();
            String currencyCode = data.consumeString(10);
            MonetaryUtil.centsToUnit(cents, currencyCode);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzUnitToCents(FuzzedDataProvider data) {
        try {
            double unitValue = data.consumeDouble();
            String currencyCode = data.consumeString(10);
            MonetaryUtil.unitToCents(BigDecimal.valueOf(unitValue), currencyCode);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzFormatPercentage(FuzzedDataProvider data) {
        try {
            int percentageCts = data.consumeInt();
            MonetaryUtil.formatPercentage(percentageCts);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzAddVAT(FuzzedDataProvider data) {
        try {
            int priceInCents = data.consumeInt();
            double vatDouble = data.consumeDouble();
            MonetaryUtil.addVAT(priceInCents, BigDecimal.valueOf(vatDouble));
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzExtractVAT(FuzzedDataProvider data) {
        try {
            double priceDouble = data.consumeDouble();
            double vatDouble = data.consumeDouble();
            MonetaryUtil.extractVAT(BigDecimal.valueOf(priceDouble), BigDecimal.valueOf(vatDouble));
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzCalcVat(FuzzedDataProvider data) {
        try {
            double priceDouble = data.consumeDouble();
            double percentageDouble = data.consumeDouble();
            MonetaryUtil.calcVat(BigDecimal.valueOf(priceDouble), BigDecimal.valueOf(percentageDouble));
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzFixScale(FuzzedDataProvider data) {
        try {
            double rawDouble = data.consumeDouble();
            String currencyCode = data.consumeString(10);
            MonetaryUtil.fixScale(BigDecimal.valueOf(rawDouble), currencyCode);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
