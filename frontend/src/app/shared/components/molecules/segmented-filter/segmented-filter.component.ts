import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

export interface SegmentOption {
  value: string;
  label: string;
  count?: number;
}

/** Filtro por segmentos (chips) con contador opcional. */
@Component({
  selector: 'app-segmented-filter',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="chips" role="group" [attr.aria-label]="label()">
      @for (option of options(); track option.value) {
        <button type="button" class="chip" [attr.aria-pressed]="option.value === value()"
          (click)="changed.emit(option.value)">
          {{ option.label }}@if (option.count !== undefined) { <b>{{ option.count }}</b> }
        </button>
      }
    </div>
  `
})
export class SegmentedFilterComponent {
  readonly label = input('Filtrar');
  readonly options = input.required<readonly SegmentOption[]>();
  readonly value = input.required<string>();
  readonly changed = output<string>();
}
