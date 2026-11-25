import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export interface StatItem {
  label: string;
  value: number;
  tone?: 'pending' | 'approved' | 'rejected';
}

@Component({
  selector: 'app-stat-strip',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="stats">
      @for (item of items(); track item.label) {
        <div class="stat" [class]="item.tone ?? ''">
          <strong>{{ item.value }}</strong>
          <span>{{ item.label }}</span>
        </div>
      }
    </div>
  `
})
export class StatStripComponent {
  readonly items = input.required<readonly StatItem[]>();
}
