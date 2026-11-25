import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-state-message',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (kind() === 'loading') {
      <div class="skeleton" role="status" [attr.aria-label]="message()">
        <i></i><i></i><i></i>
        <span class="sr-only">{{ message() }}</span>
      </div>
    } @else {
      <div class="empty">
        @if (title()) { <strong>{{ title() }}</strong> }
        <span>{{ message() }}</span>
      </div>
    }
  `
})
export class StateMessageComponent {
  readonly kind = input.required<'loading' | 'empty'>();
  readonly title = input('');
  readonly message = input.required<string>();
}
