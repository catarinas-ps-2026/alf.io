import { describe, it, expect, beforeEach } from 'vitest';
import { type ComponentFixture, TestBed, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { WarningModalComponent } from './warning-modal/warning-modal.component';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

describe('WarningModalComponent', () => {
    let component: WarningModalComponent;
    let fixture: ComponentFixture<WarningModalComponent>;

    const mockActiveModal = {
        dismiss: vi.fn(),
        close: vi.fn(),
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [WarningModalComponent],
            imports: [TranslateModule.forRoot()],
            providers: [
                { provide: NgbActiveModal, useValue: mockActiveModal },
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(WarningModalComponent);
        component = fixture.componentInstance;
    });

    describe('component initialization', () => {
        it('should create', () => {
            expect(component).toBeTruthy();
        });
    });

    describe('@Input properties', () => {
        it('should accept message input', () => {
            component.message = 'Warning: This action cannot be undone';
            expect(component.message).toBe('Warning: This action cannot be undone');
        });

        it('should accept parameters input', () => {
            component.parameters = { key1: 'value1', key2: 'value2' };
            expect(component.parameters).toEqual({ key1: 'value1', key2: 'value2' });
        });

        it('should accept empty parameters', () => {
            component.parameters = {};
            expect(component.parameters).toEqual({});
        });
    });

    describe('constructor', () => {
        it('should inject NgbActiveModal', () => {
            expect(component.activeModal).toBeDefined();
            expect(component.activeModal).toBe(mockActiveModal);
        });
    });
});