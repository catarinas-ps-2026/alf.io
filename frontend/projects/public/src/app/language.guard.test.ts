import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { type ActivatedRouteSnapshot, type RouterStateSnapshot } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LanguageGuard } from './language.guard';
import { I18nService } from './shared/i18n.service';
import { PurchaseContextService } from './shared/purchase-context.service';
import { TranslateService } from '@ngx-translate/core';
import type { PurchaseContext } from './model/purchase-context';

describe('LanguageGuard', () => {
    let guard: LanguageGuard;
    let mockI18nService: any;
    let mockPurchaseContextService: any;
    let mockTranslateService: any;

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
        invoicingConfiguration: { enabled: false, onlyInvoice: false, vatIncluded: false, userCanDownloadReceiptOrInvoice: true, enabledItalyEInvoicing: false },
        assignmentConfiguration: { enableAttendeeAutocomplete: true },
        contentLanguages: [{ locale: 'en', name: 'English' }, { locale: 'it', name: 'Italiano' }],
        currency: 'USD',
    } as unknown as PurchaseContext;

    const mockRoute = (queryParams?: any, data?: any, params?: any): Partial<ActivatedRouteSnapshot> => ({
        queryParams: queryParams || {},
        data: data || {},
        params: params || {},
    });

    const mockState: Partial<RouterStateSnapshot> = {
        url: '/event/test-event',
    };

    beforeEach(async () => {
        mockI18nService = {
            getPersistedLanguage: vi.fn(),
            useTranslation: vi.fn(() => of(true)),
            getAvailableLanguages: vi.fn(() => of([{ locale: 'en', name: 'English' }, { locale: 'it', name: 'Italiano' }])),
        };

        mockPurchaseContextService = {
            getContext: vi.fn(() => of(mockPurchaseContext)),
        };

        mockTranslateService = {
            getBrowserLang: vi.fn(() => 'en'),
        };

        await TestBed.configureTestingModule({
            providers: [
                LanguageGuard,
                { provide: I18nService, useValue: mockI18nService },
                { provide: PurchaseContextService, useValue: mockPurchaseContextService },
                { provide: TranslateService, useValue: mockTranslateService },
            ],
        }).compileComponents();

        guard = TestBed.inject(LanguageGuard);
    });

    describe('extractLang logic', () => {
        it('should use queryParam lang when available and supported', () => {
            mockI18nService.getPersistedLanguage.mockReturnValue('en');

            const route = mockRoute({ lang: 'it' }, { type: 'event', publicIdentifierParameter: 'eventShortName' }, { eventShortName: 'test-event' });
            mockPurchaseContextService.getContext.mockReturnValue(of(mockPurchaseContext));

            guard.canActivate(route as ActivatedRouteSnapshot, mockState as RouterStateSnapshot).subscribe();

            expect(mockI18nService.useTranslation).toHaveBeenCalledWith('event', 'test-event', 'it');
        });

        it('should use persisted lang when queryParam not available', () => {
            mockI18nService.getPersistedLanguage.mockReturnValue('it');
            mockI18nService.useTranslation.mockReturnValue(of(true));

            const route = mockRoute({}, { type: 'event', publicIdentifierParameter: 'eventShortName' }, { eventShortName: 'test-event' });
            mockPurchaseContextService.getContext.mockReturnValue(of(mockPurchaseContext));

            guard.canActivate(route as ActivatedRouteSnapshot, mockState as RouterStateSnapshot).subscribe();

            expect(mockI18nService.useTranslation).toHaveBeenCalledWith('event', 'test-event', 'it');
        });

        it('should use browser lang when no queryParam or persisted', () => {
            mockI18nService.getPersistedLanguage.mockReturnValue(null);
            mockI18nService.useTranslation.mockReturnValue(of(true));

            const route = mockRoute({}, { type: 'event', publicIdentifierParameter: 'eventShortName' }, { eventShortName: 'test-event' });
            mockPurchaseContextService.getContext.mockReturnValue(of(mockPurchaseContext));

            guard.canActivate(route as ActivatedRouteSnapshot, mockState as RouterStateSnapshot).subscribe();

            expect(mockTranslateService.getBrowserLang).toHaveBeenCalled();
        });

        it('should fallback to en when browser lang not available', () => {
            mockI18nService.getPersistedLanguage.mockReturnValue(null);
            mockTranslateService.getBrowserLang.mockReturnValue('fr');
            mockI18nService.useTranslation.mockReturnValue(of(true));

            const route = mockRoute({}, { type: 'event', publicIdentifierParameter: 'eventShortName' }, { eventShortName: 'test-event' });
            mockPurchaseContextService.getContext.mockReturnValue(of(mockPurchaseContext));

            guard.canActivate(route as ActivatedRouteSnapshot, mockState as RouterStateSnapshot).subscribe();

            expect(mockI18nService.useTranslation).toHaveBeenCalledWith('event', 'test-event', 'en');
        });

        it('should use first available language as last resort', () => {
            mockI18nService.getPersistedLanguage.mockReturnValue(null);
            mockTranslateService.getBrowserLang.mockReturnValue('de');
            mockI18nService.useTranslation.mockReturnValue(of(true));

            const route = mockRoute({}, { type: 'event', publicIdentifierParameter: 'eventShortName' }, { eventShortName: 'test-event' });
            mockPurchaseContextService.getContext.mockReturnValue(of(mockPurchaseContext));

            guard.canActivate(route as ActivatedRouteSnapshot, mockState as RouterStateSnapshot).subscribe();

            expect(mockI18nService.useTranslation).toHaveBeenCalledWith('event', 'test-event', 'en');
        });
    });

    describe('getForContext vs getForApp', () => {
        it('should call getForContext when type and publicIdentifier are provided', () => {
            mockI18nService.getPersistedLanguage.mockReturnValue(null);
            mockI18nService.useTranslation.mockReturnValue(of(true));

            const route = mockRoute({}, { type: 'event', publicIdentifierParameter: 'eventShortName' }, { eventShortName: 'test-event' });

            guard.canActivate(route as ActivatedRouteSnapshot, mockState as RouterStateSnapshot).subscribe();

            expect(mockPurchaseContextService.getContext).toHaveBeenCalledWith('event', 'test-event');
        });

        it('should call getForApp when type is null', () => {
            mockI18nService.getPersistedLanguage.mockReturnValue(null);
            mockI18nService.useTranslation.mockReturnValue(of(true));
            mockI18nService.getAvailableLanguages.mockReturnValue(of([{ locale: 'en', name: 'English' }]));

            const route = mockRoute({}, { type: null, publicIdentifierParameter: 'eventShortName' }, { eventShortName: 'test-event' });

            guard.canActivate(route as ActivatedRouteSnapshot, mockState as RouterStateSnapshot).subscribe();

            expect(mockI18nService.getAvailableLanguages).toHaveBeenCalled();
            expect(mockPurchaseContextService.getContext).not.toHaveBeenCalled();
        });

        it('should fallback to getForApp on error in getForContext', () => {
            mockI18nService.getPersistedLanguage.mockReturnValue(null);
            mockI18nService.useTranslation.mockReturnValue(of(true));
            mockI18nService.getAvailableLanguages.mockReturnValue(of([{ locale: 'en', name: 'English' }]));

            const route = mockRoute({}, { type: 'event', publicIdentifierParameter: 'eventShortName' }, { eventShortName: 'test-event' });
            mockPurchaseContextService.getContext.mockReturnValue(throwError(() => new Error('Not found')));

            guard.canActivate(route as ActivatedRouteSnapshot, mockState as RouterStateSnapshot).subscribe();

            expect(mockI18nService.getAvailableLanguages).toHaveBeenCalled();
        });
    });

    describe('canActivate return value', () => {
        it('should return true when useTranslation completes successfully', () => {
            mockI18nService.getPersistedLanguage.mockReturnValue('en');
            mockI18nService.useTranslation.mockReturnValue(of(true));

            const route = mockRoute({}, { type: 'event', publicIdentifierParameter: 'eventShortName' }, { eventShortName: 'test-event' });
            mockPurchaseContextService.getContext.mockReturnValue(of(mockPurchaseContext));

            let result: boolean = false;
            guard.canActivate(route as ActivatedRouteSnapshot, mockState as RouterStateSnapshot)
                .subscribe((res) => (result = res));

            expect(result).toBe(true);
        });
    });
});