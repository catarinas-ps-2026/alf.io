# Módulo de Reservación - Análisis de Tests

## Descripción General

Módulo que gestiona el flujo completo de reserva de tickets: desde la selección de eventos y llenado de formularios hasta la confirmación y pago.

```
Booking → Overview → [ProcessingPayment / OfflinePayment / CustomOfflinePayment / DeferredOfflinePayment] → Success
```

---


## Clasificación de Componentes

### IMPORTANTES - Lógica de Negocio (9 componentes)

| Componente | Archivo | Descripción |
|------------|---------|--------|
| **overview** | overview.component.ts | Gestión completa de reserva: ngOnInit con zip(), confirmación, pago, métricas, formularios |
| **booking** | booking.component.ts | Flujo principal de reserva: formulario contacto/tickets, validación, Italian E-Invoicing |
| **success** | success.component.ts | Confirmación de reserva: polling de estado, tickets, wallet, email |
| **success-subscription** | success-subscription.component.ts | Confirmación de suscripción: eventos compatibles, PIN display, reenvío email |
| **processing-payment** | processing-payment.component.ts| Procesamiento de pago: polling de estado, forceCheck, navegación |
| **offline-payment** | offline-payment.component.ts  | Pago offline: polling de estado, invoice disponible |
| **custom-offline-payment** | custom-offline-payment.component.ts |  Método pago custom: polling, localización de payment method |
| **payment-method-selector** | payment-method-selector.component.ts | Selector de métodos: filtrado, localización, emisión de provider |
| **invoice-form** | invoice-form.component.ts | Formulario factura: Italian E-Invoicing, países VAT, validación |

### SECUNDARIOS - Servicios/Synch (7 componentes)

| Componente | Archivo | Descripción |
|------------|---------|-------|
| **ticket-form** | ticket-form.component.ts| Sincroniza idioma con form, getter emailEditForbidden |
| **error** | error.component.ts | Página de error simple: carga contexto |
| **deferred-offline-payment** | deferred-offline-payment.component.ts | Página simple: carga contexto y reservation |

### MODALES/UI - Solo presentación (6 componentes)

| Componente | Archivo | Descripción |
|------------|---------|--------|
| **cancel-reservation** | cancel-reservation.component.ts | Modal simple: solo NgbActiveModal |
| **download-ticket** | download-ticket.component.ts | Modal: descarga ticket, getters wallet config |
| **release-ticket** | release-ticket.component.ts  | Modal simple: solo NgbActiveModal |
| **modal-remove-subscription** | modal-remove-subscription.component.ts | Modal simple: solo NgbActiveModal |
| **reservation-expired** | reservation-expired.component.ts | Modal simple: solo NgbActiveModal |
| **animated-dots** | animated-dots.component.ts | Componente visual puro: sin lógica |

### OTROS (1 componente)

| Componente | Archivo | Razón |
|------------|---------|-------|
| **summary-table** | summary-table.component.ts | Componente de presentación puro, getters simples |

## Cobertura de Tests (17 componentes)

| Componente | Tests | Estado |
|------------|-------|--------|
|booking.component | 33 | Completo | 
|cancel-reservation.component | 5 | Complejto|
|custom-offline-payment.component | 11 | Completo |
|deferred-offline-payment.component | 3 | Completo |
|download-ticket.component | 13 | Completo |
|error.component | 5 | Completo |
|expired-reservation.component | 5 | Completo |
|invoice-form.component | 28 | Completo |
|modal-remove-subscription.component | 5 | Completo |
|offline-payment.component | 9 | Completo |
|overview.component | 53 | Completo |
|payment-method-selector.component | 14 | Completo |
|processing-payment.component | 14 | Completo |
|release-ticket.component | 8 | Completo |
|success-subscription.component | 18 | Completo |
|success.component | 32 | Completo |
|ticket-form.component | 9 | Completo |

|total | 262 | Completo |

## Estructura de Archivos

