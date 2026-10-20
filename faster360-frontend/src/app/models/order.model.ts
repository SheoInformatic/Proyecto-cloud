import { Product } from './product.model';

export interface CartItem {
  product: Product;
  quantity: number;
  selectedColor?: string;
  selectedSize?: string;
}

export interface CreateOrderPayload {
  customerName: string;
  customerEmail: string;
  totalAmount: number;
}

export interface OrderResponse {
  id: number;
  orderNumber: string;
  customerName: string;
  customerEmail: string;
  totalAmount: number;
  status: string;
  createdAt: string;
}

export interface ToastMessage {
  id: number;
  type: 'success' | 'info' | 'warning' | 'error';
  title: string;
  message: string;
}

export interface PromoCoupon {
  code: string;
  discountType: 'percentage' | 'fixed';
  discountValue: number;
  description: string;
}
