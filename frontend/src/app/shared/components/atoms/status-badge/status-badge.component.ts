import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { LoanStatus } from '../../../models/api.models';

const STATUS_LABELS: Record<LoanStatus, string> = {
  PENDING: 'Pendiente',
  APPROVED: 'Aprobado',
  REJECTED: 'Rechazado'
};

@Component({
  selector: 'app-status-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span class="badge" [class]="status().toLowerCase()">{{ label() }}</span>`
})
export class StatusBadgeComponent {
  readonly status = input.required<LoanStatus>();
  protected label(): string { return STATUS_LABELS[this.status()]; }
}
