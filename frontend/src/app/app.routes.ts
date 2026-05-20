import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'students' },
  {
    path: 'students',
    loadComponent: () => import('./features/students/student-list.component').then(m => m.StudentListComponent)
  },
  {
    path: 'students/new',
    loadComponent: () => import('./features/students/student-form.component').then(m => m.StudentFormComponent)
  },
  {
    path: 'students/:id',
    loadComponent: () => import('./features/students/student-form.component').then(m => m.StudentFormComponent)
  },
  {
    path: 'professors',
    loadComponent: () => import('./features/professors/professor-list.component').then(m => m.ProfessorListComponent)
  },
  {
    path: 'professors/new',
    loadComponent: () => import('./features/professors/professor-form.component').then(m => m.ProfessorFormComponent)
  },
  {
    path: 'professors/:id',
    loadComponent: () => import('./features/professors/professor-form.component').then(m => m.ProfessorFormComponent)
  },
  { path: '**', redirectTo: 'students' }
];
