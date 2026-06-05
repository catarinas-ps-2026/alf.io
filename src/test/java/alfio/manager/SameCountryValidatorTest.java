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
package alfio.manager;

import alfio.manager.system.ConfigurationManager;
import alfio.model.PurchaseContext;
import alfio.model.VatDetail;
import ch.digitalfondue.vatchecker.EUVatCheckResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class SameCountryValidatorTest {

    @Mock
    private ConfigurationManager configurationManager;
    @Mock
    private ExtensionManager extensionManager;
    @Mock
    private PurchaseContext purchaseContext;
    @Mock
    private EuVatChecker checker;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testVatCheckingDisabled() {
        try (MockedStatic<EuVatChecker> euVatCheckerMockedStatic = mockStatic(EuVatChecker.class)) {
            euVatCheckerMockedStatic.when(() -> EuVatChecker.organizerCountry(any(), any())).thenReturn("IT");
            euVatCheckerMockedStatic.when(() -> EuVatChecker.validationEnabled(any(), any())).thenReturn(false);

            SameCountryValidator validator = new SameCountryValidator(
                    configurationManager,
                    extensionManager,
                    purchaseContext,
                    "reservation-123",
                    checker
            );

            assertFalse(validator.test("12345"));
        }
    }

    @Test
    public void testVatValidStrict() {
        try (MockedStatic<EuVatChecker> euVatCheckerMockedStatic = mockStatic(EuVatChecker.class)) {
            euVatCheckerMockedStatic.when(() -> EuVatChecker.organizerCountry(any(), any())).thenReturn("IT");
            euVatCheckerMockedStatic.when(() -> EuVatChecker.validationEnabled(any(), any())).thenReturn(true);

            EUVatCheckResponse mockResponse = mock(EUVatCheckResponse.class);
            when(mockResponse.isValid()).thenReturn(true);
            euVatCheckerMockedStatic.when(() -> EuVatChecker.validateEUVat(eq("12345"), eq("IT"), any())).thenReturn(mockResponse);

            SameCountryValidator validator = new SameCountryValidator(
                    configurationManager,
                    extensionManager,
                    purchaseContext,
                    "reservation-123",
                    checker
            );

            assertTrue(validator.test("12345"));
            verify(checker).logSuccessfulValidation(any(VatDetail.class), eq("reservation-123"), eq(purchaseContext));
        }
    }

    @Test
    public void testVatInvalidButExtensionSucceeds() {
        try (MockedStatic<EuVatChecker> euVatCheckerMockedStatic = mockStatic(EuVatChecker.class)) {
            euVatCheckerMockedStatic.when(() -> EuVatChecker.organizerCountry(any(), any())).thenReturn("IT");
            euVatCheckerMockedStatic.when(() -> EuVatChecker.validationEnabled(any(), any())).thenReturn(true);

            EUVatCheckResponse mockResponse = mock(EUVatCheckResponse.class);
            when(mockResponse.isValid()).thenReturn(false);
            euVatCheckerMockedStatic.when(() -> EuVatChecker.validateEUVat(eq("12345"), eq("IT"), any())).thenReturn(mockResponse);

            when(extensionManager.handleTaxIdValidation(purchaseContext, "12345", "IT")).thenReturn(true);

            SameCountryValidator validator = new SameCountryValidator(
                    configurationManager,
                    extensionManager,
                    purchaseContext,
                    "reservation-123",
                    checker
            );

            assertTrue(validator.test("12345"));
            verify(checker).logSuccessfulValidation(any(VatDetail.class), eq("reservation-123"), eq(purchaseContext));
        }
    }

    @Test
    public void testVatInvalidAndExtensionFails() {
        try (MockedStatic<EuVatChecker> euVatCheckerMockedStatic = mockStatic(EuVatChecker.class)) {
            euVatCheckerMockedStatic.when(() -> EuVatChecker.organizerCountry(any(), any())).thenReturn("IT");
            euVatCheckerMockedStatic.when(() -> EuVatChecker.validationEnabled(any(), any())).thenReturn(true);

            EUVatCheckResponse mockResponse = mock(EUVatCheckResponse.class);
            when(mockResponse.isValid()).thenReturn(false);
            euVatCheckerMockedStatic.when(() -> EuVatChecker.validateEUVat(eq("12345"), eq("IT"), any())).thenReturn(mockResponse);

            when(extensionManager.handleTaxIdValidation(purchaseContext, "12345", "IT")).thenReturn(false);

            SameCountryValidator validator = new SameCountryValidator(
                    configurationManager,
                    extensionManager,
                    purchaseContext,
                    "reservation-123",
                    checker
            );

            assertFalse(validator.test("12345"));
            verify(checker, never()).logSuccessfulValidation(any(), any(), any());
        }
    }
}
