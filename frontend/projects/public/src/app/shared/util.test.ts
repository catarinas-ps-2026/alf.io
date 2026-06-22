import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import {
    writeToSessionStorage,
    getFromSessionStorage,
    removeFromSessionStorage,
    mobile,
    embedded,
    notifyPaymentErrorToParent,
    pollReservationStatus,
    groupAdditionalData,
    loadPreloaded,
    DELETE_ACCOUNT_CONFIRMATION,
} from './util';
import type { ReservationInfo } from '../model/reservation-info';
import type { PurchaseContext } from '../model/purchase-context';

describe('util.ts', () => {
    describe('DELETE_ACCOUNT_CONFIRMATION', () => {
        it('should export constant', () => {
            expect(DELETE_ACCOUNT_CONFIRMATION).toBe(
                'alfio.delete-account.confirmation',
            );
        });
    });

    describe('sessionStorage functions', () => {
        let mockSessionStorage: { [key: string]: string | null };

        beforeEach(() => {
            mockSessionStorage = {};
            Object.defineProperty(window, 'sessionStorage', {
                value: {
                    setItem: (key: string, value: string) => {
                        mockSessionStorage[key] = value;
                    },
                    getItem: (key: string) => mockSessionStorage[key] || null,
                    removeItem: (key: string) => {
                        delete mockSessionStorage[key];
                    },
                },
                configurable: true,
            });
        });

        describe('writeToSessionStorage', () => {
            it('should write to sessionStorage', () => {
                writeToSessionStorage('key1', 'value1');
                expect(mockSessionStorage['key1']).toBe('value1');
            });

            it('should handle sessionStorage errors gracefully', () => {
                Object.defineProperty(window, 'sessionStorage', {
                    value: {
                        setItem: () => {
                            throw new Error('Storage disabled');
                        },
                    },
                    configurable: true,
                });
                expect(() =>
                    writeToSessionStorage('key', 'value'),
                ).not.toThrow();
            });
        });

        describe('getFromSessionStorage', () => {
            it('should return value from sessionStorage', () => {
                mockSessionStorage['ALFIO_LANG'] = 'en';
                expect(getFromSessionStorage('ALFIO_LANG')).toBe('en');
            });

            it('should return null for non-existent key', () => {
                expect(getFromSessionStorage('nonExistent')).toBeNull();
            });

            it('should handle sessionStorage errors gracefully', () => {
                Object.defineProperty(window, 'sessionStorage', {
                    value: {
                        getItem: () => {
                            throw new Error('Storage disabled');
                        },
                    },
                    configurable: true,
                });
                expect(getFromSessionStorage('key')).toBeNull();
            });
        });

        describe('removeFromSessionStorage', () => {
            it('should remove value from sessionStorage', () => {
                mockSessionStorage['key1'] = 'value1';
                removeFromSessionStorage('key1');
                expect(mockSessionStorage['key1']).toBeUndefined();
            });

            it('should handle sessionStorage errors gracefully', () => {
                Object.defineProperty(window, 'sessionStorage', {
                    value: {
                        removeItem: () => {
                            throw new Error('Storage disabled');
                        },
                    },
                    configurable: true,
                });
                expect(() => removeFromSessionStorage('key')).not.toThrow();
            });
        });
    });

    describe('mobile', () => {
        it('should be a boolean value', () => {
            expect(typeof mobile).toBe('boolean');
        });
    });

    describe('embedded', () => {
        it('should be a boolean value', () => {
            expect(typeof embedded).toBe('boolean');
        });
    });

    describe('notifyPaymentErrorToParent', () => {
        let mockPurchaseContext: PurchaseContext;
        let mockReservationInfo: ReservationInfo;
        let postMessageSpy: any;

        beforeEach(() => {
            mockPurchaseContext = {
                id: 1,
                type: 'event',
                publicIdentifier: 'test-event',
                title: { en: 'Test Event' },
                description: {},
                shortDescription: {},
                imageUrl: null,
                format: 'IN_PERSON',
                startDate: '',
                endDate: '',
                enabled: true,
                analyticsConfiguration: null,
                embeddingConfiguration: {
                    enabled: true,
                    notificationOrigin: 'https://origin.com',
                },
                additionalCategories: [],
                ticketCategories: [],
                localization: {},
                privacyPolicyUrl: null,
                invoicingConfiguration: {
                    enabled: false,
                    onlyInvoice: false,
                    vatIncluded: false,
                    userCanDownloadReceiptOrInvoice: false,
                    enabledItalyEInvoicing: false,
                },
                assignmentConfiguration: { enableAttendeeAutocomplete: true },
            } as unknown as PurchaseContext;

            mockReservationInfo = {
                id: 'res-123',
                shortId: 'ABC123',
                firstName: 'John',
                lastName: 'Doe',
                email: 'john@example.com',
                validity: 900,
                ticketsByCategory: [],
                orderSummary: {
                    summary: [],
                    totalPrice: '100',
                    free: false,
                    displayVat: true,
                    priceInCents: 10000,
                    descriptionForPayment: 'Test',
                    totalVAT: '22',
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

            postMessageSpy = vi.fn();
            Object.defineProperty(window, 'parent', {
                value: { postMessage: postMessageSpy },
                configurable: true,
            });
        });

        it('should do nothing when embedded is false', () => {
            Object.defineProperty(window, 'parent', {
                value: window,
                configurable: true,
            });
            notifyPaymentErrorToParent(
                mockPurchaseContext,
                mockReservationInfo,
                'res-123',
                new Error('test error'),
            );
            expect(postMessageSpy).not.toHaveBeenCalled();
        });

        it('should do nothing when embedding is not enabled', () => {
            mockPurchaseContext.embeddingConfiguration.enabled = false;
            notifyPaymentErrorToParent(
                mockPurchaseContext,
                mockReservationInfo,
                'res-123',
                new Error('test error'),
            );
            expect(postMessageSpy).not.toHaveBeenCalled();
        });
    });

    describe('groupAdditionalData', () => {
        it('should return empty array for null input', () => {
            expect(groupAdditionalData(null as any)).toEqual([]);
        });

        it('should return empty array for empty array input', () => {
            expect(groupAdditionalData([])).toEqual([]);
        });

        it('should group items by serviceId', () => {
            const data = [
                {
                    serviceId: 1,
                    itemId: 'item1',
                    title: 'Service 1',
                    ticketUUID: 'ticket1',
                    ticketFieldConfiguration: ['field1'],
                    count: 1,
                    price: 10,
                },
                {
                    serviceId: 2,
                    itemId: 'item2',
                    title: 'Service 2',
                    ticketUUID: 'ticket2',
                    ticketFieldConfiguration: ['field2'],
                    count: 1,
                    price: 20,
                },
            ];
            const result = groupAdditionalData(data);
            expect(result).toHaveLength(2);
            expect(result[0].serviceId).toBe(1);
            expect(result[1].serviceId).toBe(2);
        });

        it('should increment count for items with same serviceId', () => {
            const data = [
                {
                    serviceId: 1,
                    itemId: 'item1',
                    title: 'Service 1',
                    ticketUUID: 'ticket1',
                    ticketFieldConfiguration: ['field1'],
                    count: 1,
                    price: 10,
                },
                {
                    serviceId: 1,
                    itemId: 'item2',
                    title: 'Service 1',
                    ticketUUID: 'ticket2',
                    ticketFieldConfiguration: ['field2'],
                    count: 1,
                    price: 15,
                },
            ];
            const result = groupAdditionalData(data);
            expect(result).toHaveLength(1);
            expect(result[0].count).toBe(2);
            expect(result[0].ticketFieldConfiguration).toEqual([
                'field1',
                'field2',
            ]);
        });
    });

    describe('loadPreloaded', () => {
        it('should return undefined when element not found', () => {
            document.body.innerHTML = '';
            expect(loadPreloaded('non-existent')).toBeUndefined();
        });

        it('should parse and return preloaded data', () => {
            const testData = { key: 'value' };
            document.body.innerHTML = `<script id="preload-test" type="application/json">${encodeURIComponent(JSON.stringify(testData))}</script>`;
            expect(loadPreloaded('preload-test')).toEqual(testData);
        });
    });
});
