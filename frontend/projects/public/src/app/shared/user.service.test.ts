import {
    HttpClientTestingModule,
    HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { UserService } from './user.service';
import { ANONYMOUS } from '../model/user';
import type { User } from '../model/user';

describe('UserService', () => {
    let service: UserService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [UserService],
        });
        service = TestBed.inject(UserService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    describe('initAuthenticationStatus', () => {
        it('should resolve true on successful load', async () => {
            const mockUser: User = {
                firstName: 'John',
                lastName: 'Doe',
                emailAddress: 'john@example.com',
                profile: {
                    fullName: 'John Doe',
                    additionalData: {},
                },
            };

            const promise = service.initAuthenticationStatus();

            const authReq = httpMock.expectOne(
                '/api/v2/public/user/authentication-enabled',
            );
            authReq.flush(true);

            const userReq = httpMock.expectOne('/api/v2/public/user/me');
            userReq.flush(mockUser);

            const resolved = await promise;
            expect(resolved).toBe(true);
        });

        it('should resolve true even on error', async () => {
            const promise = service.initAuthenticationStatus();

            const authReq = httpMock.expectOne(
                '/api/v2/public/user/authentication-enabled',
            );
            authReq.error(new ErrorEvent('Network error'));

            const resolved = await promise;
            expect(resolved).toBe(true);
        });

        it('should set auth status to disabled when not enabled', async () => {
            const promise = service.initAuthenticationStatus();

            const authReq = httpMock.expectOne(
                '/api/v2/public/user/authentication-enabled',
            );
            authReq.flush(false);

            await promise;

            let status: any;
            service.authenticationStatus.subscribe((s) => (status = s));
            expect(status.enabled).toBe(false);
        });
    });

    describe('updateAuthenticationStatus', () => {
        it('should update auth status with user when enabled', async () => {
            const mockUser: User = {
                firstName: 'John',
                lastName: 'Doe',
                emailAddress: 'john@example.com',
                profile: {
                    fullName: 'John Doe',
                    additionalData: {},
                },
            };

            service.updateAuthenticationStatus(true);
            (service as any).latestValue = mockUser;
            service.updateAuthenticationStatus(true);

            let status: any;
            service.authenticationStatus.subscribe((s) => (status = s));
            expect(status.enabled).toBe(true);
            expect(status.user).toEqual(mockUser);
        });

        it('should clear user when disabled', async () => {
            service.updateAuthenticationStatus(false);

            let status: any;
            service.authenticationStatus.subscribe((s) => (status = s));
            expect(status.enabled).toBe(false);
            expect(status.user).toBeUndefined();
        });
    });

    describe('getUserIdentity', () => {
        it('should return user from response body', async () => {
            const mockUser: User = {
                firstName: 'John',
                lastName: 'Doe',
                emailAddress: 'john@example.com',
                profile: {
                    fullName: 'John Doe',
                    additionalData: {},
                },
            };

            const promise = new Promise<User>((resolve) => {
                service
                    .getUserIdentity()
                    .subscribe({ next: (user) => resolve(user) });
            });

            const req = httpMock.expectOne('/api/v2/public/user/me');
            req.flush(mockUser, { status: 200, statusText: 'OK' });

            const user = await promise;
            expect(user).toEqual(mockUser);
        });

        it('should return ANONYMOUS on 204 status', async () => {
            const promise = new Promise<User>((resolve) => {
                service
                    .getUserIdentity()
                    .subscribe({ next: (user) => resolve(user) });
            });

            const req = httpMock.expectOne('/api/v2/public/user/me');
            req.flush(null, { status: 204, statusText: 'No Content' });

            const user = await promise;
            expect(user).toEqual(ANONYMOUS);
        });
    });

    describe('logout', () => {
        it('should POST to logout endpoint and update auth status', async () => {
            const mockRedirect = { url: '/login' };

            const promise = new Promise<any>((resolve) => {
                service
                    .logout()
                    .subscribe({ next: (result) => resolve(result) });
            });

            const req = httpMock.expectOne('/api/v2/public/user/logout');
            expect(req.request.method).toBe('POST');
            req.flush(mockRedirect);

            const result = await promise;
            expect(result).toEqual(mockRedirect);
        });
    });

    describe('getOrders', () => {
        it('should GET user orders', async () => {
            const mockOrders = [{ id: 'order-1' }, { id: 'order-2' }];

            const promise = new Promise<any[]>((resolve) => {
                service
                    .getOrders()
                    .subscribe({ next: (orders) => resolve(orders) });
            });

            const req = httpMock.expectOne('/api/v2/public/user/reservations');
            expect(req.request.method).toBe('GET');
            req.flush(mockOrders);

            const orders = await promise;
            expect(orders).toEqual(mockOrders);
        });
    });

    describe('updateUser', () => {
        it('should POST updated user data', async () => {
            const userData = { firstName: 'Jane', lastName: 'Smith' };
            const mockResponse = {
                entity: userData,
                success: true,
                validationResult: null,
            };

            const promise = new Promise<any>((resolve) => {
                service
                    .updateUser(userData)
                    .subscribe({ next: (res) => resolve(res) });
            });

            const req = httpMock.expectOne('/api/v2/public/user/me');
            expect(req.request.method).toBe('POST');
            req.flush(mockResponse);

            const res = await promise;
            expect(res).toEqual(mockResponse);
        });
    });

    describe('deleteProfile', () => {
        it('should DELETE profile and update auth status', async () => {
            const mockRedirect = { url: '/goodbye' };

            const promise = new Promise<any>((resolve) => {
                service
                    .deleteProfile()
                    .subscribe({ next: (result) => resolve(result) });
            });

            const req = httpMock.expectOne('/api/v2/public/user/me');
            expect(req.request.method).toBe('DELETE');
            req.flush(mockRedirect);

            const result = await promise;
            expect(result).toEqual(mockRedirect);
        });
    });

    describe('authenticationStatus observable', () => {
        it('should emit current auth status on subscription', async () => {
            const mockUser: User = {
                firstName: 'John',
                lastName: 'Doe',
                emailAddress: 'john@example.com',
                profile: {
                    fullName: 'John Doe',
                    additionalData: {},
                },
            };

            const initPromise = service.initAuthenticationStatus();

            const authReq = httpMock.expectOne(
                '/api/v2/public/user/authentication-enabled',
            );
            authReq.flush(true);

            const userReq = httpMock.expectOne('/api/v2/public/user/me');
            userReq.flush(mockUser);

            await initPromise;

            let status: any;
            service.authenticationStatus.subscribe((s) => (status = s));
            expect(status.enabled).toBe(true);
            expect(status.user).toEqual(mockUser);
        });
    });
});
