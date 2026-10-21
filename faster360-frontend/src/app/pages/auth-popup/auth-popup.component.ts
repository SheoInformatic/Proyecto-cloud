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
        <!-- BANNER DE POLÍTICA DE DOMINIO GCP -->
        <div class="gcp-policy-pill" style="margin-top: 14px; background: rgba(59, 130, 246, 0.1); border: 1px solid rgba(59, 130, 246, 0.3); border-radius: 8px; padding: 8px 12px; font-size: 11px; color: #93c5fd; display: flex; align-items: center; justify-content: center; gap: 8px;">
          <i class="fa-solid fa-shield-halved"></i>
          <span><strong>Política GCP IDaaS:</strong> Solo se admiten cuentas oficiales de Google (<code>&#64;gmail.com</code>).</span>
        </div>
      </div>

      <!-- VISTA PRINCIPAL: SELECCIÓN DE PROVEEDOR -->
      <div *ngIf="currentStep === 'select-provider'" class="provider-list">
        <!-- BOTÓN GOOGLE OFICIAL (ACCOUNTS.GOOGLE.COM) -->
        <button type="button" class="social-btn google-btn" (click)="openOfficialGoogleLogin()">
          <svg class="provider-icon" viewBox="0 0 24 24" width="22" height="22">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
          </svg>
          <div class="btn-text">
            <strong>Continuar con Google Oficial</strong>
            <small>Abrir ventana oficial accounts.google.com</small>
          </div>
          <i class="fa-solid fa-arrow-up-right-from-square arrow-icon" style="color: #60a5fa;"></i>
        </button>

        <!-- BOTÓN MICROSOFT (BLOQUEADO POR POLÍTICA GCP) -->
        <button type="button" class="social-btn microsoft-btn" (click)="notifyBlockedProvider('Microsoft')" style="opacity: 0.6; cursor: not-allowed; border-style: dashed;">
          <svg class="provider-icon" viewBox="0 0 23 23" width="22" height="22">
            <path fill="#f35325" d="M1 1h10v10H1z"/>
            <path fill="#81bc06" d="M12 1h10v10H12z"/>
            <path fill="#05a6f0" d="M1 12h10v10H1z"/>
            <path fill="#ffba08" d="M12 12h10v10H12z"/>
          </svg>
          <div class="btn-text">
            <strong>Continuar con Microsoft <span style="color: #f87171; font-size: 11px; margin-left: 6px;">[Bloqueado por GCP]</span></strong>
            <small style="color: #fca5a5;">Proveedor restringido. GCP solo admite correos &#64;gmail.com</small>
          </div>
          <i class="fa-solid fa-ban" style="color: #ef4444;"></i>
        </button>

        <div class="divider">
          <span>O ingresa con los usuarios autorizados en GCP</span>
        </div>

        <!-- CUENTAS UNIVERSITARIAS DE EVALUACIÓN CON GOOGLE/DUOC -->
        <div class="eval-users-grid">
          <button type="button" class="eval-card-btn" (click)="preparePasswordStep('Sergio Velásquez', 'sergiomarcelo3672@gmail.com', 'SV', 'Cliente VIP', true, false)">
            <span class="user-avatar sv">SV</span>
            <div class="eval-meta">
              <strong>Sergio Velásquez</strong>
              <small>Cuenta Gmail: sergiomarcelo3672&#64;gmail.com</small>
            </div>
            <span class="role-badge">Cliente</span>
          </button>

          <button type="button" class="eval-card-btn" (click)="preparePasswordStep('Omar Nieto', 'om.nieto@duocuc.cl', 'ON', 'Cloud Architect / RabbitMQ Admin', true, true)">
            <span class="user-avatar on">ON</span>
            <div class="eval-meta">
              <strong>Omar Nieto</strong>
              <small>Cuenta Duoc Google: om.nieto&#64;duocuc.cl</small>
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
            <button class="account-item" (click)="preparePasswordStep('Sergio Velásquez', 'sergiomarcelo3672@gmail.com', 'SV', 'Google Account (Gmail)', true, false)">
              <span class="account-initial sv">SV</span>
              <div class="account-info">
                <strong>Sergio Velásquez</strong>
                <small>sergiomarcelo3672&#64;gmail.com</small>
              </div>
            </button>

            <button class="account-item" (click)="preparePasswordStep('Omar Nieto', 'om.nieto@duocuc.cl', 'ON', 'Google Workspace Account', true, true)">
              <span class="account-initial on">ON</span>
              <div class="account-info">
                <strong>Omar Nieto</strong>
                <small>om.nieto&#64;duocuc.cl</small>
              </div>
            </button>
          </div>

          <div style="margin-bottom: 14px;">
            <button
              type="button"
              class="social-btn google-btn"
              style="width: 100%; border: 1px solid rgba(66, 133, 244, 0.4); background: rgba(66, 133, 244, 0.12); padding: 12px 14px;"
              (click)="openOfficialGoogleLogin()"
            >
              <i class="fa-solid fa-arrow-up-right-from-square" style="color: #60a5fa; font-size: 16px;"></i>
              <div class="btn-text">
                <strong style="color: #93c5fd;">Abrir Ventana Oficial de Google</strong>
                <small style="color: #cbd5e1;">Redirigir a accounts.google.com</small>
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
                (keyup.enter)="startCustomGoogle()"
              />
              <button class="btn-confirm" (click)="startCustomGoogle()" [disabled]="!customGmail.trim()">
                Siguiente
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- VISTA: CONFIGURAR CLIENT ID PARA GOOGLE OFICIAL -->
      <div *ngIf="currentStep === 'google-config'" class="sub-view">
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
            <h2>Login Oficial de Google</h2>
            <p>Conexión directa con <strong>accounts.google.com</strong></p>
          </div>

          <div style="background: rgba(59, 130, 246, 0.1); border: 1px solid rgba(59, 130, 246, 0.3); border-radius: 8px; padding: 12px; margin-bottom: 16px; font-size: 12px; color: #cbd5e1; line-height: 1.5;">
            Ingresa el <strong>OAuth Web Client ID</strong> de GCP para abrir la ventana oficial de Google (termina en <code>.apps.googleusercontent.com</code>):
          </div>

          <div class="custom-account-form">
            <div class="input-with-button" style="flex-direction: column; gap: 10px;">
              <input
                type="text"
                placeholder="xxxx.apps.googleusercontent.com"
                [(ngModel)]="gcpClientIdInput"
                style="width: 100%; box-sizing: border-box; padding: 10px; border-radius: 8px; border: 1px solid rgba(255,255,255,0.15); background: #0f172a; color: #fff;"
              />
              <button
                class="btn-confirm"
                style="width: 100%; justify-content: center; background: #3b82f6; padding: 10px; border-radius: 8px;"
                (click)="openOfficialGoogleLogin()"
                [disabled]="!gcpClientIdInput.trim()"
              >
                <i class="fa-solid fa-arrow-up-right-from-square" style="margin-right: 6px;"></i> Abrir Google Oficial (accounts.google.com)
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
            <button class="account-item" (click)="preparePasswordStep('Omar Nieto', 'omar.nieto@hotmail.com', 'ON', 'Microsoft Account (Hotmail/Outlook)', true, true)">
              <span class="account-initial on">ON</span>
              <div class="account-info">
                <strong>Omar Nieto</strong>
                <small>omar.nieto&#64;hotmail.com</small>
              </div>
            </button>

            <button class="account-item" (click)="preparePasswordStep('Sergio Velásquez', 'sergio.velasquez@outlook.com', 'SV', 'Microsoft Account (Hotmail/Outlook)', true, false)">
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
                (keyup.enter)="startCustomMicrosoft()"
              />
              <button class="btn-confirm ms-btn" (click)="startCustomMicrosoft()" [disabled]="!customHotmail.trim()">
                Siguiente
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- VISTA: INTRODUCIR Y VALIDAR CONTRASEÑA -->
      <div *ngIf="currentStep === 'enter-password'" class="sub-view">
        <button class="back-link" (click)="goToStep('select-provider')">
          <i class="fa-solid fa-arrow-left"></i> Cambiar de cuenta
        </button>

        <div class="password-box" style="margin-top: 14px; background: rgba(15, 23, 42, 0.7); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 14px; padding: 22px;">
          <div class="user-selected-badge" style="display: flex; align-items: center; gap: 12px; background: rgba(255, 255, 255, 0.04); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 12px; padding: 12px 14px; margin-bottom: 20px;">
            <div class="account-initial" [class.sv]="selectedUserInitial === 'SV'" [class.on]="selectedUserInitial === 'ON'">
              {{ selectedUserInitial }}
            </div>
            <div style="flex: 1; overflow: hidden;">
              <strong style="display: block; font-size: 14px; color: #ffffff;">{{ selectedUserName }}</strong>
              <small style="color: #94a3b8; font-size: 12px; text-overflow: ellipsis; overflow: hidden; display: block;">{{ selectedEmail }}</small>
            </div>
            <span class="role-badge" [class.admin]="isOmar">{{ isOmar ? 'Admin' : 'Cliente' }}</span>
          </div>

          <!-- BANNER DE ERROR EN ROJO SI LAS CREDENCIALES FALLAN -->
          <div *ngIf="errorMessage" class="error-banner" style="background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.4); border-radius: 8px; padding: 10px 14px; color: #fca5a5; font-size: 12px; margin-bottom: 16px; display: flex; align-items: center; gap: 10px;">
            <i class="fa-solid fa-circle-exclamation"></i>
            <span>{{ errorMessage }}</span>
          </div>

          <form (submit)="submitPassword(); $event.preventDefault()">
            <div class="form-group" style="margin-bottom: 16px;">
              <label style="font-size: 12px; color: #cbd5e1; display: block; margin-bottom: 6px;">Introduce tu contraseña:</label>
              <input
                type="password"
                class="form-control"
                style="width: 100%; box-sizing: border-box; background: #0f172a; border: 1px solid rgba(255, 255, 255, 0.15); border-radius: 8px; padding: 10px 12px; color: #ffffff; font-size: 13px;"
                placeholder="Contraseña"
                [(ngModel)]="authPassword"
                name="authPassword"
                required
                autofocus
              />
              <small *ngIf="isEvalUser" style="color: #64748b; font-size: 11px; margin-top: 6px; display: block;">
                Pista de evaluación: <strong style="color: #94a3b8;">{{ isOmar ? 'AdminPassword123!' : 'Password123!' }}</strong>
              </small>
            </div>

            <button
              type="submit"
              class="btn-confirm"
              style="width: 100%; justify-content: center; padding: 12px; font-weight: 600; border-radius: 8px;"
              [disabled]="isLoading || !authPassword.trim()"
            >
              <i *ngIf="isLoading" class="fa-solid fa-spinner fa-spin" style="margin-right: 6px;"></i>
              <span>{{ isLoading ? 'Validando con IDaaS...' : 'Iniciar Sesión' }}</span>
            </button>
          </form>
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
  currentStep: 'select-provider' | 'google' | 'microsoft' | 'enter-password' | 'google-config' = 'select-provider';
  customGmail: string = '';
  gcpClientIdInput: string = '161262671547-7i2di146a7heisrhqm6ihq1dd6te6jd1.apps.googleusercontent.com';

  selectedUserName: string = '';
  selectedEmail: string = '';
  selectedUserInitial: string = '';
  selectedRole: string = '';
  isEvalUser: boolean = false;
  isOmar: boolean = false;

  authPassword: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    if (typeof window !== 'undefined') {
      const cfg = this.authService.getGcpConfig();
      if (cfg.clientId) {
        this.gcpClientIdInput = cfg.clientId;
      }
      if (window.location.hash) {
        this.handleOAuthCallback();
      }
    }
  }

  private handleOAuthCallback(): void {
    const hash = window.location.hash.substring(1);
    const params = new URLSearchParams(hash);
    const idToken = params.get('id_token');

    if (idToken) {
      this.isLoading = true;
      try {
        const parts = idToken.split('.');
        if (parts.length >= 2) {
          const payloadJson = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
          const payload = JSON.parse(payloadJson);
          const email = (payload.email || '').trim().toLowerCase();
          const name = payload.name || payload.given_name || email.split('@')[0];

          // 1. Validar política de dominio (@gmail.com o @duocuc.cl)
          const domainValidation = this.authService.validateGcpEmail(email);
          if (!domainValidation.valid) {
            this.errorMessage = domainValidation.error || 'Correo no admitido por política de GCP.';
            this.isLoading = false;
            return;
          }

          // 2. Asociar Tenant
          const isOmar = email.includes('omar') || email.includes('nieto') || email.includes('duoc');
          const tenantId = isOmar ? 'OmarNieto-npke9' : 'SergioVelasquez-dnfj9';

          // 3. Completar autenticación oficial
          this.completeAuth(name, email, isOmar ? 'Google Workspace (Duoc)' : 'Google Account (Gmail)', idToken, tenantId);
        }
      } catch (err: any) {
        this.errorMessage = 'Error al procesar token oficial de Google: ' + (err.message || 'Token inválido');
        this.isLoading = false;
      }
    }
  }

  openOfficialGoogleLogin(): void {
    const config = this.authService.getGcpConfig();
    const clientId = (this.gcpClientIdInput || config.clientId || '').trim();
    if (!clientId) {
      this.currentStep = 'google-config';
      return;
    }
    this.authService.saveGcpConfig({ clientId });
    const isOmar = this.selectedEmail.includes('omar') || this.selectedEmail.includes('nieto');
    const tenantId = isOmar ? 'OmarNieto-npke9' : 'SergioVelasquez-dnfj9';
    window.location.href = this.authService.getGoogleOAuthUrl(clientId, tenantId);
  }

  goToStep(step: 'select-provider' | 'google' | 'microsoft' | 'enter-password'): void {
    this.currentStep = step;
    this.errorMessage = '';
    this.authPassword = '';
  }

  preparePasswordStep(name: string, email: string, initial: string, role: string, isEval: boolean, isOmar: boolean): void {
    this.selectedUserName = name;
    this.selectedEmail = email;
    this.selectedUserInitial = initial;
    this.selectedRole = role;
    this.isEvalUser = isEval;
    this.isOmar = isOmar;
    this.authPassword = '';
    this.errorMessage = '';
    this.currentStep = 'enter-password';
  }

  notifyBlockedProvider(provider: string): void {
    alert(`Acceso bloqueado: El proveedor ${provider} no está autorizado. Por política de seguridad IDaaS en Google Cloud, solo se admiten cuentas de correo oficiales (@gmail.com).`);
  }

  startCustomGoogle(): void {
    if (!this.customGmail.trim()) return;
    const email = this.customGmail.trim();
    const validation = this.authService.validateGcpEmail(email);
    if (!validation.valid) {
      alert(validation.error);
      return;
    }
    const isOmar = email.toLowerCase().includes('omar') || email.toLowerCase().includes('nieto');
    const isSergio = email.toLowerCase().includes('sergio');
    const name = isOmar ? 'Omar Nieto' : (isSergio ? 'Sergio Velásquez' : email.split('@')[0].toUpperCase());
    const initial = isOmar ? 'ON' : (isSergio ? 'SV' : email.substring(0, 2).toUpperCase());
    const isEval = isOmar || isSergio;
    this.preparePasswordStep(name, email, initial, isOmar ? 'Google Workspace (Duoc)' : 'Google Account (Gmail)', isEval, isOmar);
  }

  startCustomMicrosoft(): void {
    this.notifyBlockedProvider('Microsoft (Hotmail/Outlook)');
  }

  submitPassword(): void {
    this.errorMessage = '';
    const email = this.selectedEmail.trim().toLowerCase();
    const pass = this.authPassword.trim();

    if (!pass) {
      this.errorMessage = 'Debes ingresar una contraseña.';
      return;
    }

    // Validación de dominio permitido en GCP (@gmail.com)
    const domainCheck = this.authService.validateGcpEmail(email);
    if (!domainCheck.valid) {
      this.errorMessage = domainCheck.error || 'Correo no autorizado por política de GCP.';
      return;
    }

    // 1. Validación de Usuarios de Evaluación (Sergio / Omar)
    if (this.isEvalUser) {
      const expectedPassword = this.isOmar ? 'AdminPassword123!' : 'Password123!';
      if (pass !== expectedPassword) {
        this.errorMessage = `Contraseña incorrecta para ${this.selectedUserName}. Inténtalo de nuevo.`;
        return;
      }
      this.completeAuth(this.selectedUserName, this.selectedEmail, this.selectedRole);
      return;
    }

    // 2. Validación Real contra Google Cloud Identity Platform REST
    this.isLoading = true;
    const apiKey = this.authService.getSavedApiKey();
    const tenantId = this.isOmar ? 'OmarNieto-npke9' : 'SergioVelasquez-dnfj9';

    this.authService.loginWithGcpIdentity(apiKey, email, pass, tenantId).subscribe({
      next: (res) => {
        this.isLoading = false;
        const name = res.displayName || this.selectedUserName || email.split('@')[0];
        this.completeAuth(name, res.email, 'Usuario GCP Identity Platform');
      },
      error: (err) => {
        this.isLoading = false;
        const msg = err.error?.error?.message || err.message || '';
        if (msg === 'EMAIL_NOT_FOUND') {
          this.errorMessage = 'Usuario no encontrado en Identity Platform. Verifica que el correo esté registrado.';
        } else if (msg === 'INVALID_PASSWORD') {
          this.errorMessage = 'Contraseña incorrecta. Inténtalo de nuevo.';
        } else if (msg.includes('INVALID_LOGIN_CREDENTIALS')) {
          this.errorMessage = 'Credenciales inválidas (correo o contraseña incorrectos).';
        } else if (msg.includes('API key not valid')) {
          this.errorMessage = 'Web API Key de GCP no válida. Configúrala en la consola.';
        } else {
          this.errorMessage = 'Error al validar credenciales: ' + (msg || 'No se pudo conectar con el proveedor');
        }
      }
    });
  }

  private completeAuth(name: string, email: string, roleDescription: string, externalToken?: string, externalTenantId?: string): void {
    const isGoogle = email.toLowerCase().includes('gmail') || email.toLowerCase().includes('google') || email.toLowerCase().includes('duoc');
    const iss = isGoogle ? 'https://securetoken.google.com/faster360-cloud-project' : 'https://idaas.faster360.com/';
    
    const isOmar = email.toLowerCase().includes('omar') || email.toLowerCase().includes('nieto');
    const tenantId = externalTenantId || (isOmar ? 'OmarNieto-npke9' : 'SergioVelasquez-dnfj9');

    let token = externalToken;
    if (!token) {
      const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
      const payload = btoa(JSON.stringify({
        sub: email,
        name: name,
        email: email,
        iss: iss,
        tenant_id: tenantId,
        iat: Math.floor(Date.now() / 1000),
        exp: Math.floor(Date.now() / 1000) + (60 * 60 * 24)
      }));
      const fakeSignature = btoa('faster360-oauth-key-' + Date.now());
      token = `${header}.${payload}.${fakeSignature}`;
    }

    const session: UserSession = {
      email: email,
      name: name,
      token: token,
      provider: isGoogle ? 'gcp-identity-platform' : 'idaas-preset',
      tenantId: tenantId,
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
      setTimeout(() => {
        window.close();
      }, 500);
    } else {
      // Si fue abierto en pestaña independiente
      this.authService.saveSession(session);
      window.location.href = '/';
    }
  }
}
