import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RabbitAdminService, CreateQueuePayload, CreateExchangePayload, CreateBindingPayload } from '../../core/services/rabbit-admin.service';

@Component({
  selector: 'app-admin-queues',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-queues.component.html',
  styleUrls: ['./admin-queues.component.css']
})
export class AdminQueuesComponent {
  // Estado para creación de colas
  queueForm: CreateQueuePayload = {
    name: '',
    durable: true,
    exclusive: false,
    autoDelete: false
  };

  // Estado para creación de exchanges
  exchangeForm: CreateExchangePayload = {
    name: '',
    type: 'topic',
    durable: true,
    autoDelete: false
  };

  // Estado para bindings
  bindingForm: CreateBindingPayload = {
    queueName: '',
    exchangeName: '',
    routingKey: ''
  };

  // Nombres para eliminación directa
  deleteQueueName: string = '';
  deleteExchangeName: string = '';

  // Logs de actividad
  operationLogs: { timestamp: string; type: 'success' | 'error' | 'info'; message: string }[] = [];
  isLoading: boolean = false;

  constructor(private rabbitAdminService: RabbitAdminService) {}

  addLog(type: 'success' | 'error' | 'info', message: string): void {
    const time = new Date().toLocaleTimeString();
    this.operationLogs.unshift({ timestamp: time, type, message });
    if (this.operationLogs.length > 30) {
      this.operationLogs.pop();
    }
  }

  // --- MÉTODOS DE COLAS ---
  submitCreateQueue(): void {
    if (!this.queueForm.name.trim()) {
      this.addLog('error', 'El nombre de la cola es obligatorio.');
      return;
    }
    this.isLoading = true;
    this.rabbitAdminService.createQueue(this.queueForm).subscribe({
      next: (res) => {
        this.addLog('success', `Cola '${this.queueForm.name}' creada exitosamente.`);
        this.queueForm.name = '';
        this.isLoading = false;
      },
      error: (err) => {
        const errorMsg = err.error?.details?.name || err.error?.error || 'Error de conexión con rabbit-admin-service';
        this.addLog('error', `Fallo al crear cola: ${errorMsg}`);
        this.isLoading = false;
      }
    });
  }

  submitDeleteQueue(nameToDelete?: string): void {
    const target = nameToDelete || this.deleteQueueName;
    if (!target.trim()) {
      this.addLog('error', 'Ingresa el nombre de la cola a eliminar.');
      return;
    }
    this.isLoading = true;
    this.rabbitAdminService.deleteQueue(target.trim()).subscribe({
      next: (res) => {
        this.addLog('info', `Cola '${target}' eliminada.`);
        if (!nameToDelete) this.deleteQueueName = '';
        this.isLoading = false;
      },
      error: (err) => {
        this.addLog('error', `Error al eliminar cola '${target}': ${err.message}`);
        this.isLoading = false;
      }
    });
  }

  // --- MÉTODOS DE EXCHANGES ---
  submitCreateExchange(): void {
    if (!this.exchangeForm.name.trim()) {
      this.addLog('error', 'El nombre del exchange es obligatorio.');
      return;
    }
    this.isLoading = true;
    this.rabbitAdminService.createExchange(this.exchangeForm).subscribe({
      next: (res) => {
        this.addLog('success', `Exchange '${this.exchangeForm.name}' (${this.exchangeForm.type}) creado exitosamente.`);
        this.exchangeForm.name = '';
        this.isLoading = false;
      },
      error: (err) => {
        const errorMsg = err.error?.details?.name || err.error?.error || 'Error de conexión con rabbit-admin-service';
        this.addLog('error', `Fallo al crear exchange: ${errorMsg}`);
        this.isLoading = false;
      }
    });
  }

  submitDeleteExchange(): void {
    if (!this.deleteExchangeName.trim()) {
      this.addLog('error', 'Ingresa el nombre del exchange a eliminar.');
      return;
    }
    this.isLoading = true;
    this.rabbitAdminService.deleteExchange(this.deleteExchangeName.trim()).subscribe({
      next: (res) => {
        this.addLog('info', `Exchange '${this.deleteExchangeName}' eliminado.`);
        this.deleteExchangeName = '';
        this.isLoading = false;
      },
      error: (err) => {
        this.addLog('error', `Error al eliminar exchange: ${err.message}`);
        this.isLoading = false;
      }
    });
  }

  // --- MÉTODOS DE BINDINGS ---
  submitCreateBinding(): void {
    if (!this.bindingForm.queueName.trim() || !this.bindingForm.exchangeName.trim() || !this.bindingForm.routingKey.trim()) {
      this.addLog('error', 'Todos los campos de enlace (cola, exchange y routing key) son obligatorios.');
      return;
    }
    this.isLoading = true;
    this.rabbitAdminService.createBinding(this.bindingForm).subscribe({
      next: (res) => {
        this.addLog('success', `Binding establecido: '${this.bindingForm.queueName}' -> '${this.bindingForm.exchangeName}' [${this.bindingForm.routingKey}]`);
        this.isLoading = false;
      },
      error: (err) => {
        this.addLog('error', `Error al crear binding: ${err.message}`);
        this.isLoading = false;
      }
    });
  }

  submitDeleteBinding(): void {
    if (!this.bindingForm.queueName.trim() || !this.bindingForm.exchangeName.trim() || !this.bindingForm.routingKey.trim()) {
      this.addLog('error', 'Todos los campos son requeridos para eliminar el binding.');
      return;
    }
    this.isLoading = true;
    this.rabbitAdminService.deleteBinding(this.bindingForm).subscribe({
      next: (res) => {
        this.addLog('info', `Binding eliminado entre '${this.bindingForm.queueName}' y '${this.bindingForm.exchangeName}'.`);
        this.isLoading = false;
      },
      error: (err) => {
        this.addLog('error', `Error al eliminar binding: ${err.message}`);
        this.isLoading = false;
      }
    });
  }
}
