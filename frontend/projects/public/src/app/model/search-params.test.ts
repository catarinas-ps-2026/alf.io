import { describe, it, expect } from 'vitest';
import { SearchParams } from './search-params';

describe('SearchParams', () => {
    describe('constructor', () => {
        it('should create instance with all parameters', () => {
            const params = new SearchParams('sub-123', 'org-456', 'my-org', [
                'tag1',
                'tag2',
            ]);
            expect(params).toBeDefined();
        });
    });

    describe('fromQueryAndPathParams', () => {
        it('should create SearchParams from query and path params', () => {
            const queryParams = {
                subscription: 'sub-123',
                organizer: 'org-456',
                tags: ['tag1', 'tag2'],
            };
            const pathParams = { organizerSlug: 'my-slug' };

            const searchParams = SearchParams.fromQueryAndPathParams(
                queryParams,
                pathParams,
            );

            expect(searchParams).toBeDefined();
        });

        it('should handle null/undefined values', () => {
            const queryParams = {};
            const pathParams = {};

            const searchParams = SearchParams.fromQueryAndPathParams(
                queryParams,
                pathParams,
            );

            expect(searchParams).toBeDefined();
        });

        it('should handle partial params', () => {
            const queryParams = { subscription: 'sub-123' };
            const pathParams = { organizerSlug: 'my-slug' };

            const searchParams = SearchParams.fromQueryAndPathParams(
                queryParams,
                pathParams,
            );

            expect(searchParams).toBeDefined();
        });
    });

    describe('toHttpParams', () => {
        it('should return HttpParams from object', () => {
            const params = new SearchParams('sub-123', 'org-456', 'my-org', [
                'tag1',
            ]);
            const httpParams = params.toHttpParams();

            expect(httpParams).toBeDefined();
            expect(httpParams.toString()).toContain('subscription=sub-123');
        });

        it('should handle empty values', () => {
            const params = new SearchParams(null, null, '', []);
            const httpParams = params.toHttpParams();

            expect(httpParams).toBeDefined();
        });

        it('should include organizerSlug when present', () => {
            const params = new SearchParams(null, null, 'my-slug', null);
            const httpParams = params.toHttpParams();

            expect(httpParams.toString()).toContain('organizerSlug=my-slug');
        });
    });

    describe('toParams', () => {
        it('should return plain params object', () => {
            const params = new SearchParams('sub-123', 'org-456', 'my-org', [
                'tag1',
            ]);
            const result = params.toParams();

            expect(result).toBeDefined();
            expect(result.subscription).toBe('sub-123');
            expect(result.organizer).toBe('org-456');
            expect(result.organizerSlug).toBe('my-org');
            expect(result.tags).toEqual(['tag1']);
        });

        it('should handle null values', () => {
            const params = new SearchParams(null, null, '', null);
            const result = params.toParams();

            expect(result).toBeDefined();
            expect(result.subscription).toBeUndefined();
        });
    });

    describe('transformParams', () => {
        it('should transform query and path params to result params', () => {
            const queryParams = {
                subscription: 'sub-123',
                organizer: 'org-456',
                tags: ['tag1', 'tag2'],
            };
            const pathParams = { organizerSlug: 'my-slug' };

            const result = SearchParams.transformParams(
                queryParams,
                pathParams,
            );

            expect(result).toBeDefined();
            expect(result.subscription).toBe('sub-123');
            expect(result.organizer).toBe('org-456');
            expect(result.organizerSlug).toBe('my-slug');
        });

        it('should return empty object when no params provided', () => {
            const result = SearchParams.transformParams({}, {});

            expect(result).toBeDefined();
        });
    });
});
