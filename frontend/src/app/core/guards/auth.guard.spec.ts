import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth.guard';
import { guestGuard } from './guest.guard';

describe('authGuard & guestGuard', () => {
  function setup(loggedIn: boolean) {
    const router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: router },
        { provide: AuthService, useValue: { isLoggedIn: () => loggedIn } }
      ]
    });
    return router;
  }

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('authGuard allows authenticated users and redirects guests to /login with returnUrl', () => {
    const routerAuth = setup(true);
    const allowed = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url: '/profile' } as RouterStateSnapshot)
    );
    expect(allowed).toBeTrue();
    expect(routerAuth.navigate).not.toHaveBeenCalled();

    TestBed.resetTestingModule();
    const routerGuest = setup(false);
    const blocked = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url: '/profile' } as RouterStateSnapshot)
    );
    expect(blocked).toBeFalse();
    expect(routerGuest.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { returnUrl: '/profile' } });
  });

  it('guestGuard allows guests and redirects authenticated users to /profile', () => {
    const routerGuest = setup(false);
    const allowed = TestBed.runInInjectionContext(() =>
      guestGuard({} as ActivatedRouteSnapshot, { url: '/login' } as RouterStateSnapshot)
    );
    expect(allowed).toBeTrue();
    expect(routerGuest.navigate).not.toHaveBeenCalled();

    TestBed.resetTestingModule();
    const routerAuth = setup(true);
    const blocked = TestBed.runInInjectionContext(() =>
      guestGuard({} as ActivatedRouteSnapshot, { url: '/login' } as RouterStateSnapshot)
    );
    expect(blocked).toBeFalse();
    expect(routerAuth.navigate).toHaveBeenCalledWith(['/profile']);
  });
});
