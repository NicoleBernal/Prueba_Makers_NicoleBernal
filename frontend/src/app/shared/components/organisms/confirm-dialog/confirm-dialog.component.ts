import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { ButtonComponent, ButtonVariant } from '../../atoms/button/button.component';
import { ModalComponent } from '../modal/modal.component';

/** Confirmación para acciones sensibles (aprobar, rechazar, eliminar); reemplaza a window.confirm. */
@Component({
  selector: 'app-confirm-dialog',
  imports: [ModalComponent, ButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-modal [title]="title()" (closed)="cancelled.emit()">
      <p>{{ message() }}</p>
      <div class="form-actions">
        <app-button (pressed)="cancelled.emit()">Cancelar</app-button>
        <app-button [variant]="variant()" (pressed)="confirmed.emit()">{{ confirmLabel() }}</app-button>
      </div>
    </app-modal>
  `
})
export class ConfirmDialogComponent {
  readonly title = input.required<string>();
  readonly message = input.required<string>();
  readonly confirmLabel = input('Confirmar');
  readonly variant = input<ButtonVariant>('primary');
  readonly confirmed = output<void>();
  readonly cancelled = output<void>();
}
