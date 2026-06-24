import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
    type ComponentFixture,
    TestBed,
    CUSTOM_ELEMENTS_SCHEMA,
} from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of } from 'rxjs';
import { ErrorComponent } from './error.component';
import { PurchaseContextService } from '../../shared/purchase-context.service';
import { I18nService } from '../../shared/i18n.service';
import type { PurchaseContext } from '../../model/purchase-context';

describe('ErrorComponent', () => {
    let component: ErrorComponent;
    let fixture: ComponentFixture<ErrorComponent>;

    const mockPurchaseContext: PurchaseContext = {
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
        embeddingConfiguration: { enabled: false, notificationOrigin: '' },
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

    const mockActivatedRoute = {
        data: of({
            type: 'event',
            publicIdentifierParameter: 'eventShortName',
        }),
        params: of({ eventShortName: 'test-event', reservationId: 'res-123' }),
    };

    const mockPurchaseContextService = {
        getContext: vi.fn(() => of(mockPurchaseContext)),
    };

    const mockI18nService = {
        setPageTitle: vi.fn(),
    };

    let translateService: TranslateService;

    beforeEach(async () => {
        translateService = TestBed.configureTestingModule({
            declarations: [ErrorComponent],
            imports: [TranslateModule.forRoot()],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                {
                    provide: PurchaseContextService,
                    useValue: mockPurchaseContextService,
                },
                { provide: I18nService, useValue: mockI18nService },
            ],
        }).inject(TranslateService);

        fixture = TestBed.createComponent(ErrorComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        it('should load purchase context', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(mockPurchaseContextService.getContext).toHaveBeenCalledWith(
                'event',
                'test-event',
            );
        });

        it('should set page title', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(mockI18nService.setPageTitle).toHaveBeenCalledWith(
                'reservation-page-not-found.header.title',
                mockPurchaseContext,
            );
        });

        it('should store reservationId from params', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(component.reservationId).toBe('res-123');
        });

        it('should store purchaseContext', async () => {
            component.ngOnInit();

            await new Promise((resolve) => setTimeout(resolve, 100));
            expect(component.purchaseContext).toEqual(mockPurchaseContext);
        });
    });
});
