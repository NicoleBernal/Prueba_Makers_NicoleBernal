import { ChangeDetectionStrategy, Component, effect, inject, signal } from '@angular/core';
import { Store } from '@ngrx/store';
import { LoanActions, loansFeature } from '../../../../store/loans/loans.store';
import { UserActions, usersFeature } from '../../../../store/users/users.store';

interface Toast { id: number; kind: 'success' | 'error'; message: string; }

const DURATION_MS = 5000;

/**
 * Muestra como avisos flotantes los mensajes de confirmación y error que publica el estado
 * de préstamos y usuarios, y los consume para que no se repitan.
 */
@Component({
  selector: 'app-toast-host',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toasts" aria-live="polite">
      @for (toast of toasts(); track toast.id) {
        <div class="toast" [class.error]="toast.kind === 'error'" [attr.role]="toast.kind === 'error' ? 'alert' : 'status'">
          <span>{{ toast.message }}</span>
          <button type="button" aria-label="Cerrar aviso" (click)="dismiss(toast.id)">×</button>
        </div>
      }
    </div>
  `
})
export class ToastHostComponent {
  private readonly store = inject(Store);
  private readonly loanConfirmation = this.store.selectSignal(loansFeature.selectConfirmation);
  private readonly loanError = this.store.selectSignal(loansFeature.selectError);
  private readonly userConfirmation = this.store.selectSignal(usersFeature.selectConfirmation);
  private readonly userError = this.store.selectSignal(usersFeature.selectError);
  private nextId = 0;
  readonly toasts = signal<Toast[]>([]);

  constructor() {
    effect(() => {
      const loanOk = this.loanConfirmation();
      const loanFail = this.loanError();
      const userOk = this.userConfirmation();
      const userFail = this.userError();
      if (loanOk || loanFail) this.store.dispatch(LoanActions.clearFeedback());
      if (userOk || userFail) this.store.dispatch(UserActions.clearFeedback());
      if (loanOk) this.push('success', loanOk);
      if (loanFail) this.push('error', loanFail);
      if (userOk) this.push('success', userOk);
      if (userFail) this.push('error', userFail);
    });
  }

  dismiss(id: number): void { this.toasts.update(list => list.filter(toast => toast.id !== id)); }

  private push(kind: Toast['kind'], message: string): void {
    const id = this.nextId++;
    this.toasts.update(list => [...list, { id, kind, message }]);
    setTimeout(() => this.dismiss(id), DURATION_MS);
  }
}
