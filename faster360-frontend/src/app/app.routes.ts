import { Routes } from '@angular/router';
import { AdminQueuesComponent } from './pages/admin-queues/admin-queues.component';
import { AuthPopupComponent } from './pages/auth-popup/auth-popup.component';

export const routes: Routes = [
  { path: 'login-popup', component: AuthPopupComponent },
  { path: 'admin/queues', component: AdminQueuesComponent },
  { path: '**', redirectTo: '' }
];

