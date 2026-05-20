import { Component, inject, OnInit, signal, input, numberAttribute } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ProfessorService } from '../../core/professor.service';
import { ProfessorRequest } from '../../core/models';
import { AddressFormComponent, emptyAddressGroup } from '../../shared/address-form.component';

@Component({
  selector: 'app-professor-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatProgressBarModule,
    AddressFormComponent
  ],
  template: `
    <div class="page-header">
      <h1>{{ id() ? 'Editar professor' : 'Novo professor' }}</h1>
      <button mat-stroked-button (click)="cancel()">
        <mat-icon>arrow_back</mat-icon>
        Voltar
      </button>
    </div>

    @if (loading()) {
      <mat-progress-bar mode="indeterminate"></mat-progress-bar>
    }

    <form [formGroup]="form" (ngSubmit)="onSubmit()" class="card">
      <div class="grid">
        <mat-form-field class="span-2">
          <mat-label>Nome completo</mat-label>
          <input matInput formControlName="name" maxlength="120" required>
          @if (form.get('name')?.hasError('required') && form.get('name')?.touched) {
            <mat-error>Nome é obrigatório</mat-error>
          }
        </mat-form-field>

        <mat-form-field>
          <mat-label>Telefone</mat-label>
          <input matInput formControlName="phoneNumber" maxlength="15" placeholder="(00) 00000-0000"
                 (input)="maskPhone($event)">
        </mat-form-field>

        <mat-form-field>
          <mat-label>E-mail</mat-label>
          <input matInput type="email" formControlName="emailAddress" maxlength="180" required>
          @if (form.get('emailAddress')?.hasError('email')) {
            <mat-error>E-mail inválido</mat-error>
          }
        </mat-form-field>

        <mat-form-field>
          <mat-label>Salário</mat-label>
          <span matTextPrefix>R$&nbsp;</span>
          <input matInput inputmode="decimal"
                 [value]="salaryDisplay()"
                 (focus)="onSalaryFocus($event)"
                 (blur)="onSalaryBlur($event)"
                 (input)="onSalaryInput($event)"
                 required>
          @if (form.get('salary')?.hasError('min') && form.get('salary')?.touched) {
            <mat-error>Salário não pode ser negativo</mat-error>
          }
          @if (form.get('salary')?.hasError('required') && form.get('salary')?.touched) {
            <mat-error>Salário é obrigatório</mat-error>
          }
        </mat-form-field>
      </div>

      <app-address-form [parentGroup]="form"></app-address-form>

      <div class="form-actions">
        <button mat-button type="button" (click)="cancel()">Cancelar</button>
        <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || saving()">
          {{ id() ? 'Salvar alterações' : 'Cadastrar' }}
        </button>
      </div>
    </form>
  `,
  styles: [`
    .grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
    }
    .span-2 {
      grid-column: span 2;
    }
    mat-form-field {
      width: 100%;
    }
    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid rgba(0, 0, 0, 0.08);
    }
  `]
})
export class ProfessorFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ProfessorService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly id = input<number | undefined, string | undefined>(undefined, { transform: (v) => v ? numberAttribute(v) : undefined });
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly salaryDisplay = signal('0,00');

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    phoneNumber: ['', [Validators.required, Validators.maxLength(30)]],
    emailAddress: ['', [Validators.required, Validators.email, Validators.maxLength(180)]],
    salary: [null as number | null, [Validators.required, Validators.min(0)]],
    addresses: this.fb.array([])
  });

  ngOnInit() {
    const id = this.id();
    if (id !== undefined && !Number.isNaN(id)) {
      this.loading.set(true);
      this.service.get(id).subscribe({
        next: p => {
          this.form.patchValue({
            name: p.name,
            phoneNumber: p.phoneNumber,
            emailAddress: p.emailAddress,
            salary: p.salary
          });
          this.salaryDisplay.set(this.formatBRL(p.salary ?? 0));
          const fa = this.form.get('addresses') as FormArray;
          for (const a of p.addresses) {
            const g = emptyAddressGroup(this.fb);
            g.patchValue(a);
            fa.push(g);
          }
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
    }
  }

  maskPhone(event: Event) {
    const input = event.target as HTMLInputElement;
    const d = input.value.replace(/\D/g, '').slice(0, 11);
    let formatted = d;
    if (d.length > 6) {
      formatted = d.length > 10
        ? `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`
        : `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
    } else if (d.length > 2) {
      formatted = `(${d.slice(0, 2)}) ${d.slice(2)}`;
    } else if (d.length > 0) {
      formatted = `(${d}`;
    }
    input.value = formatted;
    this.form.get('phoneNumber')!.setValue(formatted, { emitEvent: false });
  }

  onSalaryFocus(event: FocusEvent) {
    const raw = this.form.get('salary')?.value ?? 0;
    const input = event.target as HTMLInputElement;
    input.value = raw === 0 ? '' : String(raw).replace('.', ',');
    input.select();
  }

  onSalaryBlur(event: FocusEvent) {
    const input = event.target as HTMLInputElement;
    const parsed = this.parseBRL(input.value);
    this.form.get('salary')!.setValue(parsed);
    this.form.get('salary')!.markAsTouched();
    this.salaryDisplay.set(this.formatBRL(parsed));
    input.value = this.formatBRL(parsed);
  }

  onSalaryInput(event: Event) {
    const input = event.target as HTMLInputElement;
    const parsed = this.parseBRL(input.value);
    this.form.get('salary')!.setValue(parsed, { emitEvent: false });
  }

  private formatBRL(value: number): string {
    return new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value);
  }

  private parseBRL(raw: string): number {
    const cleaned = raw.replace(/[^\d,]/g, '').replace(',', '.');
    const num = parseFloat(cleaned);
    return isNaN(num) ? 0 : num;
  }

  onSubmit() {
    if (this.form.invalid) return;
    const payload = this.form.getRawValue() as ProfessorRequest;
    this.saving.set(true);

    const id = this.id();
    const obs = id !== undefined ? this.service.update(id, payload) : this.service.create(payload);
    obs.subscribe({
      next: () => {
        this.saving.set(false);
        this.snackBar.open(id !== undefined ? 'Professor atualizado' : 'Professor criado', 'OK', { duration: 3000 });
        this.router.navigate(['/professors']);
      },
      error: () => this.saving.set(false)
    });
  }

  cancel() {
    this.router.navigate(['/professors']);
  }
}
