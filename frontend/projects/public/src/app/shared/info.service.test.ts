import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { InfoService } from './info.service';
import type { Info } from '../model/info';

describe('InfoService', () => {
    let service: InfoService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [InfoService],
        });

        service = TestBed.inject(InfoService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    describe('getInfo', () => {
        it('should GET info from API when no preloaded data', () => {
            const mockInfo: Info = {
                version: '2.0.0',
                buildNumber: '12345',
                walletConfiguration: { gWalletEnabled: true, passEnabled: false },
            };

            service.getInfo().subscribe((result) => {
                expect(result).toEqual(mockInfo);
            });

            const req = httpMock.expectOne('/api/v2/info');
            expect(req.request.method).toBe('GET');
            req.flush(mockInfo);
        });

        it('should cache the info request', () => {
            const mockInfo: Info = {
                version: '2.0.0',
                buildNumber: '12345',
                walletConfiguration: { gWalletEnabled: true, passEnabled: false },
            };

            service.getInfo().subscribe();
            service.getInfo().subscribe();

            const req = httpMock.expectOne('/api/v2/info');
            expect(req.request.method).toBe('GET');
            req.flush(mockInfo);
        });

        it('should handle wallet configuration', () => {
            const mockInfo: Info = {
                version: '2.0.0',
                buildNumber: '12345',
                walletConfiguration: { gWalletEnabled: false, passEnabled: true },
            };

            service.getInfo().subscribe((result) => {
                expect(result.walletConfiguration.gWalletEnabled).toBe(false);
                expect(result.walletConfiguration.passEnabled).toBe(true);
            });

            const req = httpMock.expectOne('/api/v2/info');
            req.flush(mockInfo);
        });
    });
});