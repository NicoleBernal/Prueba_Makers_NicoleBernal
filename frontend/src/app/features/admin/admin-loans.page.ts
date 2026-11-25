import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { ButtonComponent } from '../../shared/components/atoms/button/button.component';
import { PageHeaderComponent } from '../../shared/components/molecules/page-header/page-header.component';
import { SearchFilterToolbarComponent } from '../../shared/components/molecules/search-filter-toolbar/search-filter-toolbar.component';
import { SegmentOption, SegmentedFilterComponent } from '../../shared/components/molecules/segmented-filter/segmented-filter.component';
import { AdminLoanTableComponent } from '../../shared/components/organisms/admin-loan-table/admin-loan-table.component';
import { ConfirmDialogComponent } from '../../shared/components/organisms/confirm-dialog/confirm-dialog.component';
import { LoanFormComponent } from '../../shared/components/organisms/loan-form/loan-form.component';
import { ModalComponent } from '../../shared/components/organisms/modal/modal.component';
import { PageShellComponent } from '../../shared/components/templates/page-shell/page-shell.component';
import { Loan, LoanPayload, LoanStatus } from '../../shared/models/api.models';
import { LoanActions, loansFeature } from '../../store/loans/loans.store';

type StatusFilter = LoanStatus | 'ALL';
type PendingAction = { type: 'approve' | 'reject' | 'delete'; loan: Loan };

const STATUSES: readonly StatusFilter[] = ['ALL', 'PENDING', 'APPROVED', 'REJECTED'];

const CONFIRMATION: Record<PendingAction['type'], { title: string; verb: string; variant: 'approve' | 'reject' | 'danger' }> = {
  approve: { title: 'Aprobar préstamo', verb: 'Aprobar', variant: 'approve' },
  reject: { title: 'Rechazar préstamo', verb: 'Rechazar', variant: 'reject' },
  delete: { title: 'Eliminar préstamo', verb: 'Eliminar', variant: 'danger' }
};

/**
 * Gestión de solicitudes. El estado y la búsqueda viven en la URL (?status=…&q=…),
 * así los filtros se pueden compartir, recargar y recorrer con el historial del navegador.
 */
@Component({
  selector: 'app-admin-loans-page',
  imports: [
    ButtonComponent, PageHeaderComponent, SearchFilterToolbarComponent, SegmentedFilterComponent,
    AdminLoanTableComponent, ConfirmDialogComponent, LoanFormComponent, ModalComponent, PageShellComponent
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-page-shell>
      <app-page-header title="Préstamos" description="Revisa, edita y resuelve las solicitudes.">
        <app-button (pressed)="reload()">Actualizar</app-button>
      </app-page-header>

      <div class="toolbar">
        <app-segmented-filter label="Filtrar por estado" [options]="statusOptions()" [value]="status()"
          (changed)="setStatus($event)" />
        <app-search-filter-toolbar placeholder="Correo, nombre o ID" [searchValue]="search()"
          (searchChange)="setSearch($event)" />
      </div>

      <app-admin-loan-table [loans]="filteredLoans()" [loading]="loading()"
        (edit)="editingLoan.set($event)" (approve)="ask('approve', $event)"
        (reject)="ask('reject', $event)" (remove)="ask('delete', $event)" />

      @if (editingLoan(); as loan) {
        <app-modal [title]="'Editar préstamo #' + loan.id" (closed)="editingLoan.set(null)">
          <app-loan-form heading="" submitLabel="Guardar cambios" [cancellable]="true" [loading]="loading()"
            [initial]="{ amount: loan.amount, termMonths: loan.termMonths }"
            (submitted)="save(loan, $event)" (cancelled)="editingLoan.set(null)" />
        </app-modal>
      }

      @if (pending(); as action) {
        <app-confirm-dialog [title]="confirmation[action.type].title"
          [message]="'¿' + confirmation[action.type].verb + ' el préstamo #' + action.loan.id + ' de ' + (action.loan.borrowerName || action.loan.userEmail) + '?'"
          [confirmLabel]="confirmation[action.type].verb" [variant]="confirmation[action.type].variant"
          (confirmed)="confirm(action)" (cancelled)="pending.set(null)" />
      }
    </app-page-shell>
  `
})
export class AdminLoansPage implements OnInit {
  private readonly store = inject(Store);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly params = toSignal(this.route.queryParamMap, { initialValue: this.route.snapshot.queryParamMap });

  protected readonly confirmation = CONFIRMATION;
  readonly loans = this.store.selectSignal(loansFeature.selectLoans);
  readonly loading = this.store.selectSignal(loansFeature.selectLoading);
  readonly editingLoan = signal<Loan | null>(null);
  readonly pending = signal<PendingAction | null>(null);

  readonly status = computed<StatusFilter>(() => {
    const value = this.params().get('status') as StatusFilter | null;
    return value && STATUSES.includes(value) ? value : 'ALL';
  });
  readonly search = computed(() => this.params().get('q') ?? '');

  readonly statusOptions = computed<SegmentOption[]>(() => {
    const loans = this.loans();
    const count = (status: LoanStatus) => loans.filter(loan => loan.status === status).length;
    return [
      { value: 'ALL', label: 'Todos', count: loans.length },
      { value: 'PENDING', label: 'Pendientes', count: count('PENDING') },
      { value: 'APPROVED', label: 'Aprobados', count: count('APPROVED') },
      { value: 'REJECTED', label: 'Rechazados', count: count('REJECTED') }
    ];
  });

  readonly filteredLoans = computed(() => {
    const query = this.search().trim().toLowerCase();
    const status = this.status();
    return this.loans().filter(loan => (status === 'ALL' || loan.status === status)
      && (!query || loan.userEmail.toLowerCase().includes(query)
        || (loan.borrowerName ?? '').toLowerCase().includes(query)
        || String(loan.id).includes(query)));
  });

  ngOnInit(): void { this.reload(); }

  reload(): void { this.store.dispatch(LoanActions.loadAll()); }

  setStatus(status: string): void { this.navigate({ status: status === 'ALL' ? null : status }); }
  setSearch(query: string): void { this.navigate({ q: query.trim() || null }); }

  ask(type: PendingAction['type'], loan: Loan): void { this.pending.set({ type, loan }); }

  confirm({ type, loan }: PendingAction): void {
    this.pending.set(null);
    if (type === 'approve') this.store.dispatch(LoanActions.approve({ id: loan.id }));
    else if (type === 'reject') this.store.dispatch(LoanActions.reject({ id: loan.id }));
    else this.store.dispatch(LoanActions.delete({ id: loan.id }));
  }

  save(loan: Loan, payload: LoanPayload): void {
    this.store.dispatch(LoanActions.update({ id: loan.id, payload }));
    this.editingLoan.set(null);
  }

  private navigate(queryParams: Record<string, string | null>): void {
    void this.router.navigate([], { relativeTo: this.route, queryParams, queryParamsHandling: 'merge', replaceUrl: true });
  }
}
