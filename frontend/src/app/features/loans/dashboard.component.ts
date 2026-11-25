import { ChangeDetectionStrategy, Component, OnInit, computed, inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { LoanListComponent } from '../../shared/components/organisms/loan-list/loan-list.component';
import { LoanFormComponent } from '../../shared/components/organisms/loan-form/loan-form.component';
import { PageHeaderComponent } from '../../shared/components/molecules/page-header/page-header.component';
import { StatItem, StatStripComponent } from '../../shared/components/molecules/stat-strip/stat-strip.component';
import { PageShellComponent } from '../../shared/components/templates/page-shell/page-shell.component';
import { LoanPayload } from '../../shared/models/api.models';
import { LoanActions, loansFeature } from '../../store/loans/loans.store';

@Component({
  selector: 'app-dashboard',
  imports: [LoanListComponent, LoanFormComponent, PageHeaderComponent, StatStripComponent, PageShellComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-page-shell>
      <app-page-header title="Mis préstamos" description="Solicita un préstamo y consulta su estado." />
      <app-stat-strip [items]="stats()" />
      <section class="grid two">
        <div class="card sticky">
          <app-loan-form [loading]="loading()" (submitted)="submit($event)" />
        </div>
        <div class="card">
          <app-loan-list [loans]="loans()" [loading]="loading()" (refresh)="reload()" />
        </div>
      </section>
    </app-page-shell>
  `
})
export class DashboardComponent implements OnInit {
  private readonly store = inject(Store);
  readonly loans = this.store.selectSignal(loansFeature.selectLoans);
  readonly loading = this.store.selectSignal(loansFeature.selectLoading);
  readonly stats = computed<StatItem[]>(() => {
    const loans = this.loans();
    const count = (status: string) => loans.filter(loan => loan.status === status).length;
    return [
      { label: 'Solicitudes', value: loans.length },
      { label: 'Pendientes', value: count('PENDING'), tone: 'pending' },
      { label: 'Aprobadas', value: count('APPROVED'), tone: 'approved' },
      { label: 'Rechazadas', value: count('REJECTED'), tone: 'rejected' }
    ];
  });
  ngOnInit(): void { this.reload(); }
  reload(): void { this.store.dispatch(LoanActions.loadMine()); }
  submit(payload: LoanPayload): void { this.store.dispatch(LoanActions.create({ payload })); }
}
