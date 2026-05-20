import { ChangeDetectionStrategy, Component, inject, OnInit, signal, input, numberAttribute } from '@angular/core';
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
import { switchMap, of } from 'rxjs';

import { StudentService } from '../../core/student.service';
import { Student, StudentRequest } from '../../core/models';
import { AddressFormComponent, emptyAddressGroup } from '../../shared/address-form.component';

@Component({
  selector: 'app-student-form',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatProgressBarModule,
    AddressFormComponent
  ],
  template: `
    <div class="page-header">
      <h1>{{ id() ? 'Editar estudante' : 'Novo estudante' }}</h1>
      <button mat-stroked-button (click)="cancel()">
        <mat-icon>arrow_back</mat-icon>
        Voltar
      </button>
    </div>

    @if (loading()) {
      <mat-progress-bar mode="indeterminate"></mat-progress-bar>
    }

    <form [formGroup]="form" (ngSubmit)="onSubmit()" class="card">
      <div class="photo-row">
        <div class="photo-preview">
          @if (previewUrl()) {
            <img [src]="previewUrl()!" alt="Foto do estudante">
          } @else {
            <div class="placeholder">
              <mat-icon>person</mat-icon>
              <span>Sem foto</span>
            </div>
          }
        </div>
        <div class="photo-controls">
          <input #fileInput type="file" accept="image/jpeg,image/png,image/webp,image/gif"
                 (change)="onFileSelected($event)" hidden>
          <button mat-stroked-button type="button" color="primary" (click)="fileInput.click()">
            <mat-icon>upload</mat-icon>
            {{ pendingFile() ? 'Trocar foto selecionada' : (currentPhoto() ? 'Substituir foto' : 'Escolher foto') }}
          </button>
          @if (pendingFile()) {
            <button mat-button type="button" (click)="clearPendingFile()">
              <mat-icon>close</mat-icon>
              Cancelar nova foto
            </button>
            <small>{{ pendingFile()!.name }} ({{ (pendingFile()!.size / 1024) | number:'1.0-0' }} KB)</small>
          } @else if (currentPhoto() && id()) {
            <button mat-button color="warn" type="button" (click)="removeCurrentPhoto()" [disabled]="saving()">
              <mat-icon>delete</mat-icon>
              Remover foto
            </button>
          }
          <small class="hint">JPG, PNG, WEBP ou GIF — até 5 MB.</small>
        </div>
      </div>

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

        <mat-form-field class="span-2">
          <mat-label>Matrícula</mat-label>
          <input matInput formControlName="studentNumber" maxlength="60" required>
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

    .photo-row {
      display: flex;
      gap: 24px;
      align-items: flex-start;
      margin-bottom: 24px;
      padding-bottom: 24px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.08);
    }

    .photo-preview {
      width: 140px;
      height: 140px;
      border-radius: 12px;
      overflow: hidden;
      background: #f0f0f0;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        color: rgba(0, 0, 0, 0.4);

        mat-icon {
          font-size: 48px;
          width: 48px;
          height: 48px;
        }

        span {
          font-size: 12px;
        }
      }
    }

    .photo-controls {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
      padding-top: 12px;

      small {
        color: rgba(0, 0, 0, 0.6);
        font-size: 12px;
      }

      .hint {
        margin-top: 4px;
        color: rgba(0, 0, 0, 0.45);
      }
    }
  `]
})
export class StudentFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(StudentService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly id = input<number | undefined, string | undefined>(undefined, { transform: (v) => v ? numberAttribute(v) : undefined });
  readonly loading = signal(false);
  readonly saving = signal(false);

  readonly currentPhoto = signal<string | null>(null);
  readonly pendingFile = signal<File | null>(null);
  readonly previewUrl = signal<string | null>(null);

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    phoneNumber: ['', [Validators.required, Validators.maxLength(30)]],
    emailAddress: ['', [Validators.required, Validators.email, Validators.maxLength(180)]],
    studentNumber: ['', [Validators.required, Validators.maxLength(60)]],
    addresses: this.fb.array([])
  });

  ngOnInit() {
    const id = this.id();
    if (id !== undefined && !Number.isNaN(id)) {
      this.loading.set(true);
      this.service.get(id).subscribe({
        next: s => {
          this.form.patchValue({
            name: s.name,
            phoneNumber: s.phoneNumber,
            emailAddress: s.emailAddress,
            studentNumber: s.studentNumber
          });
          const fa = this.form.get('addresses') as FormArray;
          for (const a of s.addresses) {
            const g = emptyAddressGroup(this.fb);
            g.patchValue(a);
            fa.push(g);
          }
          this.currentPhoto.set(s.photo ?? null);
          this.previewUrl.set(s.photo ?? null);
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

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (file.size > 5 * 1024 * 1024) {
      this.snackBar.open('Arquivo excede 5 MB', 'OK', { duration: 4000 });
      input.value = '';
      return;
    }
    this.pendingFile.set(file);
    const reader = new FileReader();
    reader.onload = () => this.previewUrl.set(reader.result as string);
    reader.readAsDataURL(file);
    input.value = '';
  }

  clearPendingFile() {
    this.pendingFile.set(null);
    this.previewUrl.set(this.currentPhoto());
  }

  removeCurrentPhoto() {
    const id = this.id();
    if (id === undefined) return;
    this.saving.set(true);
    this.service.deletePhoto(id).subscribe({
      next: s => {
        this.currentPhoto.set(s.photo ?? null);
        this.previewUrl.set(null);
        this.pendingFile.set(null);
        this.saving.set(false);
        this.snackBar.open('Foto removida', 'OK', { duration: 3000 });
      },
      error: () => this.saving.set(false)
    });
  }

  onSubmit() {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    const payload: StudentRequest = {
      name: raw.name!,
      phoneNumber: raw.phoneNumber!,
      emailAddress: raw.emailAddress!,
      studentNumber: raw.studentNumber!,
      photo: this.currentPhoto(),
      addresses: (raw.addresses ?? []) as StudentRequest['addresses']
    };
    this.saving.set(true);

    const id = this.id();
    const save$ = id !== undefined
      ? this.service.update(id, payload)
      : this.service.create(payload);

    save$.pipe(
      switchMap((saved: Student) => {
        const file = this.pendingFile();
        if (file) {
          return this.service.uploadPhoto(saved.id, file);
        }
        return of(saved);
      })
    ).subscribe({
      next: () => {
        this.saving.set(false);
        this.snackBar.open(id !== undefined ? 'Estudante atualizado' : 'Estudante criado', 'OK', { duration: 3000 });
        this.router.navigate(['/students']);
      },
      error: () => this.saving.set(false)
    });
  }

  cancel() {
    this.router.navigate(['/students']);
  }
}
