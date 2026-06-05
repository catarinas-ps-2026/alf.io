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

package alfio.repository;

import alfio.model.Audit;
import alfio.model.Event;
import alfio.model.PurchaseContext;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class AuditingRepositoryTest {

    private final AuditingRepository auditingRepository = mock(AuditingRepository.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

    @Test
    void testInsertShort() {
        auditingRepository.insert("res", 1, 2, Audit.EventType.RESERVATION_CREATE, new Date(), Audit.EntityType.TICKET, "ent");
        verify(auditingRepository).insert(eq("res"), eq(1), eq(2), eq(Audit.EventType.RESERVATION_CREATE), any(Date.class), eq(Audit.EntityType.TICKET), eq("ent"), (String) isNull());
    }

    @Test
    void testInsertWithModifications() {
        List<Map<String, Object>> mods = List.of(Map.of("k", "v"));
        auditingRepository.insert("res", 1, 2, Audit.EventType.RESERVATION_CREATE, new Date(), Audit.EntityType.TICKET, "ent", mods);
        verify(auditingRepository).insert(eq("res"), eq(1), eq(2), eq(Audit.EventType.RESERVATION_CREATE), any(Date.class), eq(Audit.EntityType.TICKET), eq("ent"), contains("k"));
    }

    @Test
    void testInsertWithPurchaseContext() {
        PurchaseContext pc = mock(PurchaseContext.class);
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(2);
        when(pc.event()).thenReturn(Optional.of(event));
        
        auditingRepository.insert("res", 1, pc, Audit.EventType.RESERVATION_CREATE, new Date(), Audit.EntityType.TICKET, "ent");
        verify(auditingRepository).insert(eq("res"), eq(1), eq(2), eq(Audit.EventType.RESERVATION_CREATE), any(Date.class), eq(Audit.EntityType.TICKET), eq("ent"), (String) isNull());
    }
}
