import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { ReservationExpiredComponent } from './reservation-expired.component';

describe('ReservationExpiredComponent', () => {
    let component: ReservationExpiredComponent;
    let fixture: ComponentFixture<ReservationExpiredComponent>;
    let mockActiveModal: any;

    beforeEach(async () => {
        mockActiveModal = {
            close: vi.fn(),
            dismiss: vi.fn(),
        };

        TestBed.configureTestingModule({
            declarations: [ReservationExpiredComponent],
            imports: [NgbModule, TranslateModule.forRoot()],
            providers: [
                { provide: NgbActiveModal, useValue: mockActiveModal },
            ],
        });

        fixture = TestBed.createComponent(ReservationExpiredComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should have activeModal injected', () => {
        expect(component.activeModal).toBeDefined();
        expect(component.activeModal).toBe(mockActiveModal);
    });

    describe('@Input name', () => {
        it('should accept name input', () => {
            component.name = 'Test Event';
            expect(component.name).toBe('Test Event');
        });

        it('should accept empty name', () => {
            component.name = '';
            expect(component.name).toBe('');
        });
    });

    describe('template interactions', () => {
        it('should call activeModal.close when button is clicked', () => {
            fixture.detectChanges();

            const button = fixture.nativeElement.querySelector('button');
            button.click();

            expect(mockActiveModal.close).toHaveBeenCalledWith('to-event-site');
        });
    });
});