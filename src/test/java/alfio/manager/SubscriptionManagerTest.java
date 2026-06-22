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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import alfio.model.modification.SubscriptionDescriptorModification;
import alfio.model.subscription.SubscriptionDescriptor;
import alfio.repository.EventRepository;
import alfio.repository.SubscriptionRepository;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class SubscriptionManagerTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private Environment environment;

    private SubscriptionManager manager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        manager = new SubscriptionManager(subscriptionRepository, eventRepository, jdbcTemplate, environment);
    }

    @Test
    public void testFindAll() {
        List<SubscriptionDescriptor> list = Collections.singletonList(mock(SubscriptionDescriptor.class));
        when(subscriptionRepository.findAllByOrganizationIds(1)).thenReturn(list);

        List<SubscriptionDescriptor> result = manager.findAll(1);

        assertEquals(list, result);
        verify(subscriptionRepository).findAllByOrganizationIds(1);
    }

    @Test
    public void testCreateSubscriptionDescriptorSuccess() {
        SubscriptionDescriptorModification modification = mock(SubscriptionDescriptorModification.class);
        when(modification.getMaxAvailable()).thenReturn(10);
        when(modification.getTitle()).thenReturn(Map.of("en", "Gold Subscription"));
        when(modification.getDescription()).thenReturn(Map.of("en", "Access to all VIP events"));
        when(modification.getOnSaleFrom()).thenReturn(null);
        when(modification.getOnSaleTo()).thenReturn(null);
        when(modification.getPriceCts()).thenReturn(5000);
        when(modification.getVat()).thenReturn(new BigDecimal("8.0"));
        when(modification.getVatStatus()).thenReturn(null);
        when(modification.getCurrency()).thenReturn("CHF");
        when(modification.getIsPublic()).thenReturn(true);
        when(modification.getOrganizationId()).thenReturn(1);
        when(modification.getMaxEntries()).thenReturn(5);
        when(modification.getValidityType()).thenReturn(null);
        when(modification.getValidityTimeUnit()).thenReturn(null);
        when(modification.getValidityUnits()).thenReturn(null);
        when(modification.getValidityFrom()).thenReturn(null);
        when(modification.getValidityTo()).thenReturn(null);
        when(modification.getUsageType()).thenReturn(null);
        when(modification.getTermsAndConditionsUrl()).thenReturn("https://tc.url");
        when(modification.getPrivacyPolicyUrl()).thenReturn("https://pp.url");
        when(modification.getFileBlobId()).thenReturn(null);
        when(modification.getPaymentProxies()).thenReturn(Collections.emptyList());
        when(modification.getTimeZone()).thenReturn(ZoneId.of("UTC"));
        when(modification.getSupportsTicketsGeneration()).thenReturn(true);

        when(subscriptionRepository.createSubscriptionDescriptor(
                        any(UUID.class),
                        anyMap(),
                        anyMap(),
                        anyInt(),
                        any(),
                        any(),
                        anyInt(),
                        any(BigDecimal.class),
                        any(),
                        anyString(),
                        anyBoolean(),
                        anyInt(),
                        anyInt(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anyString(),
                        anyString(),
                        any(),
                        anyList(),
                        anyString(),
                        anyString(),
                        anyBoolean()))
                .thenReturn(1);

        Optional<UUID> result = manager.createSubscriptionDescriptor(modification);

        assertTrue(result.isPresent());
    }

    @Test
    public void testCreateSubscriptionDescriptorFailure() {
        SubscriptionDescriptorModification modification = mock(SubscriptionDescriptorModification.class);
        when(modification.getMaxAvailable()).thenReturn(10);
        when(modification.getTitle()).thenReturn(Map.of("en", "Title"));
        when(modification.getDescription()).thenReturn(Map.of("en", "Description"));
        when(modification.getVat()).thenReturn(BigDecimal.ZERO);
        when(modification.getIsPublic()).thenReturn(true);
        when(modification.getOrganizationId()).thenReturn(1);
        when(modification.getTimeZone()).thenReturn(ZoneId.of("UTC"));

        when(subscriptionRepository.createSubscriptionDescriptor(
                        any(UUID.class),
                        anyMap(),
                        anyMap(),
                        anyInt(),
                        any(),
                        any(),
                        anyInt(),
                        any(BigDecimal.class),
                        any(),
                        any(),
                        anyBoolean(),
                        anyInt(),
                        anyInt(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        anyBoolean()))
                .thenReturn(0);

        Optional<UUID> result = manager.createSubscriptionDescriptor(modification);

        assertTrue(result.isEmpty());
    }
}
