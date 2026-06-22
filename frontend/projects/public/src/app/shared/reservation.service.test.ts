import {
    HttpClientTestingModule,
    HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ReservationService } from './reservation.service';
import type { ReservationRequest } from '../model/reservation-request';
import type { ValidatedResponse } from '../model/validated-response';
import type { ReservationInfo } from '../model/reservation-info';

describe('ReservationService', () => {
    let service: ReservationService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [ReservationService],
        });
        service = TestBed.inject(ReservationService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    describe('reserveTickets', () => {
        it('should POST reservation request to reserve-tickets endpoint', async () => {
            const eventShortName = 'test-event';
            const reservation: ReservationRequest = { items: [] };
            const lang = 'en';
            const mockResponse: ValidatedResponse<string> = {
                entity: 'reservation-id-123',
                success: true,
                validationResult: null,
            };

            const promise = new Promise<ValidatedResponse<string>>(
                (resolve) => {
                    service
                        .reserveTickets(eventShortName, reservation, lang)
                        .subscribe({
                            next: (res) => resolve(res),
                        });
                },
            );

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/event/${eventShortName}/reserve-tickets`,
                ),
            );
            expect(req.request.method).toBe('POST');
            expect(req.request.params.get('lang')).toBe(lang);
            req.flush(mockResponse);

            const result = await promise;
            expect(result).toEqual(mockResponse);
        });
    });

    describe('getReservationInfo', () => {
        it('should GET reservation info by reservation ID', async () => {
            const reservationId = 'res-123';
            const mockResponse: ReservationInfo = {
                id: 'res-123',
                shortId: 'ABC123',
                firstName: 'John',
                lastName: 'Doe',
                email: 'john@example.com',
                validity: 900,
                ticketsByCategory: [],
                orderSummary: {
                    summary: [],
                    totalPrice: '100.00',
                    free: false,
                    displayVat: true,
                    priceInCents: 10000,
                    descriptionForPayment: 'Test',
                    totalVAT: '22.00',
                    vatPercentage: '22%',
                },
                status: 'PENDING',
                validatedBookingInformation: true,
                formattedExpirationDate: {},
                invoiceNumber: null,
                invoiceRequested: false,
                invoiceOrReceiptDocumentPresent: false,
                paid: false,
                tokenAcquired: false,
                paymentProxy: null,
                addCompanyBillingDetails: false,
                customerReference: '',
                skipVatNr: false,
                billingAddress: '',
                billingDetails: {
                    companyName: '',
                    addressLine1: '',
                    addressLine2: '',
                    zip: '',
                    city: '',
                    state: '',
                    country: 'US',
                    taxId: '',
                    invoicingAdditionalInfo: {},
                },
                containsCategoriesLinkedToGroups: false,
                activePaymentMethods: {},
                metadata: {
                    hideContactData: false,
                    lockEmailEdit: false,
                    hideConfirmationButtons: false,
                    readyForConfirmation: false,
                    finalized: false,
                },
            };

            const promise = new Promise<ReservationInfo>((resolve) => {
                service
                    .getReservationInfo(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(`/api/v2/public/reservation/${reservationId}`),
            );
            expect(req.request.method).toBe('GET');
            req.flush(mockResponse);

            const result = await promise;
            expect(result).toEqual(mockResponse);
        });
    });

    describe('cancelPendingReservation', () => {
        it('should DELETE pending reservation', async () => {
            const reservationId = 'res-123';

            const promise = new Promise<boolean>((resolve) => {
                service
                    .cancelPendingReservation(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(`/api/v2/public/reservation/${reservationId}`),
            );
            expect(req.request.method).toBe('DELETE');
            req.flush(true);

            const result = await promise;
            expect(result).toBe(true);
        });
    });

    describe('validateToOverview', () => {
        it('should POST validation request with ignoreWarnings param', async () => {
            const reservationId = 'res-123';
            const mockResponse: ValidatedResponse<boolean> = {
                entity: true,
                success: true,
                validationResult: null,
            };

            const promise = new Promise<ValidatedResponse<boolean>>(
                (resolve) => {
                    service
                        .validateToOverview(
                            reservationId,
                            { firstName: 'John' },
                            'en',
                            true,
                        )
                        .subscribe({
                            next: (res) => resolve(res),
                        });
                },
            );

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/validate-to-overview`,
                ),
            );
            expect(req.request.method).toBe('POST');
            expect(req.request.params.get('lang')).toBe('en');
            expect(req.request.params.get('ignoreWarnings')).toBe('true');
            req.flush(mockResponse);

            const result = await promise;
            expect(result).toEqual(mockResponse);
        });

        it('should handle ignoreWarnings=false correctly', async () => {
            const reservationId = 'res-123';
            const mockResponse: ValidatedResponse<boolean> = {
                entity: false,
                success: false,
                validationResult: { errors: [] },
            };

            const promise = new Promise<ValidatedResponse<boolean>>(
                (resolve) => {
                    service
                        .validateToOverview(reservationId, {}, 'en', false)
                        .subscribe({ next: (res) => resolve(res) });
                },
            );

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/validate-to-overview`,
                ),
            );
            expect(req.request.params.get('ignoreWarnings')).toBe('false');
            req.flush(mockResponse);

            const result = await promise;
            expect(result).toEqual(mockResponse);
        });
    });

    describe('backToBooking', () => {
        it('should POST to back-to-booking endpoint', async () => {
            const reservationId = 'res-123';

            const promise = new Promise<boolean>((resolve) => {
                service
                    .backToBooking(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/back-to-booking`,
                ),
            );
            expect(req.request.method).toBe('POST');
            expect(req.request.body).toEqual({});
            req.flush(true);

            const result = await promise;
            expect(result).toBe(true);
        });
    });

    describe('reSendReservationEmail', () => {
        it('should POST to re-send-email endpoint with correct params', async () => {
            const purchaseContextType = 'event';
            const publicIdentifier = 'test-event';
            const reservationId = 'res-123';
            const lang = 'en';

            const promise = new Promise<boolean>((resolve) => {
                service
                    .reSendReservationEmail(
                        purchaseContextType,
                        publicIdentifier,
                        reservationId,
                        lang,
                    )
                    .subscribe({
                        next: (res) => resolve(res),
                    });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/${purchaseContextType}/${publicIdentifier}/reservation/${reservationId}/re-send-email`,
                ),
            );
            expect(req.request.method).toBe('POST');
            expect(req.request.params.get('lang')).toBe(lang);
            req.flush(true);

            const result = await promise;
            expect(result).toBe(true);
        });
    });

    describe('initPayment', () => {
        it('should POST to init payment endpoint', async () => {
            const reservationId = 'res-123';
            const mockToken = { token: 'tok-123', paymentGateway: 'STRIPE' };

            const promise = new Promise<any>((resolve) => {
                service
                    .initPayment(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/payment/CREDIT_CARD/init`,
                ),
            );
            expect(req.request.method).toBe('POST');
            req.flush(mockToken);

            const result = await promise;
            expect(result).toEqual(mockToken);
        });
    });

    describe('getPaymentStatus', () => {
        it('should GET payment status', async () => {
            const reservationId = 'res-123';
            const mockResult = { success: true };

            const promise = new Promise<any>((resolve) => {
                service
                    .getPaymentStatus(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/payment/CREDIT_CARD/status`,
                ),
            );
            expect(req.request.method).toBe('GET');
            req.flush(mockResult);

            const result = await promise;
            expect(result).toEqual(mockResult);
        });
    });

    describe('forcePaymentStatusCheck', () => {
        it('should GET force check endpoint', async () => {
            const reservationId = 'res-123';
            const mockResult = { status: 'PENDING' };

            const promise = new Promise<any>((resolve) => {
                service
                    .forcePaymentStatusCheck(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/transaction/force-check`,
                ),
            );
            expect(req.request.method).toBe('GET');
            req.flush(mockResult);

            const result = await promise;
            expect(result).toEqual(mockResult);
        });
    });

    describe('removePaymentToken', () => {
        it('should DELETE payment token', async () => {
            const reservationId = 'res-123';

            const promise = new Promise<boolean>((resolve) => {
                service
                    .removePaymentToken(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/payment/token`,
                ),
            );
            expect(req.request.method).toBe('DELETE');
            req.flush(true);

            const result = await promise;
            expect(result).toBe(true);
        });
    });

    describe('resetPaymentStatus', () => {
        it('should DELETE payment status', async () => {
            const reservationId = 'res-123';

            const promise = new Promise<boolean>((resolve) => {
                service
                    .resetPaymentStatus(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/payment`,
                ),
            );
            expect(req.request.method).toBe('DELETE');
            req.flush(true);

            const result = await promise;
            expect(result).toBe(true);
        });
    });

    describe('registerPaymentAttempt', () => {
        it('should PUT payment attempt', async () => {
            const reservationId = 'res-123';

            const promise = new Promise<boolean>((resolve) => {
                service
                    .registerPaymentAttempt(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/payment`,
                ),
            );
            expect(req.request.method).toBe('PUT');
            req.flush(true);

            const result = await promise;
            expect(result).toBe(true);
        });
    });

    describe('checkDynamicDiscountAvailability', () => {
        it('should POST to check-discount endpoint', async () => {
            const eventShortName = 'test-event';
            const reservation: ReservationRequest = { items: [] };
            const mockDiscount = { code: 'DISCOUNT10', value: 10 };

            const promise = new Promise<any>((resolve) => {
                service
                    .checkDynamicDiscountAvailability(
                        eventShortName,
                        reservation,
                    )
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/event/${eventShortName}/check-discount`,
                ),
            );
            expect(req.request.method).toBe('POST');
            req.flush(mockDiscount);

            const result = await promise;
            expect(result).toEqual(mockDiscount);
        });
    });

    describe('applySubscriptionCode', () => {
        it('should POST subscription code with correct body', async () => {
            const reservationId = 'res-123';
            const code = 'SUB123';
            const email = 'test@example.com';
            const mockResponse: ValidatedResponse<boolean> = {
                entity: true,
                success: true,
                validationResult: null,
            };

            const promise = new Promise<ValidatedResponse<boolean>>(
                (resolve) => {
                    service
                        .applySubscriptionCode(reservationId, code, email)
                        .subscribe({ next: (res) => resolve(res) });
                },
            );

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/apply-code`,
                ),
            );
            expect(req.request.method).toBe('POST');
            expect(req.request.body).toEqual({
                code,
                email,
                amount: 1,
                type: 'SUBSCRIPTION',
            });
            req.flush(mockResponse);

            const result = await promise;
            expect(result).toEqual(mockResponse);
        });
    });

    describe('removeSubscription', () => {
        it('should DELETE subscription with type param', async () => {
            const reservationId = 'res-123';

            const promise = new Promise<boolean>((resolve) => {
                service
                    .removeSubscription(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/remove-code`,
                ),
            );
            expect(req.request.method).toBe('DELETE');
            expect(req.request.params.get('type')).toBe('SUBSCRIPTION');
            req.flush(true);

            const result = await promise;
            expect(result).toBe(true);
        });
    });

    describe('getApplicableCustomPaymentMethodDetails', () => {
        it('should GET applicable custom payment methods', async () => {
            const reservationId = 'res-123';
            const mockMethods = [{ id: 'custom-1', name: 'Custom Method 1' }];

            const promise = new Promise<any>((resolve) => {
                service
                    .getApplicableCustomPaymentMethodDetails(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/applicable-custom-payment-method-details`,
                ),
            );
            expect(req.request.method).toBe('GET');
            req.flush(mockMethods);

            const result = await promise;
            expect(result).toEqual(mockMethods);
        });
    });

    describe('getSelectedCustomPaymentMethodDetails', () => {
        it('should GET selected custom payment method details', async () => {
            const reservationId = 'res-123';
            const mockMethod = { id: 'custom-1', name: 'Custom Method 1' };

            const promise = new Promise<any>((resolve) => {
                service
                    .getSelectedCustomPaymentMethodDetails(reservationId)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne((r) =>
                r.url.includes(
                    `/api/v2/public/reservation/${reservationId}/selected-custom-payment-method-details`,
                ),
            );
            expect(req.request.method).toBe('GET');
            req.flush(mockMethod);

            const result = await promise;
            expect(result).toEqual(mockMethod);
        });
    });
});
