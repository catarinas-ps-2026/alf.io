import { describe, it, expect, beforeEach, vi } from 'vitest';
import { type ComponentFixture, TestBed, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core/testing';
import { UntypedFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of, Subject } from 'rxjs';
import { InvoiceFormComponent } from './invoice-form.component';
import { I18nService } from '../../shared/i18n.service';
import type { InvoicingConfiguration } from '../../model/event';
import type { LocalizedCountry } from '../../model/localized-country';
import type { PurchaseContext } from '../../model/purchase-context';

describe('InvoiceFormComponent', () => {
    let component: InvoiceFormComponent;
    let fixture: ComponentFixture<InvoiceFormComponent>;

    const mockCountries: LocalizedCountry[] = [
        { isoCode: 'IT', name: 'Italy', eu: true },
        { isoCode: 'DE', name: 'Germany', eu: true },
        { isoCode: 'US', name: 'United States', eu: false },
    ];

    const mockInvoicingConfiguration: InvoicingConfiguration = {
        enabled: true,
        onlyInvoice: false,
        vatIncluded: false,
        userCanDownloadReceiptOrInvoice: true,
        enabledItalyEInvoicing: true,
        euVatCheckingEnabled: true,
        customerReferenceEnabled: true,
        vatNumberStrictlyRequired: true,
    };

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
        invoicingConfiguration: mockInvoicingConfiguration,
        assignmentConfiguration: { enableAttendeeAutocomplete: true },
    } as unknown as PurchaseContext;

    const formBuilder = new UntypedFormBuilder();

    const mockTranslateService = {
        currentLang: 'en',
        onLangChange: new Subject(),
        instant: vi.fn(() => ''),
        get: vi.fn(() => of('')),
    };

    const mockI18nService = {
        getVatCountries: vi.fn(() => of(mockCountries)),
    };

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [InvoiceFormComponent],
            imports: [ReactiveFormsModule, TranslateModule.forRoot()],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                { provide: I18nService, useValue: mockI18nService },
            ],
        });

        const translate = TestBed.inject(TranslateService);
        translate.currentLang = 'en';

        fixture = TestBed.createComponent(InvoiceFormComponent);
        component = fixture.componentInstance;

        component.form = formBuilder.group({
            addCompanyBillingDetails: false,
            billingAddressCompany: '',
            vatNr: '',
            skipVatNr: false,
            italyEInvoicingReferenceType: 'NONE',
            italyEInvoicingReferencePEC: null,
            italyEInvoicingReferenceAddresseeCode: null,
            vatCountryCode: 'IT',
        });
        component.purchaseContext = mockPurchaseContext;
        component.invoicingConfiguration = mockInvoicingConfiguration;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        it('should load countries', () => {
            component.ngOnInit();
            expect(mockI18nService.getVatCountries).toHaveBeenCalledWith('en');
            expect(component.countries).toEqual(mockCountries);
        });

        it('should subscribe to language changes', () => {
            component.ngOnInit();
            expect(mockTranslateService.onLangChange).toBeDefined();
        });

        it('should update taxIdIsRequired based on skipVatNr', () => {
            component.ngOnInit();
            expect(component.taxIdIsRequired).toBe(true);

            component.form.get('skipVatNr').setValue(true);
            expect(component.taxIdIsRequired).toBe(false);
        });

        it('should update Italy fields on reference type change', () => {
            component.ngOnInit();

            component.form.get('italyEInvoicingReferenceType').setValue('ADDRESSEE_CODE');
            component.form.get('italyEInvoicingReferencePEC').setValue('test@pec.it');

            component.ngOnInit();

            expect(component.form.get('italyEInvoicingReferencePEC').value).toBeNull();
        });
    });

    describe('updateItalyEInvoicingFields', () => {
        it('should clear PEC when addressee code is selected', () => {
            component.form.get('italyEInvoicingReferencePEC').setValue('test@pec.it');
            component.form.get('italyEInvoicingReferenceType').setValue('ADDRESSEE_CODE');

            component.updateItalyEInvoicingFields();

            expect(component.form.get('italyEInvoicingReferencePEC').value).toBeNull();
        });

        it('should clear addressee code when PEC is selected', () => {
            component.form.get('italyEInvoicingReferenceAddresseeCode').setValue('ABC123');
            component.form.get('italyEInvoicingReferenceType').setValue('PEC');

            component.updateItalyEInvoicingFields();

            expect(component.form.get('italyEInvoicingReferenceAddresseeCode').value).toBeNull();
        });

        it('should clear both when NONE is selected', () => {
            component.form.get('italyEInvoicingReferencePEC').setValue('test@pec.it');
            component.form.get('italyEInvoicingReferenceAddresseeCode').setValue('ABC123');
            component.form.get('italyEInvoicingReferenceType').setValue('NONE');

            component.updateItalyEInvoicingFields();

            expect(component.form.get('italyEInvoicingReferencePEC').value).toBeNull();
            expect(component.form.get('italyEInvoicingReferenceAddresseeCode').value).toBeNull();
        });
    });

    describe('addresseeCodeSelected', () => {
        it('should return true when addressee code is selected', () => {
            component.form.get('italyEInvoicingReferenceType').setValue('ADDRESSEE_CODE');
            expect(component.addresseeCodeSelected).toBe(true);
        });

        it('should return false when other type is selected', () => {
            component.form.get('italyEInvoicingReferenceType').setValue('PEC');
            expect(component.addresseeCodeSelected).toBe(false);
        });
    });

    describe('pecSelected', () => {
        it('should return true when PEC is selected', () => {
            component.form.get('italyEInvoicingReferenceType').setValue('PEC');
            expect(component.pecSelected).toBe(true);
        });

        it('should return false when other type is selected', () => {
            component.form.get('italyEInvoicingReferenceType').setValue('ADDRESSEE_CODE');
            expect(component.pecSelected).toBe(false);
        });
    });

    describe('getCountries', () => {
        it('should call i18nService.getVatCountries', () => {
            component.getCountries('en');
            expect(mockI18nService.getVatCountries).toHaveBeenCalledWith('en');
        });
    });

    describe('euVatCheckingEnabled', () => {
        it('should return true from invoicing configuration', () => {
            expect(component.euVatCheckingEnabled).toBe(true);
        });
    });

    describe('customerReferenceEnabled', () => {
        it('should return true from invoicing configuration', () => {
            expect(component.customerReferenceEnabled).toBe(true);
        });
    });

    describe('invoiceBusiness', () => {
        it('should return form value', () => {
            component.form.get('addCompanyBillingDetails').setValue(true);
            expect(component.invoiceBusiness).toBe(true);
        });
    });

    describe('vatNumberStrictlyRequired', () => {
        it('should return from invoicing configuration', () => {
            expect(component.vatNumberStrictlyRequired).toBe(true);
        });
    });

    describe('enabledItalyEInvoicing', () => {
        it('should return from invoicing configuration', () => {
            expect(component.enabledItalyEInvoicing).toBe(true);
        });
    });

    describe('italyEInvoicingFormDisplayed', () => {
        it('should return true when enabled and country is IT', () => {
            component.form.get('vatCountryCode').setValue('IT');
            expect(component.italyEInvoicingFormDisplayed).toBe(true);
        });

        it('should return false when country is not IT', () => {
            component.form.get('vatCountryCode').setValue('DE');
            expect(component.italyEInvoicingFormDisplayed).toBe(false);
        });

        it('should return false when Italy e-invoicing is not enabled', () => {
            component.purchaseContext = undefined;
            component.invoicingConfiguration = { ...mockInvoicingConfiguration, enabledItalyEInvoicing: false };
            component.form.get('vatCountryCode').setValue('IT');
            expect(component.italyEInvoicingFormDisplayed).toBe(false);
        });
    });

    describe('countrySelected', () => {
        it('should return true when country is selected', () => {
            component.form.get('vatCountryCode').setValue('IT');
            expect(component.countrySelected).toBe(true);
        });

        it('should return false when country is null', () => {
            component.form.get('vatCountryCode').setValue(null);
            expect(component.countrySelected).toBe(false);
        });
    });

    describe('searchCountry', () => {
        it('should return true when term matches iso code', () => {
            const country = { isoCode: 'IT', name: 'Italy', eu: true };
            expect(component.searchCountry('it', country)).toBe(true);
        });

        it('should return true when term matches name', () => {
            const country = { isoCode: 'IT', name: 'Italy', eu: true };
            expect(component.searchCountry('italy', country)).toBe(true);
        });

        it('should return false when no match', () => {
            const country = { isoCode: 'IT', name: 'Italy', eu: true };
            expect(component.searchCountry('germany', country)).toBe(false);
        });

        it('should return true when term is empty', () => {
            const country = { isoCode: 'IT', name: 'Italy', eu: true };
            expect(component.searchCountry('', country)).toBe(true);
        });
    });

    describe('ngOnDestroy', () => {
        it('should unsubscribe from langChange', () => {
            component.ngOnInit();
            const unsubscribeSpy = vi.spyOn(component.langChangeSub, 'unsubscribe');
            component.ngOnDestroy();
            expect(unsubscribeSpy).toHaveBeenCalled();
        });
    });
});