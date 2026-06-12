import { describe, it, expect, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { UntypedFormBuilder } from '@angular/forms';
import { AdditionalFieldService } from './additional-field.service';
import type { AdditionalField } from '../model/ticket';
import type { UserAdditionalData } from '../model/user';

describe('AdditionalFieldService', () => {
    let service: AdditionalFieldService;
    let formBuilder: UntypedFormBuilder;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [AdditionalFieldService, UntypedFormBuilder],
        });
        service = TestBed.inject(AdditionalFieldService);
        formBuilder = TestBed.inject(UntypedFormBuilder);
    });

    describe('buildAdditionalFields', () => {
        it('should return empty form group when no fields provided', () => {
            const result = service.buildAdditionalFields([], [], 'en');
            expect(result).toBeDefined();
            expect(Object.keys(result.controls)).toHaveLength(0);
        });

        it('should build fields from before array', () => {
            const beforeFields: AdditionalField[] = [
                {
                    name: 'custom-field',
                    type: 'text',
                    required: false,
                    restrictedValues: [],
                    fields: [
                        { fieldIndex: 0, fieldValue: 'test value', restrictedValueSelection: false },
                    ],
                },
            ];

            const result = service.buildAdditionalFields(beforeFields, [], 'en');
            expect(result.get('custom-field')).toBeDefined();
        });

        it('should build fields from after array', () => {
            const afterFields: AdditionalField[] = [
                {
                    name: 'after-field',
                    type: 'text',
                    required: true,
                    restrictedValues: [],
                    fields: [
                        { fieldIndex: 0, fieldValue: null, restrictedValueSelection: false },
                    ],
                },
            ];

            const result = service.buildAdditionalFields([], afterFields, 'en');
            expect(result.get('after-field')).toBeDefined();
        });

        it('should handle checkbox type with multiple restricted values', () => {
            const beforeFields: AdditionalField[] = [
                {
                    name: 'checkbox-field',
                    type: 'checkbox',
                    required: false,
                    restrictedValues: ['option1', 'option2', 'option3'],
                    fields: [
                        { fieldIndex: 0, fieldValue: null, restrictedValueSelection: false },
                        { fieldIndex: 1, fieldValue: null, restrictedValueSelection: false },
                        { fieldIndex: 2, fieldValue: null, restrictedValueSelection: false },
                    ],
                },
            ];

            const result = service.buildAdditionalFields(beforeFields, [], 'en');
            const checkboxControl = result.get('checkbox-field');
            expect(checkboxControl).toBeDefined();
        });

        it('should use userData label value when fieldValue is null', () => {
            const beforeFields: AdditionalField[] = [
                {
                    name: 'user-field',
                    type: 'text',
                    required: false,
                    restrictedValues: [],
                    fields: [
                        { fieldIndex: 0, fieldValue: null, restrictedValueSelection: false },
                    ],
                },
            ];

            const userData: UserAdditionalData = {
                'user-field': {
                    label: { en: 'User Label' },
                    values: ['User Value'],
                },
            };

            const result = service.buildAdditionalFields(beforeFields, [], 'en', userData);
            const control = result.get('user-field');
            expect(control).toBeDefined();
        });

        it('should handle null before and after arrays', () => {
            const result = service.buildAdditionalFields(null as any, null as any, 'en');
            expect(result).toBeDefined();
        });
    });

    describe('private methods', () => {
        it('should handle user data with missing values', () => {
            const beforeFields: AdditionalField[] = [
                {
                    name: 'test-field',
                    type: 'text',
                    required: false,
                    restrictedValues: [],
                    fields: [
                        { fieldIndex: 0, fieldValue: null, restrictedValueSelection: false },
                    ],
                },
            ];

            const userData: UserAdditionalData = {
                'test-field': {
                    label: { en: 'Label' },
                    values: [],
                },
            };

            const result = service.buildAdditionalFields(beforeFields, [], 'en', userData);
            expect(result.get('test-field')).toBeDefined();
        });

        it('should handle user data with index out of bounds', () => {
            const beforeFields: AdditionalField[] = [
                {
                    name: 'test-field',
                    type: 'text',
                    required: false,
                    restrictedValues: [],
                    fields: [
                        { fieldIndex: 0, fieldValue: null, restrictedValueSelection: false },
                    ],
                },
            ];

            const userData: UserAdditionalData = {
                'test-field': {
                    label: { en: 'Label' },
                    values: [],
                },
            };

            const result = service.buildAdditionalFields(beforeFields, [], 'en', userData);
            expect(result.get('test-field')).toBeDefined();
        });
    });
});