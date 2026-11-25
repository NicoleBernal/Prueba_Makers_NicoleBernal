import { AuthSessionService } from './core/auth/auth-session.service';
import { AuthActions, authFeature, safeReturnUrl } from './store/auth/auth.store';
import { LoanActions, loansFeature } from './store/loans/loans.store';
import { Loan, User } from './shared/models/api.models';
import { UserActions, usersFeature } from './store/users/users.store';

describe('critical state and session behavior', () => {
  beforeEach(() => localStorage.clear());

  it('stores a successful authentication in state', () => {
    const user = { email: 'user@makers.com', name: 'User', role: 'USER' as const };
    const state = authFeature.reducer(undefined, AuthActions.loginSuccess({ token: 'token', user }));
    expect(state.token).toBe('token');
    expect(state.user).toEqual(user);
    expect(state.loading).toBe(false);
  });

  it('clears auth data on logout', () => {
    const populated = authFeature.reducer(undefined, AuthActions.loginSuccess({
      token: 'token', user: { email: 'admin@makers.com', name: 'Admin', role: 'ADMIN' }
    }));
    const state = authFeature.reducer(populated, AuthActions.logout());
    expect(state.token).toBeNull();
    expect(state.user).toBeNull();
    expect(state.restored).toBe(true);
  });

  it('rejects an expired persisted JWT', () => {
    const service = new AuthSessionService();
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({ sub: 'user@makers.com', role: 'USER', exp: 1 }));
    localStorage.setItem('makers.session', JSON.stringify({
      token: `${header}.${payload}.signature`,
      user: { email: 'user@makers.com', name: 'User', role: 'USER' }
    }));
    expect(service.read()).toBeNull();
    expect(localStorage.getItem('makers.session')).toBeNull();
  });

  it('updates a loan after an administrative decision', () => {
    const pending: Loan = {
      id: 1, amount: 5000, termMonths: 12, status: 'PENDING', userEmail: 'user@makers.com',
      createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
    };
    const loaded = loansFeature.reducer(undefined, LoanActions.loadSuccess({ loans: [pending] }));
    const approved = { ...pending, status: 'APPROVED' as const };
    const state = loansFeature.reducer(loaded, LoanActions.decisionSuccess({ loan: approved, confirmation: 'Aprobado' }));
    expect(state.loans[0].status).toBe('APPROVED');
    expect(state.confirmation).toBe('Aprobado');
  });

  it('replaces the edited loan and keeps its status', () => {
    const approved: Loan = {
      id: 2, amount: 1000, termMonths: 6, status: 'APPROVED', userEmail: 'user@makers.com',
      createdAt: '2026-01-01T00:00:00', updatedAt: '2026-01-01T00:00:00'
    };
    const other: Loan = { ...approved, id: 3, amount: 200 };
    const loaded = loansFeature.reducer(undefined, LoanActions.loadSuccess({ loans: [approved, other] }));
    const saving = loansFeature.reducer(loaded, LoanActions.update({ id: 2, payload: { amount: 2500, termMonths: 18 } }));
    expect(saving.loading).toBe(true);
    const state = loansFeature.reducer(saving, LoanActions.updateSuccess({ loan: { ...approved, amount: 2500, termMonths: 18 } }));
    expect(state.loans.map(l => [l.id, l.amount, l.termMonths, l.status]))
      .toEqual([[2, 2500, 18, 'APPROVED'], [3, 200, 6, 'APPROVED']]);
    expect(state.confirmation).toBe('Préstamo actualizado.');
    expect(state.loading).toBe(false);
  });

  it('marks the pending-loans request as loading', () => {
    const state = loansFeature.reducer(undefined, LoanActions.loadPending());
    expect(state.loading).toBe(true);
    expect(state.error).toBeNull();
  });

  it('adds and removes users through the admin state', () => {
    const user: User = { id: 3, name: 'Admin', email: 'admin@makers.com', role: 'ADMIN', createdAt: '2026-01-01T00:00:00' };
    const created = usersFeature.reducer(undefined, UserActions.saveSuccess({ user, confirmation: 'Usuario creado.' }));
    const deleted = usersFeature.reducer(created, UserActions.deleteSuccess({ id: user.id }));
    expect(created.users).toEqual([user]);
    expect(deleted.users).toEqual([]);
  });

  it('only returns to internal routes that the role may open', () => {
    expect(safeReturnUrl('/admin/users?q=ana', 'ADMIN')).toBe('/admin/users?q=ana');
    expect(safeReturnUrl('/admin/users', 'USER')).toBe('/dashboard');
    expect(safeReturnUrl('https://evil.example', 'USER')).toBe('/dashboard');
    expect(safeReturnUrl('//evil.example', 'ADMIN')).toBe('/admin/loans');
    expect(safeReturnUrl('/login', 'ADMIN')).toBe('/admin/loans');
    expect(safeReturnUrl(undefined, 'USER')).toBe('/dashboard');
  });
});
