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

import alfio.model.result.ValidationResult;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import java.util.ArrayList;
import java.util.List;

public class ValidationResultFuzzTest {

    @FuzzTest
    public void fuzzValidationResultFailed(FuzzedDataProvider data) {
        try {
            int numErrors = data.consumeInt(0, 50);
            List<ValidationResult.ErrorDescriptor> errors = new ArrayList<>();
            for (int i = 0; i < numErrors; i++) {
                String fieldName = data.consumeString(50);
                String message = data.consumeString(100);
                String code = data.consumeString(30);
                errors.add(new ValidationResult.ErrorDescriptor(fieldName, message, code));
            }
            ValidationResult result = ValidationResult.failed(errors);
            result.isSuccess();
            result.getErrorCount();
            result.getValidationErrors();
            result.or(ValidationResult.success());
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzValidationResultOr(FuzzedDataProvider data) {
        try {
            String fieldName = data.consumeString(50);
            String message = data.consumeString(100);
            String code = data.consumeString(30);
            ValidationResult.ErrorDescriptor descriptor =
                    new ValidationResult.ErrorDescriptor(fieldName, message, code);
            ValidationResult first = ValidationResult.failed(descriptor);
            ValidationResult second = ValidationResult.failed(descriptor);
            first.or(second);
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }

    @FuzzTest
    public void fuzzValidationResultIfSuccess(FuzzedDataProvider data) {
        try {
            ValidationResult result = ValidationResult.success();
            result.ifSuccess(() -> {
                // no-op
            });
        } catch (NullPointerException | IllegalArgumentException e) {
            // catch exceptions from invalid inputs
        }
    }
}
