import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { User } from '../../../models/api.models';
import { ButtonComponent } from '../../atoms/button/button.component';
import { StateMessageComponent } from '../../atoms/state-message/state-message.component';

@Component({
  selector: 'app-admin-user-table',
  imports: [DatePipe, ButtonComponent, StateMessageComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loading() && users().length === 0) {
      <app-state-message kind="loading" message="Cargando usuarios…" />
    } @else if (users().length === 0) {
      <app-state-message kind="empty" title="Sin usuarios" message="Crea el primer usuario desde este panel." />
    } @else {
      <div class="table-wrap">
        <table class="responsive">
          <thead><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Creado</th><th class="actions-heading"><span class="sr-only">Acciones</span></th></tr></thead>
          <tbody>
            @for (user of users(); track user.id) {
              <tr>
                <td data-label="Nombre">{{ user.name }}</td>
                <td data-label="Correo">{{ user.email }}</td>
                <td data-label="Rol">{{ user.role === 'ADMIN' ? 'Administrador' : 'Usuario' }}</td>
                <td data-label="Creado">{{ user.createdAt | date:'shortDate' }}</td>
                <td class="actions">
                  <div class="action-buttons">
                    <app-button size="small" (pressed)="edit.emit(user)">Editar</app-button>
                    <app-button size="small" variant="danger" (pressed)="remove.emit(user)">Eliminar</app-button>
                  </div>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
  styles: `.action-buttons { display: flex; align-items: center; justify-content: flex-end; gap: .3rem; }`
})
export class AdminUserTableComponent {
  readonly users = input.required<readonly User[]>();
  readonly loading = input(false);
  readonly edit = output<User>();
  readonly remove = output<User>();
}
