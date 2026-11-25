import { ChangeDetectionStrategy, Component, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UpdateUserPayload, User, UserRole } from '../../../models/api.models';
import { ButtonComponent } from '../../atoms/button/button.component';

/** Formulario de usuario para crear (sin `user`) o editar (con `user`). La contraseña es opcional al editar. */
@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, ButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
      <label for="user-name">Nombre<input id="user-name" formControlName="name" autocomplete="off" /></label>
      <label for="user-email">Correo<input id="user-email" type="email" formControlName="email" autocomplete="off" /></label>
      <label for="user-role">Rol
        <select id="user-role" formControlName="role">
          <option value="USER">Usuario</option>
          <option value="ADMIN">Administrador</option>
        </select>
      </label>
      <label for="user-password">Contraseña{{ user() ? ' (dejar vacía para conservar)' : '' }}
        <input id="user-password" type="password" formControlName="password" autocomplete="new-password" />
      </label>
      @if (showErrors) {
        <span class="field-error">Completa nombre, correo válido, rol y una contraseña de 3 a 72 caracteres{{ user() ? ' si deseas cambiarla' : '' }}.</span>
      }
      <div class="form-actions">
        <app-button (pressed)="cancelled.emit()">Cancelar</app-button>
        <app-button type="submit" variant="primary">{{ user() ? 'Guardar cambios' : 'Crear usuario' }}</app-button>
      </div>
    </form>
  `
})
export class UserFormComponent {
  private readonly fb = inject(FormBuilder);
  readonly user = input<User | null>(null);
  readonly submitted = output<UpdateUserPayload>();
  readonly cancelled = output<void>();
  protected showErrors = false;

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    role: ['USER' as UserRole, Validators.required],
    password: ['', [Validators.minLength(3), Validators.maxLength(72)]]
  });

  constructor() {
    effect(() => {
      const user = this.user();
      this.showErrors = false;
      this.form.reset({ name: user?.name ?? '', email: user?.email ?? '', role: user?.role ?? 'USER', password: '' });
    });
  }

  protected submit(): void {
    const password = this.form.controls.password.value.trim();
    const missingPassword = !this.user() && password.length < 3;
    if (this.form.invalid || missingPassword) {
      this.showErrors = true;
      this.form.markAllAsTouched();
      return;
    }
    this.submitted.emit({ ...this.form.getRawValue(), password: password || undefined });
  }
}
