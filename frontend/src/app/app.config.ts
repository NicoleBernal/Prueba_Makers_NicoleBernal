import { ApplicationConfig, inject, isDevMode, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideStore, Store } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';

import { routes } from './app.routes';
import { jwtInterceptor } from './core/auth/auth.security';
import { AuthActions, AuthEffects, authFeature } from './store/auth/auth.store';
import { LoanEffects, loansFeature } from './store/loans/loans.store';
import { UserEffects, usersFeature } from './store/users/users.store';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor])),
    provideStore({
      [authFeature.name]: authFeature.reducer,
      [loansFeature.name]: loansFeature.reducer,
      [usersFeature.name]: usersFeature.reducer
    }),
    provideEffects(AuthEffects, LoanEffects, UserEffects),
    provideStoreDevtools({ maxAge: 25, logOnly: !isDevMode() }),
    provideAppInitializer(() => inject(Store).dispatch(AuthActions.restoreSession()))
  ]
};
