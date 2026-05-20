import { Component, inject, signal } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
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

import { ProfessorService } from '../../core/professor.service';
import { Professor } from '../../core/models';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog.component';

@Component({
  selector: 'app-professor-list',
  standalone: true,
  imports: [
    CommonModule, CurrencyPipe, RouterLink, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule,
    MatPaginatorModule, MatSlideToggleModule, MatProgressBarModule
  ],
  template: `
    <div class="page-header">
      <h1>Professores</h1>
      <div>
        <mat-slide-toggle [(ngModel)]="includeDisabled" (change)="load()">
          Incluir desabilitados
        </mat-slide-toggle>
        <a mat-flat-button color="primary" routerLink="/professors/new" style="margin-left: 16px;">
          <mat-icon>add</mat-icon>
          Novo professor
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
          <p>Nenhum professor cadastrado.</p>
        </div>
      } @else {
        <table mat-table [dataSource]="data()">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Nome</th>
            <td mat-cell *matCellDef="let p">{{ p.name }}</td>
          </ng-container>
          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>E-mail</th>
            <td mat-cell *matCellDef="let p">{{ p.emailAddress }}</td>
          </ng-container>
          <ng-container matColumnDef="phone">
            <th mat-header-cell *matHeaderCellDef>Telefone</th>
            <td mat-cell *matCellDef="let p">{{ p.phoneNumber }}</td>
          </ng-container>
          <ng-container matColumnDef="salary">
            <th mat-header-cell *matHeaderCellDef>Salário</th>
            <td mat-cell *matCellDef="let p">{{ p.salary | currency:'BRL':'symbol':'1.2-2' }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let p">
              <span class="status-chip" [class.status-active]="p.status === 'ACTIVE'" [class.status-disable]="p.status === 'DISABLE'">
                {{ p.status === 'ACTIVE' ? 'Ativo' : 'Desabilitado' }}
              </span>
            </td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef class="actions-cell">Ações</th>
            <td mat-cell *matCellDef="let p" class="actions-cell">
              <a mat-icon-button [routerLink]="['/professors', p.id]" aria-label="Editar">
                <mat-icon>edit</mat-icon>
              </a>
              @if (p.status === 'ACTIVE') {
                <button mat-icon-button color="warn" (click)="onDelete(p)" aria-label="Desabilitar">
                  <mat-icon>block</mat-icon>
                </button>
              } @else {
                <button mat-icon-button color="primary" (click)="onRestore(p)" aria-label="Restaurar">
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
export class ProfessorListComponent {
  private readonly service = inject(ProfessorService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly columns = ['name', 'email', 'phone', 'salary', 'status', 'actions'];
  readonly data = signal<Professor[]>([]);
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

  onDelete(p: Professor) {
    this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Desabilitar professor',
        message: `Tem certeza que deseja desabilitar "${p.name}"? Os dados serão preservados e podem ser restaurados.`,
        confirmLabel: 'Desabilitar'
      }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.service.softDelete(p.id).subscribe(() => {
          this.snackBar.open('Professor desabilitado', 'OK', { duration: 3000 });
          this.load();
        });
      }
    });
  }

  onRestore(p: Professor) {
    this.service.restore(p.id).subscribe(() => {
      this.snackBar.open('Professor restaurado', 'OK', { duration: 3000 });
      this.load();
    });
  }
}
