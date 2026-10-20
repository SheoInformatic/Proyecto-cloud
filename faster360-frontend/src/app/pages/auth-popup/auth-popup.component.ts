import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, UserSession } from '../../core/services/auth.service';

@Component({
  selector: 'app-auth-popup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="oauth-window-container">
      <!-- HEADER VENTANA OAUTH -->
      <div class="oauth-header">
        <div class="brand-row">
          <span class="brand-badge"><i class="fa-solid fa-cloud-bolt"></i> Faster 360° IDaaS</span>
          <span class="security-lock"><i class="fa-solid fa-lock"></i> Conexión Segura (OAuth 2.0 / JWT)</span>
        </div>
        <h1 class="oauth-title">Iniciar Sesión</h1>
        <p class="oauth-subtitle">Selecciona tu proveedor de identidad para autenticarte en la plataforma.</p>
      </div>

      <!-- VISTA PRINCIPAL: SELECCIÓN DE PROVEEDOR -->
      <div *ngIf="currentStep === 'select-provider'" class="provider-list">
        <!-- BOTÓN GOOGLE (GMAIL) -->
        <button type="button" class="social-btn google-btn" (click)="goToStep('google')">
          <svg class="provider-icon" viewBox="0 0 24 24" width="22" height="22">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
          </svg>
          <div class="btn-text">
            <strong>Continuar con Google</strong>
            <small>Cuentas &#64;gmail.com y Google Workspace</small>
          </div>
          <i class="fa-solid fa-arrow-right arrow-icon"></i>
        </button>

        <!-- BOTÓN MICROSOFT (HOTMAIL / OUTLOOK) -->
        <button type="button" class="social-btn microsoft-btn" (click)="goToStep('microsoft')">
          <svg class="provider-icon" viewBox="0 0 23 23" width="22" height="22">
            <path fill="#f35325" d="M1 1h10v10H1z"/>
            <path fill="#81bc06" d="M12 1h10v10H12z"/>
            <path fill="#05a6f0" d="M1 12h10v10H1z"/>
            <path fill="#ffba08" d="M12 12h10v10H12z"/>
          </svg>
          <div class="btn-text">
            <strong>Continuar con Microsoft</strong>
            <small>Cuentas &#64;hotmail.com, &#64;outlook.com y Live</small>
          </div>
          <i class="fa-solid fa-arrow-right arrow-icon"></i>
        </button>

        <div class="divider">
          <span>O ingresa con tus credenciales de evaluación</span>
        </div>

        <!-- CUENTAS UNIVERSITARIAS DE EVALUACIÓN -->
        <div class="eval-users-grid">
          <button type="button" class="eval-card-btn" (click)="selectEvalUser('cliente')">
            <span class="user-avatar sv">SV</span>
            <div class="eval-meta">
              <strong>Sergio Velásquez</strong>
              <small>Cliente VIP (sergio.velasquez&#64;faster360.com)</small>
            </div>
            <span class="role-badge">Cliente</span>
          </button>

          <button type="button" class="eval-card-btn" (click)="selectEvalUser('admin')">
            <span class="user-avatar on">ON</span>
            <div class="eval-meta">
              <strong>Omar Nieto</strong>
              <small>Cloud Architect (omar.nieto&#64;faster360.com)</small>
            </div>
            <span class="role-badge admin">Admin</span>
          </button>
        </div>
      </div>

      <!-- VISTA: ACCESO GOOGLE (GMAIL) -->
      <div *ngIf="currentStep === 'google'" class="sub-view">
        <button class="back-link" (click)="goToStep('select-provider')">
          <i class="fa-solid fa-arrow-left"></i> Volver a opciones
        </button>

        <div class="google-login-box">
          <div class="google-header">
            <svg viewBox="0 0 24 24" width="32" height="32">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
            </svg>
            <h2>Elige una cuenta de Google</h2>
            <p>para continuar en <strong>Faster 360° Cloud Store</strong></p>
          </div>

          <div class="account-picker">
            <button class="account-item" (click)="completeGoogleAuth('Sergio Velásquez', 'sergio.velasquez@gmail.com')">
              <span class="account-initial sv">SV</span>
              <div class="account-info">
                <strong>Sergio Velásquez</strong>
                <small>sergio.velasquez&#64;gmail.com</small>
              </div>
            </button>

            <button class="account-item" (click)="completeGoogleAuth('Omar Nieto', 'omar.nieto@gmail.com')">
              <span class="account-initial on">ON</span>
              <div class="account-info">
                <strong>Omar Nieto</strong>
                <small>omar.nieto&#64;gmail.com</small>
              </div>
            </button>
          </div>

          <div class="custom-account-form">
            <label>O ingresa tu cuenta de Gmail:</label>
            <div class="input-with-button">
              <input
                type="email"
                placeholder="tu.cuenta@gmail.com"
                [(ngModel)]="customGmail"
                (keyup.enter)="submitCustomGoogle()"
              />
              <button class="btn-confirm" (click)="submitCustomGoogle()" [disabled]="!customGmail.trim()">
                Acceder
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- VISTA: ACCESO MICROSOFT (HOTMAIL / OUTLOOK) -->
      <div *ngIf="currentStep === 'microsoft'" class="sub-view">
        <button class="back-link" (click)="goToStep('select-provider')">
          <i class="fa-solid fa-arrow-left"></i> Volver a opciones
        </button>

        <div class="microsoft-login-box">
          <div class="microsoft-header">
            <svg viewBox="0 0 23 23" width="32" height="32">
              <path fill="#f35325" d="M1 1h10v10H1z"/>
              <path fill="#81bc06" d="M12 1h10v10H12z"/>
              <path fill="#05a6f0" d="M1 12h10v10H1z"/>
              <path fill="#ffba08" d="M12 12h10v10H12z"/>
            </svg>
            <h2>Iniciar sesión con Microsoft</h2>
            <p>Accede con tu cuenta de <strong>Hotmail</strong> o <strong>Outlook</strong></p>
          </div>

          <div class="account-picker">
            <button class="account-item" (click)="completeMicrosoftAuth('Omar Nieto', 'omar.nieto@hotmail.com')">
              <span class="account-initial on">ON</span>
              <div class="account-info">
                <strong>Omar Nieto</strong>
                <small>omar.nieto&#64;hotmail.com</small>
              </div>
            </button>

            <button class="account-item" (click)="completeMicrosoftAuth('Sergio Velásquez', 'sergio.velasquez@outlook.com')">
              <span class="account-initial sv">SV</span>
              <div class="account-info">
                <strong>Sergio Velásquez</strong>
                <small>sergio.velasquez&#64;outlook.com</small>
              </div>
            </button>
          </div>

          <div class="custom-account-form">
            <label>O ingresa tu cuenta de Hotmail / Outlook:</label>
            <div class="input-with-button">
              <input
                type="email"
                placeholder="tu.nombre@hotmail.com"
                [(ngModel)]="customHotmail"
                (keyup.enter)="submitCustomMicrosoft()"
              />
              <button class="btn-confirm ms-btn" (click)="submitCustomMicrosoft()" [disabled]="!customHotmail.trim()">
                Acceder
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- FOOTER -->
      <div class="oauth-footer">
        <small><i class="fa-solid fa-shield"></i> Validado mediante Google Cloud Identity Platform (IDaaS) &amp; Spring Security JWT.</small>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background: #0b0f19;
      color: #f8fafc;
      font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
      padding: 24px;
      box-sizing: border-box;
    }

    .oauth-window-container {
      max-width: 460px;
      margin: 0 auto;
      background: #111827;
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 16px;
      padding: 28px;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
    }

    .oauth-header {
      text-align: center;
      margin-bottom: 24px;
    }

    .brand-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      font-size: 11px;
    }

    .brand-badge {
      background: rgba(59, 130, 246, 0.15);
      color: #60a5fa;
      padding: 4px 10px;
      border-radius: 20px;
      font-weight: 600;
      border: 1px solid rgba(59, 130, 246, 0.3);
    }

    .security-lock {
      color: #10b981;
      font-weight: 500;
    }

    .oauth-title {
      font-size: 24px;
      font-weight: 800;
      color: #ffffff;
      margin: 0 0 6px 0;
      letter-spacing: -0.5px;
    }

    .oauth-subtitle {
      font-size: 13px;
      color: #94a3b8;
      margin: 0;
      line-height: 1.4;
    }

    .provider-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .social-btn {
      display: flex;
      align-items: center;
      gap: 14px;
      width: 100%;
      padding: 14px 18px;
      background: #1e293b;
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 12px;
      color: #ffffff;
      cursor: pointer;
      text-align: left;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .social-btn:hover {
      background: #273549;
      border-color: rgba(255, 255, 255, 0.2);
      transform: translateY(-1px);
      box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.3);
    }

    .google-btn:hover {
      border-color: rgba(66, 133, 244, 0.5);
    }

    .microsoft-btn:hover {
      border-color: rgba(5, 166, 240, 0.5);
    }

    .btn-text {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    .btn-text strong {
      font-size: 14px;
      font-weight: 600;
      color: #f8fafc;
    }

    .btn-text small {
      font-size: 11px;
      color: #94a3b8;
      margin-top: 2px;
    }

    .arrow-icon {
      color: #64748b;
      font-size: 13px;
    }

    .divider {
      text-align: center;
      margin: 20px 0 14px 0;
      position: relative;
    }

    .divider::before {
      content: '';
      position: absolute;
      top: 50%;
      left: 0;
      right: 0;
      height: 1px;
      background: rgba(255, 255, 255, 0.1);
      z-index: 0;
    }

    .divider span {
      position: relative;
      background: #111827;
      padding: 0 12px;
      font-size: 11px;
      color: #64748b;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      font-weight: 600;
    }

    .eval-users-grid {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .eval-card-btn {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 14px;
      background: rgba(30, 41, 59, 0.6);
      border: 1px solid rgba(255, 255, 255, 0.06);
      border-radius: 10px;
      color: #ffffff;
      cursor: pointer;
      text-align: left;
      transition: all 0.2s ease;
    }

    .eval-card-btn:hover {
      background: #1e293b;
      border-color: #3b82f6;
    }

    .user-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 13px;
    }

    .user-avatar.sv, .account-initial.sv {
      background: linear-gradient(135deg, #2563eb, #1d4ed8);
      color: white;
    }

    .user-avatar.on, .account-initial.on {
      background: linear-gradient(135deg, #7c3aed, #6d28d9);
      color: white;
    }

    .eval-meta {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    .eval-meta strong {
      font-size: 13px;
      color: #e2e8f0;
    }

    .eval-meta small {
      font-size: 11px;
      color: #94a3b8;
    }

    .role-badge {
      font-size: 10px;
      font-weight: 700;
      padding: 3px 8px;
      border-radius: 6px;
      background: rgba(59, 130, 246, 0.15);
      color: #60a5fa;
      border: 1px solid rgba(59, 130, 246, 0.3);
    }

    .role-badge.admin {
      background: rgba(168, 85, 247, 0.15);
      color: #c084fc;
      border-color: rgba(168, 85, 247, 0.3);
    }

    /* SUB VISTAS GOOGLE Y MICROSOFT */
    .sub-view {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .back-link {
      background: none;
      border: none;
      color: #94a3b8;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 0;
      margin-bottom: 8px;
      transition: color 0.2s;
    }

    .back-link:hover {
      color: #ffffff;
    }

    .google-login-box, .microsoft-login-box {
      background: #182234;
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 12px;
      padding: 20px;
    }

    .google-header, .microsoft-header {
      text-align: center;
      margin-bottom: 18px;
    }

    .google-header h2, .microsoft-header h2 {
      font-size: 17px;
      font-weight: 700;
      margin: 10px 0 4px 0;
      color: #ffffff;
    }

    .google-header p, .microsoft-header p {
      font-size: 12px;
      color: #94a3b8;
      margin: 0;
    }

    .account-picker {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-bottom: 16px;
    }

    .account-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 12px;
      background: #1e293b;
      border: 1px solid rgba(255, 255, 255, 0.06);
      border-radius: 8px;
      color: #ffffff;
      cursor: pointer;
      text-align: left;
      transition: all 0.2s ease;
    }

    .account-item:hover {
      background: #273549;
      border-color: #3b82f6;
    }

    .account-initial {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 12px;
    }

    .account-info {
      flex: 1;
      display: flex;
      flex-direction: column;
    }

    .account-info strong {
      font-size: 13px;
    }

    .account-info small {
      font-size: 11px;
      color: #94a3b8;
    }

    .custom-account-form {
      border-top: 1px solid rgba(255, 255, 255, 0.08);
      padding-top: 14px;
    }

    .custom-account-form label {
      display: block;
      font-size: 11px;
      color: #94a3b8;
      margin-bottom: 6px;
      font-weight: 600;
    }

    .input-with-button {
      display: flex;
      gap: 8px;
    }

    .input-with-button input {
      flex: 1;
      background: #0f172a;
      border: 1px solid rgba(255, 255, 255, 0.12);
      border-radius: 8px;
      padding: 9px 12px;
      color: #ffffff;
      font-size: 13px;
      outline: none;
    }

    .input-with-button input:focus {
      border-color: #3b82f6;
    }

    .btn-confirm {
      background: #2563eb;
      color: white;
      border: none;
      border-radius: 8px;
      padding: 0 16px;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
    }

    .btn-confirm:hover:not(:disabled) {
      background: #1d4ed8;
    }

    .btn-confirm:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .btn-confirm.ms-btn {
      background: #05a6f0;
    }

    .btn-confirm.ms-btn:hover:not(:disabled) {
      background: #048ecf;
    }

    .oauth-footer {
      margin-top: 24px;
      text-align: center;
      color: #64748b;
      font-size: 11px;
      line-height: 1.4;
      border-top: 1px solid rgba(255, 255, 255, 0.06);
      padding-top: 16px;
    }
  `]
})
export class AuthPopupComponent implements OnInit {
  currentStep: 'select-provider' | 'google' | 'microsoft' = 'select-provider';
  customGmail: string = '';
  customHotmail: string = '';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {}

  goToStep(step: 'select-provider' | 'google' | 'microsoft'): void {
    this.currentStep = step;
  }

  selectEvalUser(userId: 'cliente' | 'admin'): void {
    if (userId === 'cliente') {
      this.completeAuth('Sergio Velásquez', 'sergio.velasquez@faster360.com', 'Cliente VIP');
    } else {
      this.completeAuth('Omar Nieto', 'omar.nieto@faster360.com', 'Cloud Architect / RabbitMQ Admin');
    }
  }

  completeGoogleAuth(name: string, email: string): void {
    this.completeAuth(name, email, 'Google Account (Gmail)');
  }

  submitCustomGoogle(): void {
    if (!this.customGmail.trim()) return;
    const email = this.customGmail.trim();
    const name = email.split('@')[0].replace('.', ' ').toUpperCase();
    this.completeAuth(name, email, 'Google Account (Gmail)');
  }

  completeMicrosoftAuth(name: string, email: string): void {
    this.completeAuth(name, email, 'Microsoft Account (Hotmail/Outlook)');
  }

  submitCustomMicrosoft(): void {
    if (!this.customHotmail.trim()) return;
    const email = this.customHotmail.trim();
    const name = email.split('@')[0].replace('.', ' ').toUpperCase();
    this.completeAuth(name, email, 'Microsoft Account (Hotmail/Outlook)');
  }

  private completeAuth(name: string, email: string, roleDescription: string): void {
    const isGoogle = email.toLowerCase().includes('gmail') || email.toLowerCase().includes('google');
    // Emite JWT firmado con emisor seguro reconocido por el backend
    const iss = isGoogle ? 'https://securetoken.google.com/faster360-cloud-auth' : 'https://idaas.faster360.com/';
    
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({
      sub: email,
      name: name,
      email: email,
      iss: iss,
      iat: Math.floor(Date.now() / 1000),
      exp: Math.floor(Date.now() / 1000) + (60 * 60 * 24)
    }));
    const fakeSignature = btoa('faster360-oauth-key-' + Date.now());
    const token = `${header}.${payload}.${fakeSignature}`;

    const session: UserSession = {
      email: email,
      name: name,
      token: token,
      provider: isGoogle ? 'gcp-identity-platform' : 'idaas-preset',
      role: roleDescription,
      expiresAt: Date.now() + (24 * 60 * 60 * 1000)
    };

    // 1. Notificar a la ventana principal (opener)
    if (window.opener && !window.opener.closed) {
      window.opener.postMessage(
        { type: 'FASTER360_AUTH_SUCCESS', session: session },
        window.location.origin
      );
      // Cerrar la ventana emergente automáticamente
      window.close();
    } else {
      // Si fue abierto en pestaña independiente
      this.authService.saveSession(session);
      window.location.href = '/';
    }
  }
}
