import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { Loan } from '../../../models/api.models';
import { ButtonComponent } from '../../atoms/button/button.component';
import { StateMessageComponent } from '../../atoms/state-message/state-message.component';
import { StatusBadgeComponent } from '../../atoms/status-badge/status-badge.component';

@Component({
  selector: 'app-admin-loan-table',
  imports: [CurrencyPipe, DatePipe, ButtonComponent, StateMessageComponent, StatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loading() && loans().length === 0) {
      <app-state-message kind="loading" message="Cargando solicitudes…" />
    } @else if (loans().length === 0) {
      <app-state-message kind="empty" title="Sin resultados" message="No hay solicitudes para los filtros elegidos." />
    } @else {
      <div class="table-wrap">
        <table class="responsive">
          <thead><tr><th>ID</th><th>Solicitante</th><th>Monto</th><th>Plazo</th><th>Estado</th><th>Fecha</th><th class="actions-heading"><span class="sr-only">Acciones</span></th></tr></thead>
          <tbody>
            @for (loan of loans(); track loan.id) {
              <tr>
                <td data-label="ID">#{{ loan.id }}</td>
                <td data-label="Solicitante">{{ loan.borrowerName || loan.userEmail }}</td>
                <td data-label="Monto">{{ loan.amount | currency:'COP':'symbol-narrow':'1.2-2' }}</td>
                <td data-label="Plazo">{{ loan.termMonths }} meses</td>
                <td data-label="Estado"><app-status-badge [status]="loan.status" /></td>
                <td data-label="Fecha">{{ loan.createdAt | date:'shortDate' }}</td>
                <td class="actions">
                  <div class="action-buttons">
                    @if (loan.status === 'PENDING') {
                      <app-button size="small" variant="approve" (pressed)="approve.emit(loan)">Aprobar</app-button>
                      <app-button size="small" variant="reject" (pressed)="reject.emit(loan)">Rechazar</app-button>
                    }
                    <app-button size="small" (pressed)="edit.emit(loan)">Editar</app-button>
                    <app-button size="small" variant="danger" (pressed)="remove.emit(loan)">Eliminar</app-button>
                  </div>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
  styles: `
    .action-buttons { display: flex; flex-wrap: wrap; align-items: center; justify-content: flex-end; gap: .3rem; }
  `
})
export class AdminLoanTableComponent {
  readonly loans = input.required<readonly Loan[]>();
  readonly loading = input(false);
  readonly edit = output<Loan>();
  readonly approve = output<Loan>();
  readonly reject = output<Loan>();
  readonly remove = output<Loan>();
}
