import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Address } from './models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AddressService {
  private readonly http = inject(HttpClient);

  private base(personId: number): string {
    return `${environment.apiBaseUrl}/persons/${personId}/addresses`;
  }

  list(personId: number): Observable<Address[]> {
    return this.http.get<Address[]>(this.base(personId));
  }

  create(personId: number, payload: Address): Observable<Address> {
    return this.http.post<Address>(this.base(personId), payload);
  }

  update(personId: number, addressId: number, payload: Address): Observable<Address> {
    return this.http.put<Address>(`${this.base(personId)}/${addressId}`, payload);
  }

  delete(personId: number, addressId: number): Observable<void> {
    return this.http.delete<void>(`${this.base(personId)}/${addressId}`);
  }
}
