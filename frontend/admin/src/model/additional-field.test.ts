import { describe, expect, it } from 'vitest';
import type { AdditionalFieldType } from './additional-field.ts';
import {
    renderAdditionalFieldType,
    supportsMinMaxLength,
    supportsPlaceholder,
    supportsRestrictedValues,
} from './additional-field.ts';

describe('supportsPlaceholder', () => {
    // Técnicas: Particiones de equivalencia
    // - PE1: Tipos con placeholder (input:text, input:tel, vat:eu, textarea, input:dateOfBirth) → true
    // - PE2: Tipos sin placeholder (country, select, checkbox, radio) → false
    const typesWithPlaceholder: AdditionalFieldType[] = [
        'input:text',
        'input:tel',
        'vat:eu',
        'textarea',
        'input:dateOfBirth',
    ];

    const typesWithoutPlaceholder: AdditionalFieldType[] = [
        'country',
        'select',
        'checkbox',
        'radio',
    ];

    it.each(typesWithPlaceholder)('returns true for %s', (type) => {
        expect(supportsPlaceholder(type)).toBe(true);
    });

    it.each(typesWithoutPlaceholder)('returns false for %s', (type) => {
        expect(supportsPlaceholder(type)).toBe(false);
    });
});

describe('supportsRestrictedValues', () => {
    // Técnicas: Particiones de equivalencia
    // - PE1: Tipos con valores restringidos (checkbox, radio, select) → true
    // - PE2: Tipos sin valores restringidos → false
    const typesWithRestrictedValues: AdditionalFieldType[] = [
        'checkbox',
        'radio',
        'select',
    ];

    const typesWithoutRestrictedValues: AdditionalFieldType[] = [
        'input:text',
        'input:tel',
        'vat:eu',
        'textarea',
        'country',
        'input:dateOfBirth',
    ];

    it.each(typesWithRestrictedValues)('returns true for %s', (type) => {
        expect(supportsRestrictedValues(type)).toBe(true);
    });

    it.each(typesWithoutRestrictedValues)('returns false for %s', (type) => {
        expect(supportsRestrictedValues(type)).toBe(false);
    });
});

describe('renderAdditionalFieldType', () => {
    // Técnicas: Particiones de equivalencia + Valores límite
    // - PE1: Tipos válidos en diccionario → descripción correspondiente
    // - PE2: Tipo inválido → 'unknown'
    // - VL: Primer tipo del diccionario, último tipo, tipo inexistente
    const typeDescriptions: Array<[AdditionalFieldType, string]> = [
        ['input:text', 'Single-line text input'],
        ['input:tel', 'Phone number input'],
        ['vat:eu', 'European VAT number input'],
        ['textarea', 'Multi-line text input'],
        ['country', 'Country selection drop-down'],
        ['select', 'Single-choice drop-down'],
        ['radio', 'Single-choice radio buttons'],
        ['checkbox', 'Multiple-choice checkboxes'],
        ['input:dateOfBirth', 'Date of birth input'],
    ];

    it.each(
        typeDescriptions,
    )('returns correct description for %s', (type, expected) => {
        expect(renderAdditionalFieldType(type)).toBe(expected);
    });

    it('returns "unknown" for invalid type', () => {
        expect(
            renderAdditionalFieldType('invalid:type' as AdditionalFieldType),
        ).toBe('unknown');
    });
});

describe('supportsMinMaxLength', () => {
    // Técnicas: Particiones de equivalencia
    // - PE1: Tipos con min/max (input:text, input:tel, textarea, input:dateOfBirth) → true
    // - PE2: Tipos sin min/max → false
    const typesWithMinMaxLength: AdditionalFieldType[] = [
        'input:text',
        'input:tel',
        'textarea',
        'input:dateOfBirth',
    ];

    const typesWithoutMinMaxLength: AdditionalFieldType[] = [
        'vat:eu',
        'country',
        'select',
        'checkbox',
        'radio',
    ];

    it.each(typesWithMinMaxLength)('returns true for %s', (type) => {
        expect(supportsMinMaxLength(type)).toBe(true);
    });

    it.each(typesWithoutMinMaxLength)('returns false for %s', (type) => {
        expect(supportsMinMaxLength(type)).toBe(false);
    });
});
