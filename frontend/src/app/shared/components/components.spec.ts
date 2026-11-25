import { TestBed } from '@angular/core/testing';
import { ButtonComponent } from './atoms/button/button.component';
import { StatusBadgeComponent } from './atoms/status-badge/status-badge.component';
import { AdminLoanTableComponent } from './organisms/admin-loan-table/admin-loan-table.component';
import { LoanFormComponent } from './organisms/loan-form/loan-form.component';
import { Loan } from '../models/api.models';

const pendingLoan: Loan = {
  id: 7,
  amount: 250000,
  termMonths: 12,
  status: 'PENDING',
  userEmail: 'user@makers.com',
  borrowerName: 'Usuario Makers',
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00'
};

describe('Atomic Design reusable components', () => {
  it('emits a typed button event', () => {
    const fixture = TestBed.createComponent(ButtonComponent);
    let pressed = false;
    fixture.componentInstance.pressed.subscribe(() => pressed = true);
    fixture.detectChanges();
    fixture.nativeElement.querySelector('button').click();
    expect(pressed).toBe(true);
  });

  it('renders the localized loan status', () => {
    const fixture = TestBed.createComponent(StatusBadgeComponent);
    fixture.componentRef.setInput('status', 'APPROVED');
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Aprobado');
    expect(fixture.nativeElement.querySelector('.approved')).toBeTruthy();
  });

  it('validates and emits a loan payload', () => {
    const fixture = TestBed.createComponent(LoanFormComponent);
    const payloads: unknown[] = [];
    fixture.componentInstance.submitted.subscribe(payload => payloads.push(payload));
    fixture.detectChanges();

    fixture.componentInstance.form.setValue({ amount: 5000, termMonths: 12 });
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));

    expect(payloads).toEqual([{ amount: 5000, termMonths: 12 }]);
    expect(fixture.componentInstance.form.value).toEqual({ amount: null, termMonths: null });
  });

  it('rejects non-integer or out-of-range terms and invalid amounts', () => {
    const fixture = TestBed.createComponent(LoanFormComponent);
    fixture.detectChanges();
    const form = fixture.componentInstance.form;

    form.setValue({ amount: 5000, termMonths: 2 });
    expect(form.invalid).toBe(true);
    form.setValue({ amount: 5000, termMonths: 61 });
    expect(form.invalid).toBe(true);
    form.setValue({ amount: 5000, termMonths: 12.5 });
    expect(form.invalid).toBe(true);
    form.setValue({ amount: 99.99, termMonths: 12 });
    expect(form.invalid).toBe(true);
    form.setValue({ amount: 100.123, termMonths: 12 });
    expect(form.invalid).toBe(true);
    form.setValue({ amount: 1_000_000, termMonths: 60 });
    expect(form.valid).toBe(true);
    form.setValue({ amount: 100, termMonths: 3 });
    expect(form.valid).toBe(true);
  });

  it('prefills the loan form when editing and keeps the values after submitting', () => {
    const fixture = TestBed.createComponent(LoanFormComponent);
    fixture.componentRef.setInput('initial', { amount: 8000, termMonths: 24 });
    fixture.componentRef.setInput('cancellable', true);
    fixture.componentRef.setInput('submitLabel', 'Guardar cambios');
    const payloads: unknown[] = [];
    let cancelled = false;
    fixture.componentInstance.submitted.subscribe(payload => payloads.push(payload));
    fixture.componentInstance.cancelled.subscribe(() => cancelled = true);
    fixture.detectChanges();

    expect(fixture.componentInstance.form.value).toEqual({ amount: 8000, termMonths: 24 });
    expect(fixture.nativeElement.textContent).toContain('Guardar cambios');

    fixture.componentInstance.form.patchValue({ termMonths: 36 });
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    expect(payloads).toEqual([{ amount: 8000, termMonths: 36 }]);
    expect(fixture.componentInstance.form.value).toEqual({ amount: 8000, termMonths: 36 });

    fixture.nativeElement.querySelector('app-button button').click();
    expect(cancelled).toBe(true);
  });

  it('forwards the selected loan from the admin table', () => {
    const fixture = TestBed.createComponent(AdminLoanTableComponent);
    fixture.componentRef.setInput('loans', [pendingLoan]);
    let selected: Loan | undefined;
    let rejected: Loan | undefined;
    let removed: Loan | undefined;
    let edited: Loan | undefined;
    fixture.componentInstance.edit.subscribe(loan => edited = loan);
    fixture.componentInstance.approve.subscribe(loan => selected = loan);
    fixture.componentInstance.reject.subscribe(loan => rejected = loan);
    fixture.componentInstance.remove.subscribe(loan => removed = loan);
    fixture.detectChanges();

    fixture.nativeElement.querySelector('.approve').click();
    fixture.nativeElement.querySelector('.reject').click();
    fixture.nativeElement.querySelector('.danger').click();
    [...fixture.nativeElement.querySelectorAll('td.actions button')]
      .find((button: HTMLButtonElement) => button.textContent?.trim() === 'Editar')?.click();
    expect(edited).toEqual(pendingLoan);
    expect(selected).toEqual(pendingLoan);
    expect(rejected).toEqual(pendingLoan);
    expect(removed).toEqual(pendingLoan);
    expect(fixture.nativeElement.textContent).toContain('Usuario Makers');
    expect(fixture.nativeElement.textContent).toContain('Editar');
    expect(fixture.nativeElement.textContent).toContain('Eliminar');
  });

  it('keeps an actions cell for loans with a final status', () => {
    const fixture = TestBed.createComponent(AdminLoanTableComponent);
    fixture.componentRef.setInput('loans', [{ ...pendingLoan, status: 'APPROVED' }]);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('thead th').length).toBe(7);
    expect(fixture.nativeElement.querySelectorAll('tbody td').length).toBe(7);
    expect(fixture.nativeElement.querySelector('td.actions .action-buttons')).toBeTruthy();
    expect(fixture.nativeElement.querySelectorAll('td.actions app-button').length).toBe(2);
  });

  it('falls back to the email for legacy loan responses without a borrower name', () => {
    const fixture = TestBed.createComponent(AdminLoanTableComponent);
    fixture.componentRef.setInput('loans', [{ ...pendingLoan, borrowerName: null }]);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('user@makers.com');
  });
});
