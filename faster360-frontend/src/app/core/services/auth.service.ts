import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, map, of } from 'rxjs';

export interface PredefinedUser {
  id: 'cliente' | 'admin';
  name: string;
  email: string;
  defaultPassword: string;
  role: string;
  avatar: string;
  description: string;
}

export interface UserSession {
  email: string;
  name: string;
  token: string;
  refreshToken?: string;
  localId?: string;
  provider: 'gcp-identity-platform' | 'idaas-preset';
  role?: string;
  expiresAt?: number;
}

export interface GcpAuthResponse {
  kind: string;
  localId: string;
  email: string;
  displayName?: string;
  idToken: string;
  registered?: boolean;
  refreshToken: string;
  expiresIn: string;
}

interface GcpRefreshResponse {
  access_token: string;
  expires_in: string;
  token_type: string;
  refresh_token: string;
  id_token: string;
  user_id: string;
  project_id: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<UserSession | null>(null);
  public currentUser$: Observable<UserSession | null> = this.currentUserSubject.asObservable();

  // 2 USUARIOS PREDEFINIDOS PARA EL ENTORNO FASTER360
  public readonly PREDEFINED_USERS: PredefinedUser[] = [
    {
      id: 'cliente',
      name: 'Sergio Velásquez',
      email: 'sergio.velasquez@faster360.com',
      defaultPassword: 'Password123!',
      role: 'Cliente / Comprador',
      avatar: 'SV',
      description: 'Usuario para compras, carritos y generación de pedidos en la tienda.'
    },
    {
      id: 'admin',
      name: 'Omar Nieto',
      email: 'omar.nieto@faster360.com',
      defaultPassword: 'AdminPassword123!',
      role: 'Cloud Architect / RabbitMQ Admin',
      avatar: 'ON',
      description: 'Usuario administrativo con permisos para topología y supervisión AMQP.'
    }
  ];

  // Google Cloud Identity Platform REST endpoints
  private gcpBaseUrl = 'https://identitytoolkit.googleapis.com/v1/accounts';
  private gcpSecureTokenUrl = 'https://securetoken.googleapis.com/v1/token';
  private defaultApiKey = 'AIzaSyBR-V_BPNAz7c2ak0kEvaI6ydWeZvbz4pY';

