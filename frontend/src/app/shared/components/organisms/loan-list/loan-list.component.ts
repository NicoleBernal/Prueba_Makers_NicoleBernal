import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { Loan } from '../../../models/api.models';
import { ButtonComponent } from '../../atoms/button/button.component';
import { StateMessageComponent } from '../../atoms/state-message/state-message.component';
import { StatusBadgeComponent } from '../../atoms/status-badge/status-badge.component';

@Component({
  selector: 'app-loan-list',
  imports: [CurrencyPipe, DatePipe, ButtonComponent, StateMessageComponent, StatusBadgeComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section aria-labelledby="history-title">
      <div class="section-title">
        <h2 id="history-title">Estado de solicitudes</h2>
        <app-button (pressed)="refresh.emit()">Actualizar</app-button>
      </div>
      @if (loading() && loans().length === 0) {
        <app-state-message kind="loading" message="Cargando préstamos…" />
      } @else if (loans().length === 0) {
        <app-state-message kind="empty" title="Aún no tienes solicitudes" message="Tu primera solicitud aparecerá aquí." />
      } @else {
        <div class="loan-list">
          @for (loan of loans(); track loan.id) {
            <article class="loan-item" [class]="loan.status.toLowerCase()">
              <div>
                <strong>{{ loan.amount | currency:'COP':'symbol-narrow':'1.2-2' }}</strong>
                <span>{{ loan.termMonths }} meses · {{ loan.createdAt | date:'mediumDate' }}</span>
              </div>
              <app-status-badge [status]="loan.status" />
            </article>
          }
        </div>
      }
    </section>
  `
})
export class LoanListComponent {
  readonly loans = input.required<readonly Loan[]>();
  readonly loading = input(false);
  readonly refresh = output<void>();
}
