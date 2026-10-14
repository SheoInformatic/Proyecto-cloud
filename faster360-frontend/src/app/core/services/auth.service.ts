import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface UserSession {
  email: string;
  name: string;
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<UserSession | null>(null);
  public currentUser$: Observable<UserSession | null> = this.currentUserSubject.asObservable();

  constructor() {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      const saved = localStorage.getItem('faster360_session');
      if (saved) {
        this.currentUserSubject.next(JSON.parse(saved));
      }
    }
  }

  loginWithIdaaS(email: string, name: string): void {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({
      sub: email,
      name: name,
      iss: 'https://idaas.faster360.com/',
      exp: Math.floor(Date.now() / 1000) + (60 * 60 * 24)
    }));
    const fakeSignature = btoa('faster360-secure-signature-key-2026');
    const jwtToken = `${header}.${payload}.${fakeSignature}`;

    const session: UserSession = { email, name, token: jwtToken };

    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      localStorage.setItem('faster360_session', JSON.stringify(session));
    }
    this.currentUserSubject.next(session);
  }

  logout(): void {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      localStorage.removeItem('faster360_session');
    }
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return this.currentUserSubject.value ? this.currentUserSubject.value.token : null;
  }

  isAuthenticated(): boolean {
    return this.currentUserSubject.value !== null;
  }
}