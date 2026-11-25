import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { Store } from '@ngrx/store';
import { ButtonComponent } from '../../shared/components/atoms/button/button.component';
import { PageHeaderComponent } from '../../shared/components/molecules/page-header/page-header.component';
import { AdminUserTableComponent } from '../../shared/components/organisms/admin-user-table/admin-user-table.component';
import { ConfirmDialogComponent } from '../../shared/components/organisms/confirm-dialog/confirm-dialog.component';
import { ModalComponent } from '../../shared/components/organisms/modal/modal.component';
import { UserFormComponent } from '../../shared/components/organisms/user-form/user-form.component';
import { PageShellComponent } from '../../shared/components/templates/page-shell/page-shell.component';
import { CreateUserPayload, UpdateUserPayload, User } from '../../shared/models/api.models';
import { UserActions, usersFeature } from '../../store/users/users.store';

@Component({
  selector: 'app-admin-users-page',
  imports: [
    ButtonComponent, PageHeaderComponent, AdminUserTableComponent, ConfirmDialogComponent,
    ModalComponent, UserFormComponent, PageShellComponent
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-page-shell>
      <app-page-header title="Usuarios" description="Crea, edita y elimina cuentas.">
        <app-button variant="primary" (pressed)="editing.set(null)">Nuevo usuario</app-button>
      </app-page-header>

      <app-admin-user-table [users]="users()" [loading]="loading()"
        (edit)="editing.set($event)" (remove)="deleting.set($event)" />

      @if (editing() !== undefined) {
        <app-modal [title]="editing() ? 'Editar usuario' : 'Crear usuario'" (closed)="editing.set(undefined)">
          <app-user-form [user]="editing() ?? null" (submitted)="save($event)" (cancelled)="editing.set(undefined)" />
        </app-modal>
      }

      @if (deleting(); as user) {
        <app-confirm-dialog title="Eliminar usuario" confirmLabel="Eliminar" variant="danger"
          [message]="'¿Eliminar al usuario ' + user.email + '? Esta acción no se puede deshacer.'"
          (confirmed)="remove(user)" (cancelled)="deleting.set(null)" />
      }
    </app-page-shell>
  `
})
export class AdminUsersPage implements OnInit {
  private readonly store = inject(Store);
  readonly users = this.store.selectSignal(usersFeature.selectUsers);
  readonly loading = this.store.selectSignal(usersFeature.selectLoading);
  /** `undefined`: cerrado · `null`: creando · `User`: editando. */
  readonly editing = signal<User | null | undefined>(undefined);
  readonly deleting = signal<User | null>(null);

  ngOnInit(): void { this.store.dispatch(UserActions.load()); }

  save(payload: UpdateUserPayload): void {
    const editing = this.editing();
    if (editing) {
      this.store.dispatch(UserActions.update({ id: editing.id, payload }));
    } else {
      this.store.dispatch(UserActions.create({ payload: payload as CreateUserPayload }));
    }
    this.editing.set(undefined);
  }

  remove(user: User): void {
    this.deleting.set(null);
    this.store.dispatch(UserActions.delete({ id: user.id }));
  }
}