  constructor(private http: HttpClient) {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      const saved = localStorage.getItem('faster360_session');
      if (saved) {
        try {
          this.currentUserSubject.next(JSON.parse(saved));
        } catch (e) {
          localStorage.removeItem('faster360_session');
        }
      }
    }
  }

  // --- GCP IDENTITY PLATFORM / FIREBASE AUTH (IDaaS REAL) ---

  /**
   * Inicia sesión en Google Cloud Identity Platform con Email y Contraseña.
   * Endpoint: accounts:signInWithPassword?key=[API_KEY]
   */
  loginWithGcpIdentity(apiKey: string, email: string, password: string): Observable<GcpAuthResponse> {
    const key = apiKey?.trim() || this.defaultApiKey;
    const url = `${this.gcpBaseUrl}:signInWithPassword?key=${key}`;
    const body = {
      email: email.trim(),
      password: password.trim(),
      returnSecureToken: true
    };

    return this.http.post<GcpAuthResponse>(url, body).pipe(
      tap((res) => {
        const role = email.toLowerCase().includes('admin') ? 'Cloud Architect / RabbitMQ Admin' : 'Cliente / Comprador';
        const session: UserSession = {
          email: res.email,
          name: res.displayName || (email.toLowerCase().includes('admin') || email.toLowerCase().includes('omar') ? 'Omar Nieto' : 'Sergio Velásquez'),
          token: res.idToken,
          refreshToken: res.refreshToken,
          localId: res.localId,
          provider: 'gcp-identity-platform',
          role: role,
          expiresAt: Date.now() + (parseInt(res.expiresIn || '3600', 10) * 1000)
        };
        this.saveSession(session);
      })
    );
  }

  /**
   * Registra un nuevo usuario en Google Cloud Identity Platform.
   * Endpoint: accounts:signUp?key=[API_KEY]
   */
  signUpWithGcpIdentity(apiKey: string, email: string, password: string): Observable<GcpAuthResponse> {
    const key = apiKey?.trim() || this.defaultApiKey;
    const url = `${this.gcpBaseUrl}:signUp?key=${key}`;
    const body = {
      email: email.trim(),
      password: password.trim(),
      returnSecureToken: true
    };

    return this.http.post<GcpAuthResponse>(url, body).pipe(
      tap((res) => {
        const role = email.toLowerCase().includes('admin') ? 'Cloud Architect / RabbitMQ Admin' : 'Cliente / Comprador';
        const session: UserSession = {
          email: res.email,
          name: res.displayName || (email.toLowerCase().includes('admin') || email.toLowerCase().includes('omar') ? 'Omar Nieto' : 'Sergio Velásquez'),
          token: res.idToken,
          refreshToken: res.refreshToken,
          localId: res.localId,
          provider: 'gcp-identity-platform',
          role: role,
          expiresAt: Date.now() + (parseInt(res.expiresIn || '3600', 10) * 1000)
        };
        this.saveSession(session);
      })
    );
  }

  // --- RENOVACIÓN DE TOKEN (TOKEN REFRESH) ---

  /**
   * Indica si la sesión actual cuenta con un Refresh Token disponible.
   */
  hasRefreshToken(): boolean {
    const session = this.currentUserSubject.value;
    return !!(session && (session.refreshToken || session.provider === 'idaas-preset'));
  }

  /**
   * Renueva el token JWT automáticamente utilizando el endpoint de SecureToken de Google
   * Endpoint: https://securetoken.googleapis.com/v1/token?key=[API_KEY]
   */
  refreshToken(): Observable<string> {
    const current = this.currentUserSubject.value;
    if (!current) {
      return of('');
    }

    // 1. Si es GCP Identity Platform: llamado oficial a SecureToken API
    if (current.provider === 'gcp-identity-platform' && current.refreshToken) {
      const apiKey = this.getSavedApiKey() || this.defaultApiKey;
      const url = `${this.gcpSecureTokenUrl}?key=${apiKey}`;
      const payload = new HttpParams()
        .set('grant_type', 'refresh_token')
        .set('refresh_token', current.refreshToken);

      return this.http.post<GcpRefreshResponse>(url, payload.toString(), {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
      }).pipe(
        map((res) => {
          const updated: UserSession = {
            ...current,
            token: res.id_token,
            refreshToken: res.refresh_token,
            expiresAt: Date.now() + (parseInt(res.expires_in || '3600', 10) * 1000)
          };
          this.saveSession(updated);
          return res.id_token;
        })
      );
    }

    // 2. Si es preset IDaaS: re-generar JWT firmado con nuevo timestamp
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({
      sub: current.email,
      name: current.name,
      email: current.email,
      iss: 'https://idaas.faster360.com/',
      iat: Math.floor(Date.now() / 1000),
      exp: Math.floor(Date.now() / 1000) + (60 * 60 * 24)
    }));
    const fakeSignature = btoa('faster360-renewed-key-' + Date.now());
    const newToken = `${header}.${payload}.${fakeSignature}`;

    const updatedSession: UserSession = {
      ...current,
      token: newToken,
      expiresAt: Date.now() + (24 * 60 * 60 * 1000)
    };
    this.saveSession(updatedSession);
    return of(newToken);
  }

  // --- MODO DEMO / PRESETS ---

  loginWithIdaaS(email: string, name: string): void {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({
      sub: email,
      name: name,
      email: email,
      iss: 'https://idaas.faster360.com/',
      iat: Math.floor(Date.now() / 1000),
      exp: Math.floor(Date.now() / 1000) + (60 * 60 * 24)
    }));
    const fakeSignature = btoa('faster360-secure-signature-key-2026');
    const jwtToken = `${header}.${payload}.${fakeSignature}`;

    const role = email.toLowerCase().includes('admin') ? 'Cloud Architect / RabbitMQ Admin' : 'Cliente / Comprador';

    const session: UserSession = {
      email,
      name,
      token: jwtToken,
      provider: 'idaas-preset',
      role: role,
      expiresAt: Date.now() + (24 * 60 * 60 * 1000)
    };
    this.saveSession(session);
  }

  private saveSession(session: UserSession): void {
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

  getSavedApiKey(): string {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      return localStorage.getItem('faster360_gcp_api_key') || this.defaultApiKey;
    }
    return this.defaultApiKey;
  }

  saveApiKey(apiKey: string): void {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      localStorage.setItem('faster360_gcp_api_key', apiKey.trim());
    }
  }
}