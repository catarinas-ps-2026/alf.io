# Módulo de Pago - Análisis de Tests

## Descripción General
Módulo que maneja los proxies de pago en el flujo de reservas. Cada componente de proxy adapta un proveedor de pago específico (Stripe, PayPal, Mollie, etc.) a la interfaz unificada `PaymentProvider`.

## Clasificación de Componentes

### IMPORTANTES - Lógica de Negocio (4 componentes)

| Componente | Archivo | Descripción |
|------------|---------|-------|
| **stripe-payment-proxy** | stripe-payment-proxy.component.ts | Carga dinámica de scripts (SCA/non-SCA), TranslateService, ReservationService, integración Stripe v3 |
| **offline-payment-proxy** | offline-payment-proxy.component.ts| Interacción con formulario (handleRecaptchaResponse), lógica propiedad `deferred` |
| **onsite-payment-proxy** | onsite-payment-proxy.component.ts | Interacción con formulario (handleRecaptchaResponse) |
| **custom-offline-payment-proxy** | custom-offline-payment-proxy.component.ts| I18nService, lógica de localización, getter `selectedPaymentMethodDescription` |

### SECUNDARIOS - Paso Directo Simple (3 componentes)

| Componente | Archivo | Razón |
|------------|---------|--------|
| **saferpay-payment-proxy** | saferpay-payment-proxy.component.ts| Solo emite en ngOnChanges |
| **paypal-payment-proxy** | paypal-payment-proxy.component.ts  | Solo emite en ngOnChanges |
| **mollie-payment-proxy** | mollie-payment-proxy.component.ts | Solo emite en ngOnChanges |

## Estrategia de Tests

### Orden de Prioridad
1. **stripe-payment-proxy** - Más complejo, maneja flujo SCA, carga de scripts
2. **custom-offline-payment-proxy** - Lógica de localización con I18nService
3. **offline-payment-proxy** - Manejo de formularios con recaptcha
4. **onsite-payment-proxy** - Similar a offline pero más simple

### Cobertura de Tests

#### stripe-payment-proxy (10 tests)
- ngOnChanges emite paymentProvider cuando proxy/method coinciden
- ngOnChanges llama unloadAll cuando proxy no coincide
- matchProxyAndMethod retorna true para proxy STRIPE
- useSCA getter retorna el valor de parameters.enableSCA
- No emite cuando el método no ha cambiado

#### custom-offline-payment-proxy (14 tests)
- ngOnChanges emite CustomOfflinePaymentProvider cuando hay match
- matchProxyAndMethod retorna false cuando method/proxy son undefined
- matchProxyAndMethod retorna false cuando availableMethods está vacío
- matchProxyAndMethod retorna true cuando encuentra coincidencia
- selectedPaymentMethodDescription retorna fallback en inglés
- selectedPaymentMethodDescription retorna descripción localizada

#### offline-payment-proxy (12 tests)
- ngOnChanges emite SimplePaymentProvider cuando hay match
- matchProxyAndMethod retorna true para BANK_TRANSFER + OFFLINE
- deferred getter retorna parameters.deferred
- handleRecaptchaResponse establece valor del formulario

#### onsite-payment-proxy (8 tests)
- ngOnChanges emite SimplePaymentProvider cuando hay match
- matchProxyAndMethod retorna true para ON_SITE + ON_SITE
- handleRecaptchaResponse establece valor del formulario

### payment-provider (12 tests)
- getPaymentProvider retorna el provider correcto para cada proxy
- getPaymentProvider retorna null para proxy desconocido


|Componente | Tests | Estado |
|-----------|-------|--------|
|custom-offline-payment-proxy.component | 14 | Completo |
|onsite-payment-proxy.component | 8 | Completo |
|offline-payment-proxy.component | 12 | Completo |
|stripe-payment-proxy.component | 10 | Completo |
|payment-provider.component | 12 | Completo |
|total | 56 | Completo |

## Estructura de Archivos

```
src/app/payment/
├── README.md                          # Este archivo
├── test-data.ts                       # Datos mock compartidos
│── payment-provider.component.test.ts
├── stripe-payment-proxy/
│   └── stripe-payment-proxy.component.test.ts
├── offline-payment-proxy/
│   └── offline-payment-proxy.component.test.ts
├── onsite-payment-proxy/
│   └── onsite-payment-proxy.component.test.ts
└── custom-offline-payment-proxy/
    └── custom-offline-payment-proxy.component.test.ts
```


## Patrones Comunes

### Configuración de Mocks
- ActivatedRoute: `data: of({}), params: of({}), snapshot: {}`
- Services: `vi.fn(() => of(mockData))`
- FormGroup: `{ get: vi.fn(() => ({ setValue: vi.fn() })) }`

### Configuración de TestBed
```typescript
TestBed.configureTestingModule({
    declarations: [ComponentUnderTest],
    providers: [
        { provide: ServiceX, useValue: mockServiceX },
        { provide: ActivatedRoute, useValue: mockRoute },
    ],
    imports: [TranslateModule.forRoot()],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
});
```

## Notas de Migración (Jasmine → Vitest)

- `done()` callback → async/await
- `route.data` como objeto → `route.data` como `of(objeto)`
- `toBeTrue()` / `toBeFalse()` → `toBe(true)` / `toBe(false)`
- `jasmine.createSpy()` → `vi.fn()`
- TranslateModule requiere `.forRoot()` en imports de tests
