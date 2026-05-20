import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export function emptyAddressGroup(fb: FormBuilder): FormGroup {
  return fb.group({
    id: [null as number | null],
    street: ['', [Validators.required, Validators.maxLength(200)]],
    city: ['', [Validators.required, Validators.maxLength(120)]],
    state: ['', [Validators.required, Validators.maxLength(60)]],
    zipCode: ['', [Validators.required, Validators.maxLength(20)]],
    country: ['Brasil', [Validators.required, Validators.maxLength(60)]]
  });
}

@Component({
  selector: 'app-address-form',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <div class="addresses-section">
      <div class="section-header">
        <h3>Endereços</h3>
        <button mat-stroked-button type="button" color="primary" (click)="add()">
          <mat-icon>add</mat-icon>
          Adicionar endereço
        </button>
      </div>

      @if (formArray().length === 0) {
        <div class="empty-state">Nenhum endereço cadastrado.</div>
      }

      <div [formGroup]="parentGroup()">
        <div formArrayName="addresses">
          @for (group of formArray().controls; let i = $index; track i) {
            <div class="address-card" [formGroupName]="i">
              <div class="address-header">
                <strong>Endereço {{ i + 1 }}</strong>
                <button mat-icon-button type="button" color="warn" (click)="remove(i)" aria-label="Remover">
                  <mat-icon>delete</mat-icon>
                </button>
              </div>
              <div class="address-grid">
                <mat-form-field class="span-2">
                  <mat-label>Rua / Logradouro</mat-label>
                  <input matInput formControlName="street" maxlength="200">
                </mat-form-field>
                <mat-form-field>
                  <mat-label>Cidade</mat-label>
                  <input matInput formControlName="city" maxlength="120">
                </mat-form-field>
                <mat-form-field>
                  <mat-label>Estado</mat-label>
                  <input matInput formControlName="state" maxlength="60">
                </mat-form-field>
                <mat-form-field>
                  <mat-label>CEP</mat-label>
                  <input matInput formControlName="zipCode" maxlength="9" placeholder="00000-000"
                         (input)="maskCep($event, i)">
                </mat-form-field>
                <mat-form-field>
                  <mat-label>País</mat-label>
                  <input matInput formControlName="country" maxlength="60">
                </mat-form-field>
              </div>
            </div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .addresses-section {
      margin-top: 24px;
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h3 {
        margin: 0;
        font-weight: 500;
      }
    }

    .address-card {
      background: #f5f5f5;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 12px;
    }

    .address-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }

    .address-grid {
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

    .empty-state {
      padding: 24px;
      text-align: center;
      color: rgba(0, 0, 0, 0.54);
      background: #f5f5f5;
      border-radius: 8px;
    }
  `]
})
export class AddressFormComponent {
  readonly parentGroup = input.required<FormGroup>();

  formArray(): FormArray {
    return this.parentGroup().get('addresses') as FormArray;
  }

  add() {
    const fb = new FormBuilder();
    this.formArray().push(emptyAddressGroup(fb));
  }

  remove(index: number) {
    this.formArray().removeAt(index);
  }

  maskCep(event: Event, index: number) {
    const input = event.target as HTMLInputElement;
    const d = input.value.replace(/\D/g, '').slice(0, 8);
    const formatted = d.length > 5 ? `${d.slice(0, 5)}-${d.slice(5)}` : d;
    input.value = formatted;
    (this.formArray().at(index) as FormGroup).get('zipCode')!.setValue(formatted, { emitEvent: false });
  }
}
