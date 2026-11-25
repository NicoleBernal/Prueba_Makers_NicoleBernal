import { TestBed } from '@angular/core/testing';
import { provideStore, Store } from '@ngrx/store';
import { ConfirmDialogComponent } from './organisms/confirm-dialog/confirm-dialog.component';
import { ModalComponent } from './organisms/modal/modal.component';
import { ToastHostComponent } from './organisms/toast-host/toast-host.component';
import { UserFormComponent } from './organisms/user-form/user-form.component';
import { SegmentedFilterComponent } from './molecules/segmented-filter/segmented-filter.component';
import { StatStripComponent } from './molecules/stat-strip/stat-strip.component';
import { LoanActions, loansFeature } from '../../store/loans/loans.store';
import { UserActions, usersFeature } from '../../store/users/users.store';
import { User } from '../models/api.models';

describe('SPA interaction components', () => {
  it('closes the modal with Escape and with a click on the backdrop, but not inside the panel', () => {
    const fixture = TestBed.createComponent(ModalComponent);
    fixture.componentRef.setInput('title', 'Editar');
    let closed = 0;
    fixture.componentInstance.closed.subscribe(() => closed++);
    fixture.detectChanges();

    const dialog = fixture.nativeElement.querySelector('[role="dialog"]');
    expect(dialog.getAttribute('aria-modal')).toBe('true');
    expect(dialog.getAttribute('aria-labelledby')).toBe(fixture.nativeElement.querySelector('h2').id);

    dialog.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }));
    expect(closed).toBe(0);
    fixture.nativeElement.querySelector('.modal-backdrop').dispatchEvent(new MouseEvent('mousedown'));
    expect(closed).toBe(1);
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    expect(closed).toBe(2);
  });

  it('asks for confirmation before a sensitive action', () => {
    const fixture = TestBed.createComponent(ConfirmDialogComponent);
    fixture.componentRef.setInput('title', 'Eliminar préstamo');
    fixture.componentRef.setInput('message', '¿Eliminar el préstamo #3?');
    fixture.componentRef.setInput('confirmLabel', 'Eliminar');
    fixture.componentRef.setInput('variant', 'danger');
    const events: string[] = [];
    fixture.componentInstance.confirmed.subscribe(() => events.push('confirmed'));
    fixture.componentInstance.cancelled.subscribe(() => events.push('cancelled'));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('¿Eliminar el préstamo #3?');
    fixture.nativeElement.querySelector('button.danger').click();
    const cancel = [...fixture.nativeElement.querySelectorAll('.form-actions button')][0] as HTMLButtonElement;
    cancel.click();
    expect(events).toEqual(['confirmed', 'cancelled']);
  });

  it('marks the active segment and emits the chosen value', () => {
    const fixture = TestBed.createComponent(SegmentedFilterComponent);
    fixture.componentRef.setInput('options', [
      { value: 'ALL', label: 'Todos', count: 4 },
      { value: 'PENDING', label: 'Pendientes', count: 1 }
    ]);
    fixture.componentRef.setInput('value', 'ALL');
    let chosen = '';
    fixture.componentInstance.changed.subscribe(value => chosen = value);
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('button.chip') as NodeListOf<HTMLButtonElement>;
    expect(buttons[0].getAttribute('aria-pressed')).toBe('true');
    expect(buttons[1].getAttribute('aria-pressed')).toBe('false');
    expect(buttons[1].textContent).toContain('1');
    buttons[1].click();
    expect(chosen).toBe('PENDING');
  });

  it('renders the summary counters', () => {
    const fixture = TestBed.createComponent(StatStripComponent);
    fixture.componentRef.setInput('items', [{ label: 'Pendientes', value: 2, tone: 'pending' }]);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.stat.pending strong').textContent).toBe('2');
  });

  describe('user form', () => {
    const existing: User = { id: 5, name: 'Ana', email: 'ana@test.com', role: 'USER', createdAt: '2026-01-01T00:00:00' };

    it('requires a password when creating', () => {
      const fixture = TestBed.createComponent(UserFormComponent);
      const payloads: unknown[] = [];
      fixture.componentInstance.submitted.subscribe(payload => payloads.push(payload));
      fixture.detectChanges();

      fixture.componentInstance.form.patchValue({ name: 'Nuevo', email: 'nuevo@test.com' });
      fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
      expect(payloads).toEqual([]);

      fixture.componentInstance.form.patchValue({ password: 'abc' });
      fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
      expect(payloads).toEqual([{ name: 'Nuevo', email: 'nuevo@test.com', role: 'USER', password: 'abc' }]);
    });

    it('keeps the current password when editing with an empty password', () => {
      const fixture = TestBed.createComponent(UserFormComponent);
      fixture.componentRef.setInput('user', existing);
      const payloads: { password?: string }[] = [];
      fixture.componentInstance.submitted.subscribe(payload => payloads.push(payload));
      fixture.detectChanges();

      expect(fixture.componentInstance.form.value).toMatchObject({ name: 'Ana', email: 'ana@test.com' });
      fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
      expect(payloads.length).toBe(1);
      expect(payloads[0].password).toBeUndefined();
    });
  });

  it('turns store feedback into toasts and consumes the message', () => {
    TestBed.configureTestingModule({
      providers: [provideStore({ [loansFeature.name]: loansFeature.reducer, [usersFeature.name]: usersFeature.reducer })]
    });
    const store = TestBed.inject(Store);
    const fixture = TestBed.createComponent(ToastHostComponent);
    fixture.detectChanges();

    store.dispatch(LoanActions.updateSuccess({
      loan: { id: 1, amount: 500, termMonths: 6, status: 'PENDING', userEmail: 'a@b.c', createdAt: '', updatedAt: '' }
    }));
    store.dispatch(UserActions.failure({ error: 'Ya existe un usuario con ese correo' }));
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Préstamo actualizado.');
    expect(text).toContain('Ya existe un usuario con ese correo');
    expect(fixture.nativeElement.querySelector('.toast.error')).toBeTruthy();

    let state: { loans: { confirmation: string | null }; users: { error: string | null } } | undefined;
    store.subscribe(value => state = value as typeof state).unsubscribe();
    expect(state?.loans.confirmation).toBeNull();
    expect(state?.users.error).toBeNull();
  });
});
