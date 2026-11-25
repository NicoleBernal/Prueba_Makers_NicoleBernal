import {
  ChangeDetectionStrategy, Component, ElementRef, HostListener, OnDestroy, afterNextRender, input, output, viewChild
} from '@angular/core';

let nextId = 0;

/** Diálogo modal accesible: foco inicial, Escape, clic en el fondo, foco atrapado y foco devuelto al cerrar. */
@Component({
  selector: 'app-modal',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="modal-backdrop" (mousedown)="onBackdrop($event)">
      <div #panel class="modal" role="dialog" aria-modal="true" tabindex="-1"
        [attr.aria-labelledby]="titleId" (keydown)="trapFocus($event)">
        <header class="modal-header">
          <h2 [id]="titleId">{{ title() }}</h2>
          <button type="button" class="icon-button" aria-label="Cerrar" (click)="closed.emit()">×</button>
        </header>
        <ng-content />
      </div>
    </div>
  `
})
export class ModalComponent implements OnDestroy {
  readonly title = input.required<string>();
  readonly closed = output<void>();
  readonly titleId = `modal-title-${nextId++}`;
  private readonly panel = viewChild.required<ElementRef<HTMLElement>>('panel');
  private readonly previouslyFocused = document.activeElement as HTMLElement | null;

  constructor() {
    afterNextRender(() => {
      const panel = this.panel().nativeElement;
      const first = panel.querySelector<HTMLElement>('input, select, textarea');
      (first ?? panel).focus();
    });
  }

  ngOnDestroy(): void { this.previouslyFocused?.focus?.(); }

  @HostListener('document:keydown.escape')
  protected onEscape(): void { this.closed.emit(); }

  protected onBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.closed.emit();
  }

  protected trapFocus(event: KeyboardEvent): void {
    if (event.key !== 'Tab') return;
    const focusable = [...this.panel().nativeElement.querySelectorAll<HTMLElement>(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    )].filter(element => !(element as HTMLButtonElement).disabled);
    if (focusable.length === 0) return;
    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    const active = document.activeElement;
    if (event.shiftKey && (active === first || active === this.panel().nativeElement)) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && active === last) {
      event.preventDefault();
      first.focus();
    }
  }
}
