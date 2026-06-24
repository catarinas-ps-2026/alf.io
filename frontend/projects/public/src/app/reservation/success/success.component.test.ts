import { describe, it, expect, beforeEach, vi } from 'vitest';
import { type ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import { SuccessComponent } from './success.component';
import { ReservationService } from '../../shared/reservation.service';
import { EventService } from '../../shared/event.service';
import { TicketService } from '../../shared/ticket.service';
import { I18nService } from '../../shared/i18n.service';
import { AnalyticsService } from '../../shared/analytics.service';
import { InfoService } from '../../shared/info.service';
import type { Event } from '../../model/event';
import type {
    ReservationInfo,
    TicketsByTicketCategory,
} from '../../model/reservation-info';
import type { Ticket } from '../../model/ticket';
import type { WalletConfiguration } from '../../model/info';

describe('SuccessComponent', () => {
    let component: SuccessComponent;
    let fixture: ComponentFixture<SuccessComponent>;

    const mockEvent: Event = {
        id: 1,
        shortName: 'test-event',
        title: { en: 'Test Event' },
        description: {},
        format: 'IN_PERSON',
        startDate: '2024-01-01T10:00:00Z',
        endDate: '2024-01-01T18:00:00Z',
        enabled: true,
        embeddingConfiguration: { enabled: false, notificationOrigin: '' },
        invoicingConfiguration: {
            enabled: false,
            onlyInvoice: false,
            vatIncluded: false,
            userCanDownloadReceiptOrInvoice: true,
            enabledItalyEInvoicing: false,
        },
        localization: {},
        analyticsConfiguration: null,
    } as unknown as Event;

    const mockReservationInfo: ReservationInfo = {
        id: 'res-123',
        shortId: 'ABC123',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        validity: 900,
        ticketsByCategory: [
            {
                name: 'General',
                ticketAccessType: 'IN_PERSON',
                tickets: [
                    {
                        uuid: 'ticket-1',
                        firstName: 'John',
                        lastName: 'Doe',
                        email: 'john@example.com',
                        locked: false,
                        assigned: true,
                    } as Ticket,
                    {
                        uuid: 'ticket-2',
                        firstName: 'Jane',
                        lastName: 'Doe',
                        email: 'jane@example.com',
                        locked: true,
                        assigned: false,
                    } as Ticket,
                ],
            },
        ],
        orderSummary: {
            summary: [],
            totalPrice: '100.00',
            free: false,
            displayVat: true,
            priceInCents: 10000,
            descriptionForPayment: 'Test reservation',
            totalVAT: '22.00',
            vatPercentage: '22%',
        },
        status: 'COMPLETE',
        validatedBookingInformation: true,
        formattedExpirationDate: {},
        invoiceNumber: 'INV-001',
        invoiceRequested: true,
        invoiceOrReceiptDocumentPresent: true,
        paid: true,
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
            readyForConfirmation: true,
            finalized: true,
        },
    };

    const mockWalletConfiguration: WalletConfiguration = {
        gWalletEnabled: true,
        passEnabled: true,
    };

    const mockActivatedRoute = {
        params: of({ eventShortName: 'test-event', reservationId: 'res-123' }),
    };

    const mockRouter = {
        navigate: vi.fn(),
    };

    const mockReservationService = {
        getReservationInfo: vi.fn(() => of(mockReservationInfo)),
        reSendReservationEmail: vi.fn(() => of(true)),
    };

    const mockEventService = {
        getEvent: vi.fn(() => of(mockEvent)),
    };

    const mockTicketService = {
        buildFormGroupForTicket: vi.fn(() => ({})),
        sendTicketByEmail: vi.fn(() => of(true)),
        updateTicket: vi.fn(() => of({ success: true })),
        openReleaseTicket: vi.fn(() => of(true)),
        openDownloadTicket: vi.fn(() => of(undefined)),
    };

    const mockI18nService = {
        setPageTitle: vi.fn(),
        getCurrentLang: vi.fn(() => 'en'),
    };

    const mockAnalyticsService = {
        pageView: vi.fn(),
    };

    const mockTranslateService = {
        currentLang: 'en',
    };

    const mockInfoService = {
        getInfo: vi.fn(() =>
            of({ walletConfiguration: mockWalletConfiguration }),
        ),
    };

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [SuccessComponent],
            providers: [
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: Router, useValue: mockRouter },
                {
                    provide: ReservationService,
                    useValue: mockReservationService,
                },
                { provide: EventService, useValue: mockEventService },
                { provide: TicketService, useValue: mockTicketService },
                { provide: I18nService, useValue: mockI18nService },
                { provide: AnalyticsService, useValue: mockAnalyticsService },
                { provide: TranslateService, useValue: mockTranslateService },
                { provide: InfoService, useValue: mockInfoService },
            ],
        });

        fixture = TestBed.createComponent(SuccessComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        it('should load event, info and reservation', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(mockEventService.getEvent).toHaveBeenCalledWith(
                'test-event',
            );
            expect(mockInfoService.getInfo).toHaveBeenCalled();
            expect(
                mockReservationService.getReservationInfo,
            ).toHaveBeenCalledWith('res-123');
            expect(mockI18nService.setPageTitle).toHaveBeenCalledWith(
                'reservation-page-complete.header.title',
                mockEvent,
            );
        });

        it('should store wallet configuration', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(component.walletConfiguration).toEqual(
                mockWalletConfiguration,
            );
        });
    });

    describe('loadReservation', () => {
        it('should load reservation and process info', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.loadReservation();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(
                mockReservationService.getReservationInfo,
            ).toHaveBeenCalled();
            expect(component.reservationInfo).toBeDefined();
        });
    });

    describe('processReservationInfo', () => {
        it('should set reservationFinalized based on status', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            const completingReservation = {
                ...mockReservationInfo,
                status: 'FINALIZING',
            };

            component.processReservationInfo(completingReservation);

            expect(component.reservationFinalized).toBe(false);
        });

        it('should set reservationFinalized to true when complete', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            component.processReservationInfo(mockReservationInfo);

            expect(component.reservationFinalized).toBe(true);
        });

        it('should count unlocked tickets', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            component.processReservationInfo(mockReservationInfo);

            expect(component.unlockedTicketCount).toBe(1);
        });

        it('should check if all tickets are assigned', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            component.processReservationInfo(mockReservationInfo);

            expect(component.ticketsAllAssigned).toBe(false);
        });
    });

    describe('sendEmailForTicket', () => {
        it('should call ticketService.sendTicketByEmail', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.eventShortName = 'test-event';
            component.sendEmailForTicket('ticket-1');

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(mockTicketService.sendTicketByEmail).toHaveBeenCalledWith(
                'test-event',
                'ticket-1',
            );
            expect(component.sendEmailForTicketStatus['ticket-1']).toBe(true);
        });
    });

    describe('reSendReservationEmail', () => {
        it('should call reservationService.reSendReservationEmail', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.eventShortName = 'test-event';
            component.reservationId = 'res-123';

            component.reSendReservationEmail();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(
                mockReservationService.reSendReservationEmail,
            ).toHaveBeenCalledWith('event', 'test-event', 'res-123', 'en');
            expect(component.reservationMailSent).toBe(true);
        });
    });

    describe('updateTicket', () => {
        it('should update ticket and reload reservation', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            component.ticketsFormControl = {
                'ticket-1': { value: { firstName: 'John' } } as any,
            };
            component.ticketsFormShow = { 'ticket-1': true };

            component.updateTicket('ticket-1');

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(mockTicketService.updateTicket).toHaveBeenCalled();
        });
    });

    describe('releaseTicket', () => {
        it('should navigate to event page when single ticket', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            component.reservationInfo.ticketsByCategory = [
                {
                    name: 'General',
                    ticketAccessType: 'IN_PERSON',
                    tickets: [{ uuid: 'ticket-1' } as Ticket],
                },
            ];

            const mockTicket = { uuid: 'ticket-1' } as Ticket;
            component.releaseTicket(mockTicket);

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(mockRouter.navigate).toHaveBeenCalledWith(
                ['event', 'test-event'],
                { replaceUrl: true },
            );
        });

        it('should reload reservation when multiple tickets', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            component.reservationInfo.ticketsByCategory = [
                {
                    name: 'General',
                    ticketAccessType: 'IN_PERSON',
                    tickets: [
                        { uuid: 'ticket-1' } as Ticket,
                        { uuid: 'ticket-2' } as Ticket,
                    ],
                },
            ];
            mockReservationService.getReservationInfo.mockReturnValue(
                of(mockReservationInfo),
            );

            const mockTicket = { uuid: 'ticket-1' } as Ticket;
            component.releaseTicket(mockTicket);

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(
                mockReservationService.getReservationInfo,
            ).toHaveBeenCalled();
        });
    });

    describe('ticketFormVisible', () => {
        it('should return true when ticketsFormShow has entries', () => {
            component.ticketsFormShow = { 'ticket-1': true };
            expect(component.ticketFormVisible).toBe(true);
        });

        it('should return false when ticketsFormShow is empty', () => {
            component.ticketsFormShow = {};
            expect(component.ticketFormVisible).toBe(false);
        });
    });

    describe('hideTicketForm', () => {
        it('should remove ticket from ticketsFormShow', () => {
            component.ticketsFormShow = { 'ticket-1': true, 'ticket-2': true };
            component.hideTicketForm('ticket-1');
            expect(component.ticketsFormShow['ticket-1']).toBeUndefined();
            expect(component.ticketsFormShow['ticket-2']).toBe(true);
        });
    });

    describe('downloadBillingDocumentVisible', () => {
        it('should return true when conditions are met', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            component.reservationInfo = mockReservationInfo;

            expect(component.downloadBillingDocumentVisible).toBe(true);
        });

        it('should return false when not paid', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            component.reservationInfo = { ...mockReservationInfo, paid: false };

            expect(component.downloadBillingDocumentVisible).toBe(false);
        });
    });

    describe('isOnlineTicket', () => {
        it('should return true for ONLINE format', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = { ...mockEvent, format: 'ONLINE' };
            const category = {
                ticketAccessType: 'IN_PERSON',
            } as TicketsByTicketCategory;

            expect(component.isOnlineTicket(category)).toBe(true);
        });

        it('should return true for HYBRID with ONLINE category', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = { ...mockEvent, format: 'HYBRID' };
            const category = {
                ticketAccessType: 'ONLINE',
            } as TicketsByTicketCategory;

            expect(component.isOnlineTicket(category)).toBe(true);
        });

        it('should return false for HYBRID with IN_PERSON category', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = { ...mockEvent, format: 'HYBRID' };
            const category = {
                ticketAccessType: 'IN_PERSON',
            } as TicketsByTicketCategory;

            expect(component.isOnlineTicket(category)).toBe(false);
        });
    });

    describe('purchaseContextTitle', () => {
        it('should return event title in current language', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            expect(component.purchaseContextTitle).toBe('Test Event');
        });
    });

    describe('walletIntegrationEnabled', () => {
        it('should return true when wallet config exists and enabled', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.walletConfiguration = mockWalletConfiguration;
            expect(component.walletIntegrationEnabled).toBe(true);
        });

        it('should return false when wallet config is null', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.walletConfiguration = null;
            expect(component.walletIntegrationEnabled).toBe(false);
        });
    });

    describe('downloadTicket', () => {
        it('should call ticketService.openDownloadTicket', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.eventShortName = 'test-event';
            component.walletConfiguration = mockWalletConfiguration;

            const mockTicket = { uuid: 'ticket-1' } as Ticket;
            component.downloadTicket(mockTicket);

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(mockTicketService.openDownloadTicket).toHaveBeenCalledWith(
                mockTicket,
                'test-event',
                mockWalletConfiguration,
            );
        });
    });

    describe('showReservationButtons', () => {
        it('should return true when finalized and not embedded', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            component.reservationInfo = mockReservationInfo;

            expect(component.showReservationButtons).toBe(true);
        });

        it('should return false when not finalized', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            const notFinalizedReservation = {
                ...mockReservationInfo,
                status: 'FINALIZING',
            };
            component.processReservationInfo(notFinalizedReservation);

            expect(component.showReservationButtons).toBe(false);
        });

        it('should return false when hideConfirmationButtons is true', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            component.event = mockEvent;
            component.reservationInfo = {
                ...mockReservationInfo,
                metadata: {
                    ...mockReservationInfo.metadata,
                    hideConfirmationButtons: true,
                },
            };

            expect(component.showReservationButtons).toBe(false);
        });
    });

    describe('getAdditionalData', () => {
        it('should return additional data for ticket', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            const mockTicket = { uuid: 'ticket-1' } as Ticket;
            component.additionalServicesWithData = {
                'ticket-1': [
                    {
                        title: { en: 'Supplement' },
                        itemId: 1,
                        serviceId: 1,
                        ticketUUID: 'ticket-1',
                        ticketFieldConfiguration: [],
                        type: 'SUPPLEMENT' as const,
                    },
                ],
            };

            const result = component.getAdditionalData(mockTicket);
            expect(result).toHaveLength(1);
        });

        it('should return empty array when no additional data', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            const mockTicket = { uuid: 'ticket-2' } as Ticket;
            component.additionalServicesWithData = {};

            const result = component.getAdditionalData(mockTicket);
            expect(result).toEqual([]);
        });
    });

    describe('hasAdditionalData', () => {
        it('should return true when ticket has additional data', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            const mockTicket = { uuid: 'ticket-1' } as Ticket;
            component.additionalServicesWithData = {
                'ticket-1': [
                    {
                        title: { en: 'Test' },
                        itemId: 1,
                        serviceId: 1,
                        ticketUUID: 'ticket-1',
                        ticketFieldConfiguration: [],
                        type: 'SUPPLEMENT' as const,
                    },
                ],
            };

            expect(component.hasAdditionalData(mockTicket)).toBe(true);
        });

        it('should return false when no additional data', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            const mockTicket = { uuid: 'ticket-1' } as Ticket;
            component.additionalServicesWithData = {};

            expect(component.hasAdditionalData(mockTicket)).toBe(false);
        });
    });
});
