import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { ModalRemoveSubscriptionComponent } from './modal-remove-subscription.component';

describe('ModalRemoveSubscriptionComponent', () => {
    let component: ModalRemoveSubscriptionComponent;
    let fixture: ComponentFixture<ModalRemoveSubscriptionComponent>;
    let mockActiveModal: any;

    beforeEach(async () => {
        mockActiveModal = {
            close: vi.fn(),
            dismiss: vi.fn(),
        };

        TestBed.configureTestingModule({
            declarations: [ModalRemoveSubscriptionComponent],
            imports: [NgbModule, TranslateModule.forRoot()],
            providers: [{ provide: NgbActiveModal, useValue: mockActiveModal }],
        });

        fixture = TestBed.createComponent(ModalRemoveSubscriptionComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should have activeModal injected', () => {
        expect(component.activeModal).toBeDefined();
        expect(component.activeModal).toBe(mockActiveModal);
    });

    describe('template interactions', () => {
        it('should call activeModal.close with false when no button is clicked', () => {
            fixture.detectChanges();

            const buttons = fixture.nativeElement.querySelectorAll('button');
            const noButton = buttons[0];
            noButton.click();

            expect(mockActiveModal.close).toHaveBeenCalledWith(false);
        });

        it('should call activeModal.close with true when yes button is clicked', () => {
            fixture.detectChanges();

            const buttons = fixture.nativeElement.querySelectorAll('button');
            const yesButton = buttons[1];
            yesButton.click();

            expect(mockActiveModal.close).toHaveBeenCalledWith(true);
        });

        it('should have two buttons', () => {
            fixture.detectChanges();

            const buttons = fixture.nativeElement.querySelectorAll('button');
            expect(buttons.length).toBe(2);
        });
    });
});
