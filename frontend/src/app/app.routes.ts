import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard } from './core/auth/auth.security';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent),
    title: 'Iniciar sesión | Makers Bank'
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/loans/dashboard.component').then(m => m.DashboardComponent),
    title: 'Mis préstamos | Makers Bank'
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'loans' },
      {
        path: 'loans',
        loadComponent: () => import('./features/admin/admin-loans.page').then(m => m.AdminLoansPage),
        title: 'Préstamos | Administración | Makers Bank'
      },
      {
        path: 'users',
        loadComponent: () => import('./features/admin/admin-users.page').then(m => m.AdminUsersPage),
        title: 'Usuarios | Administración | Makers Bank'
      }
    ]
  },
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: '**', redirectTo: 'login' }
];
