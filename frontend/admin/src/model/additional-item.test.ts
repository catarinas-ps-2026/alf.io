import { describe, expect, it } from 'vitest';
import type { SupplementPolicy } from './additional-item.ts';
import { isMandatory, isMandatoryPercentage } from './additional-item.ts';

describe('isMandatory', () => {
    const mandatoryPolicies: SupplementPolicy[] = [
        'MANDATORY_ONE_FOR_TICKET',
        'MANDATORY_PERCENTAGE_FOR_TICKET',
        'MANDATORY_PERCENTAGE_RESERVATION',
    ];

    const optionalPolicies: SupplementPolicy[] = [
        'OPTIONAL_UNLIMITED_AMOUNT',
        'OPTIONAL_MAX_AMOUNT_PER_TICKET',
        'OPTIONAL_MAX_AMOUNT_PER_RESERVATION',
    ];

    it.each(mandatoryPolicies)('returns true for %s', (policy) => {
        expect(isMandatory(policy)).toBe(true);
    });

    it.each(optionalPolicies)('returns false for %s', (policy) => {
        expect(isMandatory(policy)).toBe(false);
    });
});

describe('isMandatoryPercentage', () => {
    const percentagePolicies: SupplementPolicy[] = [
        'MANDATORY_PERCENTAGE_FOR_TICKET',
        'MANDATORY_PERCENTAGE_RESERVATION',
    ];

    const nonPercentagePolicies: SupplementPolicy[] = [
        'MANDATORY_ONE_FOR_TICKET',
        'OPTIONAL_UNLIMITED_AMOUNT',
        'OPTIONAL_MAX_AMOUNT_PER_TICKET',
        'OPTIONAL_MAX_AMOUNT_PER_RESERVATION',
    ];

    it.each(percentagePolicies)('returns true for %s', (policy) => {
        expect(isMandatoryPercentage(policy)).toBe(true);
    });

    it.each(nonPercentagePolicies)('returns false for %s', (policy) => {
        expect(isMandatoryPercentage(policy)).toBe(false);
    });
});
