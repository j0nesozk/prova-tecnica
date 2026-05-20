import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';

import { StudentService } from '../../core/student.service';
import { Student } from '../../core/models';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog.component';

@Component({
  selector: 'app-student-list',
  standalone: true,
  imports: [
    CommonModule, RouterLink, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule,
    MatPaginatorModule, MatSlideToggleModule, MatProgressBarModule
  ],
  template: `
    <div class="page-header">
      <h1>Estudantes</h1>
      <div>
        <mat-slide-toggle [(ngModel)]="includeDisabled" (change)="load()">
          Incluir desabilitados
        </mat-slide-toggle>
        <a mat-flat-button color="primary" routerLink="/students/new" style="margin-left: 16px;">
          <mat-icon>add</mat-icon>
          Novo estudante
        </a>
      </div>
    </div>

    <div class="card">
      @if (loading()) {
        <mat-progress-bar mode="indeterminate"></mat-progress-bar>
      }

      @if (data().length === 0 && !loading()) {
        <div class="empty-state">
          <mat-icon style="font-size: 48px; width: 48px; height: 48px; opacity: 0.4;">inbox</mat-icon>
          <p>Nenhum estudante cadastrado.</p>
        </div>
      } @else {
        <table mat-table [dataSource]="data()">
          <ng-container matColumnDef="photo">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let s">
              @if (s.photo) {
                <img [src]="s.photo" alt="foto" class="avatar">
              } @else {
                <div class="avatar avatar-placeholder">
                  <mat-icon>person</mat-icon>
                </div>
              }
            </td>
          </ng-container>
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Nome</th>
            <td mat-cell *matCellDef="let s">{{ s.name }}</td>
          </ng-container>
          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>E-mail</th>
            <td mat-cell *matCellDef="let s">{{ s.emailAddress }}</td>
          </ng-container>
          <ng-container matColumnDef="studentNumber">
            <th mat-header-cell *matHeaderCellDef>Matrícula</th>
            <td mat-cell *matCellDef="let s">{{ s.studentNumber }}</td>
          </ng-container>
          <ng-container matColumnDef="phone">
            <th mat-header-cell *matHeaderCellDef>Telefone</th>
            <td mat-cell *matCellDef="let s">{{ s.phoneNumber }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let s">
              <span class="status-chip" [class.status-active]="s.status === 'ACTIVE'" [class.status-disable]="s.status === 'DISABLE'">
                {{ s.status === 'ACTIVE' ? 'Ativo' : 'Desabilitado' }}
              </span>
            </td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef class="actions-cell">Ações</th>
            <td mat-cell *matCellDef="let s" class="actions-cell">
              <a mat-icon-button [routerLink]="['/students', s.id]" aria-label="Editar">
                <mat-icon>edit</mat-icon>
              </a>
              @if (s.status === 'ACTIVE') {
                <button mat-icon-button color="warn" (click)="onDelete(s)" aria-label="Desabilitar">
                  <mat-icon>block</mat-icon>
                </button>
              } @else {
                <button mat-icon-button color="primary" (click)="onRestore(s)" aria-label="Restaurar">
                  <mat-icon>restore</mat-icon>
                </button>
              }
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns;"></tr>
        </table>

        <mat-paginator
          [length]="totalElements()"
          [pageSize]="pageSize()"
          [pageIndex]="pageIndex()"
          [pageSizeOptions]="[5, 10, 25, 50]"
          (page)="onPage($event)"
          aria-label="Paginação">
        </mat-paginator>
      }
    </div>
  `
})
export class StudentListComponent {
  private readonly service = inject(StudentService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly columns = ['photo', 'name', 'email', 'studentNumber', 'phone', 'status', 'actions'];
  readonly data = signal<Student[]>([]);
  readonly totalElements = signal(0);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly loading = signal(false);
  includeDisabled = false;

  constructor() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.service.list(this.pageIndex(), this.pageSize(), this.includeDisabled).subscribe({
      next: page => {
        this.data.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onPage(event: PageEvent) {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  onDelete(s: Student) {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Desabilitar estudante',
        message: `Tem certeza que deseja desabilitar "${s.name}"? Os dados serão preservados e podem ser restaurados.`,
        confirmLabel: 'Desabilitar'
      }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.service.softDelete(s.id).subscribe(() => {
          this.snackBar.open('Estudante desabilitado', 'OK', { duration: 3000 });
          this.load();
        });
      }
    });
  }

  onRestore(s: Student) {
    this.service.restore(s.id).subscribe(() => {
      this.snackBar.open('Estudante restaurado', 'OK', { duration: 3000 });
      this.load();
    });
  }
}
