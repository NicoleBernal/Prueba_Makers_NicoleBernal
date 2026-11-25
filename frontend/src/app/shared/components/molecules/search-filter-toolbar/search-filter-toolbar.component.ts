import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

export interface FilterOption<T extends string = string> {
  value: T;
  label: string;
}

@Component({
  selector: 'app-search-filter-toolbar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="toolbar" role="search">
      <label>
        {{ searchLabel() }}
        <input
          type="search"
          [placeholder]="placeholder()"
          [value]="searchValue()"
          (input)="searchChange.emit(asInput($event).value)"
        />
      </label>
      @if (filterOptions().length) {
        <label>
          {{ filterLabel() }}
          <select [value]="filterValue()" (change)="filterChange.emit(asSelect($event).value)">
            @for (option of filterOptions(); track option.value) {
              <option [value]="option.value">{{ option.label }}</option>
            }
          </select>
        </label>
      }
    </div>
  `
})
export class SearchFilterToolbarComponent {
  readonly searchLabel = input('Buscar');
  readonly placeholder = input('');
  readonly searchValue = input('');
  readonly filterLabel = input('Filtrar');
  readonly filterValue = input('');
  readonly filterOptions = input<readonly FilterOption[]>([]);
  readonly searchChange = output<string>();
  readonly filterChange = output<string>();

  protected asInput(event: Event): HTMLInputElement { return event.target as HTMLInputElement; }
  protected asSelect(event: Event): HTMLSelectElement { return event.target as HTMLSelectElement; }
}
