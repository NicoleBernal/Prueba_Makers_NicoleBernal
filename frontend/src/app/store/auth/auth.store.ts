import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { createActionGroup, createFeature, createReducer, createSelector, emptyProps, on, props, Store } from '@ngrx/store';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, of, switchMap, tap } from 'rxjs';
import { AuthRequest, SessionUser } from '../../shared/models/api.models';
import { AuthApiService } from '../../core/services/api.services';
import { AuthSessionService } from '../../core/auth/auth-session.service';

export interface AuthState {
  token: string | null;
  user: SessionUser | null;
  loading: boolean;
  restored: boolean;
  error: string | null;
}

const initialState: AuthState = { token: null, user: null, loading: false, restored: false, error: null };

export const AuthActions = createActionGroup({
  source: 'Auth',
  events: {
    'Restore Session': emptyProps(),
    'Restore Success': props<{ token: string; user: SessionUser }>(),
    'Restore Empty': emptyProps(),
    Login: props<{ credentials: AuthRequest }>(),
    'Login Success': props<{ token: string; user: SessionUser }>(),
    'Login Failure': props<{ error: string }>(),
    Logout: emptyProps()
  }
});

export const authFeature = createFeature({
  name: 'auth',
  reducer: createReducer(
    initialState,
    on(AuthActions.restoreSession, state => ({ ...state, loading: true })),
    on(AuthActions.restoreSuccess, (state, { token, user }) => ({ ...state, token, user, loading: false, restored: true })),
    on(AuthActions.restoreEmpty, state => ({ ...state, loading: false, restored: true })),
    on(AuthActions.login, state => ({ ...state, loading: true, error: null })),
    on(AuthActions.loginSuccess, (state, { token, user }) => ({ ...state, token, user, loading: false, restored: true })),
    on(AuthActions.loginFailure, (state, { error }) => ({ ...state, loading: false, error })),
    on(AuthActions.logout, () => ({ ...initialState, restored: true }))
  )
});

/** Vuelve a la página que se pidió antes del login, solo si es una ruta interna permitida para el rol. */
export function safeReturnUrl(candidate: unknown, role: SessionUser['role']): string {
  const home = role === 'ADMIN' ? '/admin/loans' : '/dashboard';
  if (typeof candidate !== 'string' || !candidate.startsWith('/') || candidate.startsWith('//')) return home;
  if (candidate.startsWith('/login')) return home;
  if (candidate.startsWith('/admin') && role !== 'ADMIN') return home;
  return candidate;
}

export const selectIsAuthenticated = createSelector(authFeature.selectToken, token => !!token);
export const selectIsAdmin = createSelector(authFeature.selectUser, user => user?.role === 'ADMIN');

@Injectable()
export class AuthEffects {
  private readonly actions$ = inject(Actions);
  private readonly api = inject(AuthApiService);
  private readonly session = inject(AuthSessionService);
  private readonly router = inject(Router);

  restore$ = createEffect(() => this.actions$.pipe(
    ofType(AuthActions.restoreSession),
    map(() => this.session.read()),
    map(value => value ? AuthActions.restoreSuccess(value) : AuthActions.restoreEmpty())
  ));

  login$ = createEffect(() => this.actions$.pipe(
    ofType(AuthActions.login),
    switchMap(({ credentials }) => this.api.login(credentials).pipe(
      map(response => {
        const session = this.session.save(response);
        return AuthActions.loginSuccess(session);
      }),
      catchError(error => of(AuthActions.loginFailure({
        error: error?.error?.message ?? 'No fue posible iniciar sesión. Revisa tus credenciales.'
      })))
    ))
  ));

  loginSuccess$ = createEffect(() => this.actions$.pipe(
    ofType(AuthActions.loginSuccess),
    tap(({ user }) => {
      const returnUrl = this.router.parseUrl(this.router.url).queryParams['returnUrl'];
      void this.router.navigateByUrl(safeReturnUrl(returnUrl, user.role));
    })
  ), { dispatch: false });

  logout$ = createEffect(() => this.actions$.pipe(
    ofType(AuthActions.logout),
    tap(() => {
      this.session.clear();
      void this.router.navigateByUrl('/login');
    })
  ), { dispatch: false });
}

export function restoreSession(store: Store): () => void {
  return () => store.dispatch(AuthActions.restoreSession());
}
