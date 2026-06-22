import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { DownloadTicketComponent } from './download-ticket.component';
import type { Ticket } from '../../model/ticket';
import type { WalletConfiguration } from '../../model/info';

describe('DownloadTicketComponent', () => {
    let component: DownloadTicketComponent;
    let fixture: ComponentFixture<DownloadTicketComponent>;
    let mockActiveModal: any;

    const mockTicket: Ticket = {
        uuid: 'ticket-123',
        firstName: 'John',
        lastName: 'Doe',
        email: 'john@example.com',
        locked: false,
        assigned: true,
        userLanguage: 'en',
        ticketFieldConfigurationBeforeStandard: [],
        ticketFieldConfigurationAfterStandard: [],
    };

    const mockWalletConfiguration: WalletConfiguration = {
        gWalletEnabled: true,
        passEnabled: true,
    };

    beforeEach(async () => {
        mockActiveModal = {
            dismiss: vi.fn(),
            close: vi.fn(),
        };

        TestBed.configureTestingModule({
            declarations: [DownloadTicketComponent],
            imports: [NgbModule, TranslateModule.forRoot()],
            providers: [{ provide: NgbActiveModal, useValue: mockActiveModal }],
        });

        fixture = TestBed.createComponent(DownloadTicketComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('@Input properties', () => {
        it('should accept ticket input', () => {
            component.ticket = mockTicket;
            expect(component.ticket).toEqual(mockTicket);
            expect(component.ticket.uuid).toBe('ticket-123');
        });

        it('should accept eventName input', () => {
            component.eventName = 'test-event';
            expect(component.eventName).toBe('test-event');
        });

        it('should accept walletConfiguration input', () => {
            component.walletConfiguration = mockWalletConfiguration;
            expect(component.walletConfiguration).toEqual(
                mockWalletConfiguration,
            );
        });
    });

    describe('gWalletEnabled', () => {
        it('should return true when walletConfiguration is set and gWalletEnabled is true', () => {
            component.walletConfiguration = {
                gWalletEnabled: true,
                passEnabled: false,
            };
            expect(component.gWalletEnabled).toBe(true);
        });

        it('should return false when walletConfiguration is null', () => {
            component.walletConfiguration = null;
            expect(component.gWalletEnabled).toBe(false);
        });

        it('should return false when gWalletEnabled is false', () => {
            component.walletConfiguration = {
                gWalletEnabled: false,
                passEnabled: true,
            };
            expect(component.gWalletEnabled).toBe(false);
        });

        it('should return false when walletConfiguration is undefined', () => {
            component.walletConfiguration = undefined;
            expect(component.gWalletEnabled).toBe(false);
        });
    });

    describe('passEnabled', () => {
        it('should return true when walletConfiguration is set and passEnabled is true', () => {
            component.walletConfiguration = {
                gWalletEnabled: false,
                passEnabled: true,
            };
            expect(component.passEnabled).toBe(true);
        });

        it('should return false when walletConfiguration is null', () => {
            component.walletConfiguration = null;
            expect(component.passEnabled).toBe(false);
        });

        it('should return false when passEnabled is false', () => {
            component.walletConfiguration = {
                gWalletEnabled: true,
                passEnabled: false,
            };
            expect(component.passEnabled).toBe(false);
        });

        it('should return false when walletConfiguration is undefined', () => {
            component.walletConfiguration = undefined;
            expect(component.passEnabled).toBe(false);
        });
    });

    describe('close', () => {
        it('should call activeModal.dismiss()', () => {
            component.close();
            expect(mockActiveModal.dismiss).toHaveBeenCalled();
        });
    });
});
