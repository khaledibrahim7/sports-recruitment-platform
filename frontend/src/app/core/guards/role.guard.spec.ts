import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { roleGuard } from './role.guard';

describe('roleGuard', () => {
  function run(role: 'PLAYER' | 'ACADEMY' | 'CLUB' | null, loggedIn: boolean, allowed: string[]) {
    const router = jasmine.createSpyObj<Router>('Router', ['createUrlTree']);
    router.createUrlTree.and.returnValue({} as never);
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: router },
        { provide: AuthService, useValue: { userRole: () => role, isLoggedIn: () => loggedIn } }
      ]
    });
    const result = TestBed.runInInjectionContext(() => roleGuard(
      { data: { roles: allowed } } as unknown as ActivatedRouteSnapshot,
      {} as RouterStateSnapshot
    ));
    TestBed.resetTestingModule();
    return { result, router };
  }

  it('allows a role listed on the route', () => {
    expect(run('PLAYER', true, ['PLAYER']).result).toBeTrue();
  });

  it('redirects authenticated users with the wrong role home', () => {
    const { result, router } = run('CLUB', true, ['PLAYER']);
    expect(result).toBeTruthy();
    expect(router.createUrlTree).toHaveBeenCalledWith(['/']);
  });

  it('redirects unauthenticated users to login', () => {
    const { result, router } = run(null, false, ['PLAYER']);
    expect(result).toBeTruthy();
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
  });
});
