export type Status = 'ACTIVE' | 'DISABLE';

export interface Address {
  id?: number;
  street: string;
  city: string;
  state: string;
  zipCode: string;
  country: string;
}

export interface Person {
  id: number;
  name: string;
  phoneNumber: string;
  emailAddress: string;
  status: Status;
  addresses: Address[];
}

export interface Student extends Person {
  studentNumber: string;
  photo?: string | null;
}

export interface Professor extends Person {
  salary: number;
}

export interface StudentRequest {
  name: string;
  phoneNumber: string;
  emailAddress: string;
  studentNumber: string;
  photo?: string | null;
  addresses: Address[];
}

export interface ProfessorRequest {
  name: string;
  phoneNumber: string;
  emailAddress: string;
  salary: number;
  addresses: Address[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
