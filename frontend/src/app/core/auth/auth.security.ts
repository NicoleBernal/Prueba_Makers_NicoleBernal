import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { CanActivateFn, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { catchError, filter, map, take, throwError } from 'rxjs';
import { AuthSessionService } from './auth-session.service';
import { AuthActions, authFeature } from '../../store/auth/auth.store';

export const jwtInterceptor: HttpInterceptorFn = (request, next) => {
  const session = inject(AuthSessionService);
  const store = inject(Store);
  const token = session.token();
  const authenticatedRequest = token && !request.url.endsWith('/auth/login')
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;
  return next(authenticatedRequest).pipe(catchError((error: HttpErrorResponse) => {
    if (error.status === 401 && !request.url.endsWith('/auth/login')) {
      store.dispatch(AuthActions.logout());
    }
    return throwError(() => error);
  }));
};

const resolvedAuth = () => {
  const store = inject(Store);
  return store.select(authFeature.selectAuthState).pipe(filter(state => state.restored), take(1));
};

export const authGuard: CanActivateFn = (_route, state) => {
  const router = inject(Router);
  return resolvedAuth().pipe(map(auth =>
    auth.token ? true : router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } })
  ));
};

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  return resolvedAuth().pipe(map(auth =>
    auth.token && auth.user?.role === 'ADMIN' ? true : router.createUrlTree([auth.token ? '/dashboard' : '/login'])
  ));
};

export const guestGuard: CanActivateFn = () => {
  const router = inject(Router);
  return resolvedAuth().pipe(map(auth =>
    !auth.token ? true : router.createUrlTree([auth.user?.role === 'ADMIN' ? '/admin/loans' : '/dashboard'])
  ));
};
