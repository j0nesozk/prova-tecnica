import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatButtonModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-toolbar color="primary">
      <mat-icon>school</mat-icon>
      <span style="margin-left: 12px; font-weight: 500;">Cadastro Acadêmico</span>
      <span class="spacer"></span>
      <a mat-button routerLink="/students" routerLinkActive="active-link">
        <mat-icon>group</mat-icon>
        Estudantes
      </a>
      <a mat-button routerLink="/professors" routerLinkActive="active-link">
        <mat-icon>school</mat-icon>
        Professores
      </a>
    </mat-toolbar>
    <main class="app-container">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .active-link {
      background-color: rgba(255, 255, 255, 0.15);
    }
    mat-toolbar a {
      margin-left: 8px;
    }
  `]
})
export class AppComponent {}
