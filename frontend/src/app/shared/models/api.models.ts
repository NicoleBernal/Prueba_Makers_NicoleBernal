export type UserRole = 'USER' | 'ADMIN';
export type LoanStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface AuthRequest { email: string; password: string; }
export interface AuthResponse {
  token: string;
  tokenType: string;
  email: string;
  name: string;
  role: UserRole;
}

export interface SessionUser { email: string; name: string; role: UserRole; }

export interface Loan {
  id: number;
  amount: number;
  termMonths: number;
  status: LoanStatus;
  userEmail: string;
  borrowerName?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface LoanPayload { amount: number; termMonths: number; }

export interface User {
  id: number;
  email: string;
  name: string;
  role: UserRole;
  createdAt: string;
}

export interface CreateUserPayload {
  email: string;
  password: string;
  name: string;
  role: UserRole;
}

export interface UpdateUserPayload {
  email: string;
  password?: string;
  name: string;
  role: UserRole;
}

export interface ApiError {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  validationErrors?: Record<string, string>;
}
