import { describe, it, expect, beforeEach, vi } from 'vitest';
import { type ComponentFixture, TestBed, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core/testing';
import { TranslateModule } from '@ngx-translate/core';
import { FeedbackComponent } from './feedback/feedback.component';
import { FeedbackService } from './feedback/feedback.service';
import type { FeedbackContent } from '../../model/feedback';

describe('FeedbackComponent', () => {
    let component: FeedbackComponent;
    let fixture: ComponentFixture<FeedbackComponent>;
    let mockFeedbackService: any;

    beforeEach(async () => {
        const subject = {
            subscribe: vi.fn((callback) => {
                callback({ active: false, message: '', type: 'INFO' });
            }),
        };

        mockFeedbackService = {
            displayNotification: vi.fn(() => subject),
        };

        await TestBed.configureTestingModule({
            declarations: [FeedbackComponent],
            imports: [TranslateModule.forRoot()],
            providers: [
                { provide: FeedbackService, useValue: mockFeedbackService },
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(FeedbackComponent);
        component = fixture.componentInstance;
    });

    describe('component initialization', () => {
        it('should create', () => {
            expect(component).toBeTruthy();
        });

        it('should subscribe to feedback service on init', () => {
            fixture.detectChanges();
            expect(mockFeedbackService.displayNotification).toHaveBeenCalled();
        });
    });

    describe('hide', () => {
        it('should set active to false', () => {
            component.active = true;
            component.hide();
            expect(component.active).toBe(false);
        });
    });

    describe('boxClass', () => {
        it('should return border-success text-success for SUCCESS type', () => {
            component.type = 'SUCCESS';
            expect(component.boxClass).toBe('border-success text-success');
        });

        it('should return border-danger text-danger for ERROR type', () => {
            component.type = 'ERROR';
            expect(component.boxClass).toBe('border-danger text-danger');
        });

        it('should return border-primary text-primary for INFO type', () => {
            component.type = 'INFO';
            expect(component.boxClass).toBe('border-primary text-primary');
        });
    });

    describe('headerClass', () => {
        it('should return bg-success text-white for SUCCESS type', () => {
            component.type = 'SUCCESS';
            expect(component.headerClass).toBe('bg-success text-white');
        });

        it('should return bg-danger text-white for ERROR type', () => {
            component.type = 'ERROR';
            expect(component.headerClass).toBe('bg-danger text-white');
        });

        it('should return bg-white text-primary for INFO type', () => {
            component.type = 'INFO';
            expect(component.headerClass).toBe('bg-white text-primary');
        });
    });

    describe('boxIcon', () => {
        it('should return check-circle icon for SUCCESS type', () => {
            component.type = 'SUCCESS';
            expect(component.boxIcon).toEqual(['far', 'check-circle']);
        });

        it('should return exclamation-circle icon for ERROR type', () => {
            component.type = 'ERROR';
            expect(component.boxIcon).toEqual(['fas', 'exclamation-circle']);
        });

        it('should return info-circle icon for INFO type', () => {
            component.type = 'INFO';
            expect(component.boxIcon).toEqual(['fas', 'info-circle']);
        });
    });
});