```
src/app/reservation/
├── README.md                          # Este archivo
├── test-data.ts                       # Datos mock 
├── overview/
│   └── overview.component.test.ts
├── booking/
│   └── booking.component.test.ts
├── success/
│   └── success.component.test.ts
├── success-subscription/
│   └── success-subscription.component.test.ts
├── processing-payment/
│   └── processing-payment.component.test.ts
├── offline-payment/
│   └── offline-payment.component.test.ts
├── custom-offline-payment/
│   └── custom-offline-payment.component.test.ts
├── deferred-offline-payment/
│   └── deferred-offline-payment.component.test.ts
├── payment-method-selector/
│   └── payment-method-selector.component.test.ts
├── invoice-form/
│   └── invoice-form.component.test.ts
├── error/
│   └── error.component.test.ts
├── ticket-form/
│   └── ticket-form.component.test.ts
├── summary-table/                     # Sin tests - solo presentación
├── cancel-reservation/                # Modal - sin tests necesarios
├── download-ticket/                   # Modal - sin tests necesarios
├── release-ticket/                    # Modal - sin tests necesarios
├── modal-remove-subscription/        # Modal - sin tests necesarios
├── reservation-expired/              # Modal - sin tests necesarios
└── animated-dots/                     # Visual - sin tests necesarios
```

## Patrones Comunes en Tests

### Configuración de Mock ActivatedRoute
```typescript
const mockActivatedRoute = {
    data: of({ type: 'event', publicIdentifierParameter: 'eventShortName' }),
    params: of({ eventShortName: 'test-event', reservationId: 'res-123' }),
    queryParams: of({}),
    snapshot: {
        queryParamMap: { has: () => false, get: () => null },
        params: { eventShortName: 'test-event', reservationId: 'res-123' },
    },
};
```

### Configuración de Mock Services
```typescript
const mockReservationService = {
    getReservationInfo: vi.fn(() => of(mockReservationInfo)),
    confirmOverview: vi.fn(() => of({ success: true })),
    forcePaymentStatusCheck: vi.fn(() => of({ success: false })),
};

const mockPurchaseContextService = {
    getContext: vi.fn(() => of(mockPurchaseContext)),
};
```

### Configuración de TestBed (patrón completo)
```typescript
await TestBed.configureTestingModule({
    declarations: [ComponentUnderTest],
    providers: [
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: ReservationService, useValue: mockReservationService },
        { provide: PurchaseContextService, useValue: mockPurchaseContextService },
        { provide: I18nService, useValue: mockI18nService },
        { provide: AnalyticsService, useValue: mockAnalyticsService },
        { provide: TranslateService, useValue: mockTranslateService },
        { provide: NgbModal, useValue: mockModalService },
    ],
    imports: [
        TranslateModule.forRoot(),
        ReactiveFormsModule,
        NgbModule,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
}).compileComponents();
```

## Patrones de Lógica de Negocio Testeados

### ngOnInit con zip() de route.data y route.params
```typescript
zip(this.route.data, this.route.params).subscribe(([data, params]) => {
    this.publicIdentifier = params[data.publicIdentifierParameter];
    this.reservationId = params['reservationId'];
    // carga datos...
});
```

### Polling de Estado de Reserva
```typescript
pollReservationStatus(
    this.reservationId,
    this.reservationService,
    (res) => { this.reservationInfo = res; },
);
```

### Localización con I18nService
```typescript
const currentLang = this.i18nService.getCurrentLang();
const localizationKeys = Object.keys(localizations);
let translated = localizations['en'] || localizations[localizationKeys[0]];
if (localizationKeys.includes(currentLang)) {
    translated = localizations[currentLang];
}
```

### Validación de Formularios
```typescript
handleServerSideValidationError(error, this.form);
```

## Notas de Migración (Jasmine → Vitest)

| Patrón Jasmine | Patrón Vitest |
|---------------|---------------|
| `done()` callback | `async/await` |
| `route.data` como objeto plano | `route.data` envuelto en `of()` |
| `toBeTrue()` / `toBeFalse()` | `toBe(true)` / `toBe(false)` |
| `jasmine.createSpy()` | `vi.fn()` |
| `jasmine.any(Object)` | `expect.any(Object)` |
| `fixture.debugElement.nativeElement` | `fixture.nativeElement` |
| `fixture.whenStable()` | `await fixture.whenStable()` |
| TranslateModule.forRoot() | Required en imports |
