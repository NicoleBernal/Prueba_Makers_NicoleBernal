import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-alert',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (message()) {
      <div class="alert" [class]="kind()" [attr.role]="kind() === 'error' ? 'alert' : 'status'">
        {{ message() }}
      </div>
    }
  `
})
export class AlertComponent {
  readonly kind = input<'success' | 'error'>('error');
  readonly message = input<string | null>(null);
}
