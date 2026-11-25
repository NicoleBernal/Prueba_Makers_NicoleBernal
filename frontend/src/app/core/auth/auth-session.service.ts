import { Injectable } from '@angular/core';
import { AuthResponse, SessionUser, UserRole } from '../../shared/models/api.models';

interface StoredSession { token: string; user: SessionUser; }

@Injectable({ providedIn: 'root' })
export class AuthSessionService {
  private readonly key = 'makers.session';

  save(response: AuthResponse): StoredSession {
    const session = {
      token: response.token,
      user: { email: response.email, name: response.name, role: response.role }
    };
    localStorage.setItem(this.key, JSON.stringify(session));
    return session;
  }

  read(): StoredSession | null {
    try {
      const raw = localStorage.getItem(this.key);
      if (!raw) return null;
      const value = JSON.parse(raw) as StoredSession;
      const claims = this.decode(value.token);
      const role = this.claimRole(claims);
      const email = String(claims['sub'] ?? '');
      const valid = value.token.split('.').length === 3
        && Number(claims['exp'] ?? 0) * 1000 > Date.now()
        && !!value.user?.email
        && value.user.email === email
        && value.user.role === role;
      if (!valid) this.clear();
      return valid ? value : null;
    } catch {
      this.clear();
      return null;
    }
  }

  token(): string | null { return this.read()?.token ?? null; }
  clear(): void { localStorage.removeItem(this.key); }

  private decode(token: string): Record<string, unknown> {
    const encoded = token.split('.')[1]?.replace(/-/g, '+').replace(/_/g, '/');
    if (!encoded) return {};
    return JSON.parse(decodeURIComponent(atob(encoded).split('').map(c =>
      `%${c.charCodeAt(0).toString(16).padStart(2, '0')}`).join(''))) as Record<string, unknown>;
  }

  private claimRole(claims: Record<string, unknown>): UserRole | null {
    const role = claims['role'];
    return role === 'ADMIN' || role === 'USER' ? role : null;
  }
}
