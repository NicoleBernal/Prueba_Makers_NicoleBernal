import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { AlertComponent } from '../../shared/components/atoms/alert/alert.component';
import { ButtonComponent } from '../../shared/components/atoms/button/button.component';
import { AuthActions, authFeature } from '../../store/auth/auth.store';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, AlertComponent, ButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="auth-page">
      <section class="auth-brand" aria-hidden="true">
        <h2>Makers Bank</h2>
        <p>Gestión de préstamos</p>
      </section>
      <section class="auth-form">
        <div class="auth-card" aria-labelledby="login-title">
          <h1 id="login-title">Iniciar sesión</h1>
          <p class="muted">Accede para gestionar préstamos.</p>
          <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
            <label for="email">Correo electrónico
              <input id="email" type="email" formControlName="email" autocomplete="email"
                [attr.aria-invalid]="invalid('email')" aria-describedby="email-error" />
            </label>
            @if (invalid('email')) { <span id="email-error" class="field-error">Ingresa un correo válido.</span> }
            <label for="password">Contraseña
              <input id="password" type="password" formControlName="password" autocomplete="current-password"
                [attr.aria-invalid]="invalid('password')" aria-describedby="password-error" />
            </label>
            @if (invalid('password')) { <span id="password-error" class="field-error">La contraseña es obligatoria.</span> }
            <app-alert kind="error" [message]="error()" />
            <app-button type="submit" variant="primary" [fullWidth]="true" [disabled]="form.invalid || loading()">
              {{ loading() ? 'Ingresando…' : 'Iniciar sesión' }}
            </app-button>
          </form>
        </div>
      </section>
    </main>
  `
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly store = inject(Store);
  readonly loading = this.store.selectSignal(authFeature.selectLoading);
  readonly error = this.store.selectSignal(authFeature.selectError);
  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });
  invalid(name: 'email' | 'password'): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.touched || control.dirty);
  }
  submit(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    this.store.dispatch(AuthActions.login({ credentials: this.form.getRawValue() }));
  }
}
