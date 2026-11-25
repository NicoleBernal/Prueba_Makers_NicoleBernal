import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

export type ButtonVariant = 'primary' | 'ghost' | 'danger' | 'approve' | 'reject';
export type ButtonSize = 'normal' | 'small';

@Component({
  selector: 'app-button',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '[class.full-width]': 'fullWidth()' },
  template: `
    <button
      [attr.type]="type()"
      [class]="variant()"
      [class.small]="size() === 'small'"
      [class.full]="fullWidth()"
      [disabled]="disabled()"
      [attr.aria-label]="ariaLabel() || null"
      (click)="pressed.emit()"
    >
      <ng-content />
    </button>
  `,
  styles: `:host { display: inline-block; } :host.full-width, :host.full-width button { width: 100%; }`
})
export class ButtonComponent {
  readonly type = input<'button' | 'submit'>('button');
  readonly variant = input<ButtonVariant>('ghost');
  readonly size = input<ButtonSize>('normal');
  readonly disabled = input(false);
  readonly fullWidth = input(false);
  readonly ariaLabel = input('');
  readonly pressed = output<void>();
}
