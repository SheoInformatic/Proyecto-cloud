import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CreateQueuePayload {
  name: string;
  durable: boolean;
  exclusive: boolean;
  autoDelete: boolean;
}

export interface CreateExchangePayload {
  name: string;
  type: 'direct' | 'topic' | 'fanout' | 'headers';
  durable: boolean;
  autoDelete: boolean;
}

export interface CreateBindingPayload {
  queueName: string;
  exchangeName: string;
  routingKey: string;
}

@Injectable({
  providedIn: 'root'
})
export class RabbitAdminService {
  private apiUrl = 'http://localhost:8083/api/admin';

  constructor(private http: HttpClient) {}

  // Gestión de Colas
  createQueue(payload: CreateQueuePayload): Observable<any> {
    return this.http.post(`${this.apiUrl}/queues`, payload);
  }

  deleteQueue(queueName: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/queues/${queueName}`);
  }

  // Gestión de Exchanges
  createExchange(payload: CreateExchangePayload): Observable<any> {
    return this.http.post(`${this.apiUrl}/exchanges`, payload);
  }

  deleteExchange(exchangeName: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/exchanges/${exchangeName}`);
  }

  // Gestión de Bindings
  createBinding(payload: CreateBindingPayload): Observable<any> {
    return this.http.post(`${this.apiUrl}/bindings`, payload);
  }

  deleteBinding(payload: CreateBindingPayload): Observable<any> {
    return this.http.request('delete', `${this.apiUrl}/bindings`, { body: payload });
  }
}
