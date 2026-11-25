import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AuthRequest, AuthResponse, CreateUserPayload, Loan, LoanPayload, UpdateUserPayload, User
} from '../../shared/models/api.models';

const API_URL = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  login(payload: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_URL}/auth/login`, payload);
  }
}

@Injectable({ providedIn: 'root' })
export class LoanApiService {
  private readonly http = inject(HttpClient);
  myLoans(): Observable<Loan[]> { return this.http.get<Loan[]>(`${API_URL}/loans/my-loans`); }
  getById(id: number): Observable<Loan> { return this.http.get<Loan>(`${API_URL}/loans/${id}`); }
  all(): Observable<Loan[]> { return this.http.get<Loan[]>(`${API_URL}/admin/loans`); }
  pending(): Observable<Loan[]> { return this.http.get<Loan[]>(`${API_URL}/admin/loans/pending`); }
  create(payload: LoanPayload): Observable<Loan> { return this.http.post<Loan>(`${API_URL}/loans`, payload); }
  update(id: number, payload: LoanPayload): Observable<Loan> {
    return this.http.put<Loan>(`${API_URL}/admin/loans/${id}`, payload);
  }
  approve(id: number): Observable<Loan> {
    return this.http.patch<Loan>(`${API_URL}/admin/loans/${id}/approve`, {});
  }
  reject(id: number): Observable<Loan> {
    return this.http.patch<Loan>(`${API_URL}/admin/loans/${id}/reject`, {});
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/admin/loans/${id}`);
  }
}

@Injectable({ providedIn: 'root' })
export class UserApiService {
  private readonly http = inject(HttpClient);
  all(): Observable<User[]> { return this.http.get<User[]>(`${API_URL}/admin/users`); }
  create(payload: CreateUserPayload): Observable<User> { return this.http.post<User>(`${API_URL}/admin/users`, payload); }
  update(id: number, payload: UpdateUserPayload): Observable<User> { return this.http.put<User>(`${API_URL}/admin/users/${id}`, payload); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${API_URL}/admin/users/${id}`); }
}
