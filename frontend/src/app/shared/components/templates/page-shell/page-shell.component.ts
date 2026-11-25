import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-page-shell',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<main class="page"><ng-content /></main>`
})
export class PageShellComponent {}
