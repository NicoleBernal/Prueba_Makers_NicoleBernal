import { ChangeDetectionStrategy, Component, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoanPayload } from '../../../models/api.models';
import { ButtonComponent } from '../../atoms/button/button.component';

/**
 * Formulario de monto y plazo. Se reutiliza para solicitar un préstamo (usuario)
 * y para editarlo (administrador, dentro de un modal), con las mismas reglas del backend:
 * monto 100–1.000.000 con máximo 2 decimales y plazo entero de 3 a 60 meses.
 */
@Component({
  selector: 'app-loan-form',
  imports: [ReactiveFormsModule, ButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section aria-labelledby="loan-form-title">
      @if (heading()) { <h2 id="loan-form-title">{{ heading() }}</h2> }
      <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
        <label for="amount">Monto
          <div class="input-prefix">
            <span aria-hidden="true">$</span>
            <input id="amount" type="number" formControlName="amount" min="100" max="1000000" step="0.01"
              [attr.aria-invalid]="invalid('amount')" aria-describedby="amount-error" />
          </div>
        </label>
        @if (invalid('amount')) { <span id="amount-error" class="field-error">El monto debe estar entre $100 y $1.000.000, con máximo 2 decimales.</span> }
        <label for="term">Plazo (meses)
          <input id="term" type="number" formControlName="termMonths" min="3" max="60" step="1"
            [attr.aria-invalid]="invalid('termMonths')" aria-describedby="term-error" />
        </label>
        @if (invalid('termMonths')) { <span id="term-error" class="field-error">El plazo debe ser un número entero de 3 a 60 meses.</span> }
        <div class="form-actions">
          @if (cancellable()) { <app-button (pressed)="cancelled.emit()">Cancelar</app-button> }
          <app-button type="submit" variant="primary" [disabled]="form.invalid || loading()">{{ submitLabel() }}</app-button>
        </div>
      </form>
    </section>
  `
})
export class LoanFormComponent {
  private readonly fb = inject(FormBuilder);
  readonly heading = input('Solicitar préstamo');
  readonly submitLabel = input('Enviar solicitud');
  readonly initial = input<LoanPayload | null>(null);
  readonly cancellable = input(false);
  readonly loading = input(false);
  readonly submitted = output<LoanPayload>();
  readonly cancelled = output<void>();

  readonly form = this.fb.group({
    amount: [null as number | null, [
      Validators.required, Validators.min(100), Validators.max(1_000_000), Validators.pattern(/^\d+(\.\d{1,2})?$/)
    ]],
    termMonths: [null as number | null, [
      Validators.required, Validators.min(3), Validators.max(60), Validators.pattern(/^\d+$/)
    ]]
  });

  constructor() {
    effect(() => {
      const initial = this.initial();
      this.form.reset({ amount: initial?.amount ?? null, termMonths: initial?.termMonths ?? null });
    });
  }

  protected invalid(name: 'amount' | 'termMonths'): boolean {
    const control = this.form.controls[name];
    return control.invalid && (control.touched || control.dirty);
  }

  protected submit(): void {
    if (this.form.invalid) return this.form.markAllAsTouched();
    this.submitted.emit(this.form.getRawValue() as LoanPayload);
    if (!this.initial()) this.form.reset();
  }
}
