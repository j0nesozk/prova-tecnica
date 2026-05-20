import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Page, Student, StudentRequest } from './models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class StudentService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/students`;

  list(page = 0, size = 10, includeDisabled = false): Observable<Page<Student>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('includeDisabled', includeDisabled);
    return this.http.get<Page<Student>>(this.base, { params });
  }

  get(id: number): Observable<Student> {
    return this.http.get<Student>(`${this.base}/${id}`);
  }

  create(payload: StudentRequest): Observable<Student> {
    return this.http.post<Student>(this.base, payload);
  }

  update(id: number, payload: StudentRequest): Observable<Student> {
    return this.http.put<Student>(`${this.base}/${id}`, payload);
  }

  softDelete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  restore(id: number): Observable<Student> {
    return this.http.post<Student>(`${this.base}/${id}/restore`, {});
  }

  uploadPhoto(id: number, file: File): Observable<Student> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Student>(`${this.base}/${id}/photo`, formData);
  }

  deletePhoto(id: number): Observable<Student> {
    return this.http.delete<Student>(`${this.base}/${id}/photo`);
  }
}
