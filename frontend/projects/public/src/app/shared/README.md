# Módulo Shared - Análisis de Tests

## Descripción General
Módulo que contiene servicios compartidos utilizados en toda la aplicación: reserva, tickets, autenticación, internacionalización, analytics, y guardians de rutas.

## Clasificación de Componentes

### IMPORTANTES - Lógica de Negocio (11 componentes)

| Servicio/Guard | Archivo | Descripción |
|----------------|---------|------|
| **reservationService** | reservation.service.ts  | CRUD completo: reserve, confirm, cancel, payment, codes |
| **i18nService** | i18n.service.ts  | Cache países, translate, CustomLoader, persistence |
| **userService** | user.service.ts  | Auth BehaviorSubject, identity, logout, orders |
| **eventService** | event.service.ts  | Cache 20min, preloaded data, timezone helpers |
| **ticketService** | ticket.service.ts | Form builders, modal handling, timezone |
| **validationHelper** | validation-helper.ts  | applyValidationErrors, handleServerSideValidationError |
| **additionalFieldService** | additional-field.service.ts  | Build campos adicionales para tickets |
| **subscriptionService** | subscription.service.t | Cache subscriptions, reserve |
| **util.ts** (funciones) | util.ts| pollReservationStatus, sessionStorage, embedded |
| **EventGuard** | event.guard.ts | canActivate con handleCustomCss |
| **LanguageGuard** | language.guard.ts | Extracción lang (query, persisted, browser, default) |

### SECUNDARIOS - Wrapper HTTP Simples (3 servicios)

| Servicio | Archivo | Descripción |
|----------|---------|--------|
| **analyticsService** | analytics.service.ts | pageView con gtag, lógica simple |
| **infoService** | info.service.ts  | Cache simple con preloaded |
| **feedbackService** | feedback.service.ts | Solo emite Subject para toasts |

### COMPONENTES UI (4 componentes)
| Componente | Archivo | Descripción |
|------------|---------|-------|
| **FeedbackComponent** | feedback/feedback.component.ts | Muestra toasts con iconos |
| **WarningModalComponent** | warning-modal/warning-modal.component.ts | Modal simple con mensaje |
| **custom-css-helper.ts** | custom-css-helper.ts | Solo funciones de CSS |
| **translate-description.pipe.ts** | translate-description.pipe.ts | Pipe simple |

### Cobertura de Tests (10)

|Componente/Servicio | Tests | Estado |
|---------------------|-------|--------|
| reservation.service.test.ts | 18 | Completado |
| user.service.test.ts | 12 | Completado |
| subscription.service.test.ts | 9 | Completado |
| purchase-context.service.test.ts | 3 | Completado |
| additional-field.service.test.ts | 8 | Completado |
| info.service.test.ts | 3 | Completado |
| util.test.ts | 18 | Completado |
| feedback.service.test.ts | 6 | Completado |
| feedback.component.test.ts | 12 | Completado |
| warning-modal.component.test.ts | 5 | Completado |
| **TOTAL** | **94** | **Completado** |


## Estructura de Archivos de Tests

```
src/app/shared/
├── README.md                          # Este archivo
├── test-data.ts                       # Datos mock compartidos
├── feedback.service.test.ts           
├── feedback.component.test.ts          
├── warning-modal.component.test.ts     
├── feedback/
│   ├── feedback.component.ts           
│   ├── feedback.component.html
│   └── feedback.service.ts             
└── warning-modal/
    ├── warning-modal.component.ts      
    └── warning-modal.component.html
```

## Patrones de Tests

### Test de Servicio Simple (FeedbackService)
```typescript
const service = new FeedbackService();
service.displayNotification().subscribe((notification) => {
    expect(notification.type).toBe('SUCCESS');
});
service.showSuccess('message');
```

### Test de Componente con Dependencies
```typescript
await TestBed.configureTestingModule({
    declarations: [ComponentName],
    imports: [TranslateModule.forRoot()],
    providers: [
        { provide: ServiceType, useValue: mockService },
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
}).compileComponents();
```

## Notas de Migration (Jasmine → Vitest)

- `done()` callback → Promises con `await`
- `jasmine.createSpy()` → `vi.fn()`
- `TestBed.get()` → `TestBed.inject()`
- TranslateModule requiere `.forRoot()` en imports
- Componentes con `fa-icon` requieren `CUSTOM_ELEMENTS_SCHEMA`
