import { inject, Injectable } from '@angular/core';
import { createActionGroup, createFeature, createReducer, createSelector, emptyProps, on, props } from '@ngrx/store';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { catchError, map, mergeMap, of, switchMap } from 'rxjs';
import { Loan, LoanPayload, LoanStatus } from '../../shared/models/api.models';
import { LoanApiService } from '../../core/services/api.services';

interface LoansState {
  loans: Loan[];
  loading: boolean;
  error: string | null;
  confirmation: string | null;
}
const initialState: LoansState = { loans: [], loading: false, error: null, confirmation: null };
const message = (error: unknown) => (error as { error?: { message?: string } })?.error?.message ?? 'La operación no pudo completarse.';

export const LoanActions = createActionGroup({
  source: 'Loans',
  events: {
    'Load Mine': emptyProps(), 'Load All': emptyProps(), 'Load Pending': emptyProps(),
    'Load Success': props<{ loans: Loan[] }>(), 'Failure': props<{ error: string }>(),
    'Create': props<{ payload: LoanPayload }>(), 'Create Success': props<{ loan: Loan }>(),
    'Update': props<{ id: number; payload: LoanPayload }>(), 'Update Success': props<{ loan: Loan }>(),
    'Approve': props<{ id: number }>(), 'Reject': props<{ id: number }>(), 'Delete': props<{ id: number }>(),
    'Delete Success': props<{ id: number }>(),
    'Decision Success': props<{ loan: Loan; confirmation: string }>(),
    'Clear Feedback': emptyProps()
  }
});

export const loansFeature = createFeature({
  name: 'loans',
  reducer: createReducer(
    initialState,
    on(LoanActions.loadMine, LoanActions.loadAll, LoanActions.loadPending, LoanActions.create,
      LoanActions.update, LoanActions.approve, LoanActions.reject, LoanActions.delete,
      state => ({ ...state, loading: true, error: null, confirmation: null })),
    on(LoanActions.loadSuccess, (state, { loans }) => ({ ...state, loans, loading: false })),
    on(LoanActions.createSuccess, (state, { loan }) => ({ ...state, loans: [loan, ...state.loans], loading: false, confirmation: 'Solicitud creada correctamente.' })),
    on(LoanActions.updateSuccess, (state, { loan }) => ({ ...state, loans: state.loans.map(item => item.id === loan.id ? loan : item), loading: false, confirmation: 'Préstamo actualizado.' })),
    on(LoanActions.decisionSuccess, (state, { loan, confirmation }) => ({ ...state, loans: state.loans.map(item => item.id === loan.id ? loan : item), loading: false, confirmation })),
    on(LoanActions.deleteSuccess, (state, { id }) => ({
      ...state, loans: state.loans.filter(item => item.id !== id), loading: false, confirmation: 'Préstamo eliminado.'
    })),
    on(LoanActions.failure, (state, { error }) => ({ ...state, loading: false, error })),
    on(LoanActions.clearFeedback, state => ({ ...state, error: null, confirmation: null }))
  )
});

export const selectLoansByStatus = (status: LoanStatus | 'ALL') => createSelector(
  loansFeature.selectLoans, loans => status === 'ALL' ? loans : loans.filter(loan => loan.status === status)
);

@Injectable()
export class LoanEffects {
  private readonly actions$ = inject(Actions);
  private readonly api = inject(LoanApiService);
  loadMine$ = createEffect(() => this.actions$.pipe(ofType(LoanActions.loadMine), switchMap(() =>
    this.api.myLoans().pipe(map(loans => LoanActions.loadSuccess({ loans })), catchError(e => of(LoanActions.failure({ error: message(e) })))))));
  loadAll$ = createEffect(() => this.actions$.pipe(ofType(LoanActions.loadAll), switchMap(() =>
    this.api.all().pipe(map(loans => LoanActions.loadSuccess({ loans })), catchError(e => of(LoanActions.failure({ error: message(e) })))))));
  loadPending$ = createEffect(() => this.actions$.pipe(ofType(LoanActions.loadPending), switchMap(() =>
    this.api.pending().pipe(map(loans => LoanActions.loadSuccess({ loans })), catchError(e => of(LoanActions.failure({ error: message(e) })))))));
  create$ = createEffect(() => this.actions$.pipe(ofType(LoanActions.create), mergeMap(({ payload }) =>
    this.api.create(payload).pipe(map(loan => LoanActions.createSuccess({ loan })), catchError(e => of(LoanActions.failure({ error: message(e) })))))));
  update$ = createEffect(() => this.actions$.pipe(ofType(LoanActions.update), mergeMap(({ id, payload }) =>
    this.api.update(id, payload).pipe(map(loan => LoanActions.updateSuccess({ loan })), catchError(e => of(LoanActions.failure({ error: message(e) })))))));
  approve$ = createEffect(() => this.actions$.pipe(ofType(LoanActions.approve), mergeMap(({ id }) =>
    this.api.approve(id).pipe(map(loan => LoanActions.decisionSuccess({ loan, confirmation: 'Préstamo aprobado.' })), catchError(e => of(LoanActions.failure({ error: message(e) })))))));
  reject$ = createEffect(() => this.actions$.pipe(ofType(LoanActions.reject), mergeMap(({ id }) =>
    this.api.reject(id).pipe(map(loan => LoanActions.decisionSuccess({ loan, confirmation: 'Préstamo rechazado.' })), catchError(e => of(LoanActions.failure({ error: message(e) })))))));
  delete$ = createEffect(() => this.actions$.pipe(ofType(LoanActions.delete), mergeMap(({ id }) =>
    this.api.delete(id).pipe(map(() => LoanActions.deleteSuccess({ id })), catchError(e => of(LoanActions.failure({ error: message(e) })))))));
}
