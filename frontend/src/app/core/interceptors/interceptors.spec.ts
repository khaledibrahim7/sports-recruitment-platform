import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { jwtInterceptor } from './jwt.interceptor';
import { errorInterceptor } from './error.interceptor';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';
import { APP_CONFIG } from '../config/app-config';

describe('HTTP Interceptors (jwtInterceptor & errorInterceptor)', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let routerSpy: jasmine.SpyObj<Router>;
  let authServiceMock: {
    isLoggedIn: jasmine.Spy;
    refreshSession: jasmine.Spy;
    clearSession: jasmine.Spy;
  };
  let toastServiceSpy: jasmine.SpyObj<ToastService>;

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate'], { url: '/profile' });
    authServiceMock = {
      isLoggedIn: jasmine.createSpy('isLoggedIn').and.returnValue(true),
      refreshSession: jasmine.createSpy('refreshSession').and.returnValue(of({ success: true, message: 'ok', data: {} as never })),
      clearSession: jasmine.createSpy('clearSession')
    };
    toastServiceSpy = jasmine.createSpyObj<ToastService>('ToastService', ['error', 'warning', 'success', 'show']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor])),
        provideHttpClientTesting(),
        { provide: Router, useValue: routerSpy },
        { provide: AuthService, useValue: authServiceMock },
        { provide: ToastService, useValue: toastServiceSpy },
        { provide: APP_CONFIG, useValue: { apiBaseUrl: 'http://localhost:8080', sentryDsn: '', environment: 'test' } }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('jwtInterceptor attaches withCredentials and apiBaseUrl only to /api/ requests', () => {
    http.get('/api/v1/players').subscribe();
    const apiReq = httpMock.expectOne('http://localhost:8080/api/v1/players');
    expect(apiReq.request.withCredentials).toBeTrue();
    apiReq.flush({ success: true, data: [] });

    http.get('https://storage.example.com/upload').subscribe();
    const externalReq = httpMock.expectOne('https://storage.example.com/upload');
    expect(externalReq.request.withCredentials).toBeFalse();
    externalReq.flush('ok');
  });

  it('errorInterceptor shows a forbidden toast on 403 without logging the user out', () => {
    let caughtError: HttpErrorResponse | undefined;
    http.put('/api/v1/players/me', {}).subscribe({
      error: (err) => { caughtError = err; }
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/players/me');
    req.flush({ message: 'Forbidden' }, { status: 403, statusText: 'Forbidden' });

    expect(caughtError?.status).toBe(403);
    expect(authServiceMock.clearSession).not.toHaveBeenCalled();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
    expect(toastServiceSpy.error).toHaveBeenCalled();
  });

  it('errorInterceptor refreshes session on 401 and redirects to login when refresh fails', () => {
    authServiceMock.refreshSession.and.returnValue(throwError(() => new Error('Refresh expired')));

    let caughtError: HttpErrorResponse | undefined;
    http.get('/api/v1/players/me').subscribe({
      error: (err) => { caughtError = err; }
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/players/me');
    req.flush({ message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });

    expect(caughtError?.status).toBe(401);
    expect(authServiceMock.refreshSession).toHaveBeenCalled();
    expect(authServiceMock.clearSession).toHaveBeenCalled();
    expect(toastServiceSpy.warning).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { returnUrl: '/profile' } });
  });
});
