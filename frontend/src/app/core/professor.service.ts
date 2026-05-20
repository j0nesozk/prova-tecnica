import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Page, Professor, ProfessorRequest } from './models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProfessorService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/professors`;

  list(page = 0, size = 10, includeDisabled = false): Observable<Page<Professor>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('includeDisabled', includeDisabled);
    return this.http.get<Page<Professor>>(this.base, { params });
  }

  get(id: number): Observable<Professor> {
    return this.http.get<Professor>(`${this.base}/${id}`);
  }

  create(payload: ProfessorRequest): Observable<Professor> {
    return this.http.post<Professor>(this.base, payload);
  }

  update(id: number, payload: ProfessorRequest): Observable<Professor> {
    return this.http.put<Professor>(`${this.base}/${id}`, payload);
  }

  softDelete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  restore(id: number): Observable<Professor> {
    return this.http.post<Professor>(`${this.base}/${id}/restore`, {});
  }
}
