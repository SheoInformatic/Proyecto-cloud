import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Product } from './models/product.model';
import { AuthService, UserSession } from './core/services/auth.service';
import { OrderService } from './core/services/order.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  currentUser: UserSession | null = null;

  // Catálogo de productos de Faster 360
  products: Product[] = [
    {
      id: 1,
      name: 'Sneakers Faster Runner X',
      category: 'Calzado',
      price: 89990,
      image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop',
      description: 'Zapatillas de alto rendimiento con amortiguación reactiva.'
    },
    {
      id: 2,
      name: 'Smartwatch Aero 360',
      category: 'Tecnología',
      price: 149990,
      image: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop',
      description: 'Monitor de ritmo cardíaco, GPS y autonomía de 10 días.'
    },
    {
      id: 3,
      name: 'Mochila Urbana Tech Waterproof',
      category: 'Accesorios',
      price: 45990,
      image: 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop',
      description: 'Compartimento acolchado para laptop y tela impermeable.'
    },
    {
      id: 4,
      name: 'Audífonos Noise Cancelling Pro',
      category: 'Audio',
      price: 129990,
      image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop',
      description: 'Cancelación activa de ruido con sonido envolvente 360.'
    }
  ];

  cart: { product: Product; quantity: number }[] = [];
  isProcessing = false;
  lastOrderResponse: any = null;
  errorMessage = '';

  constructor(
    public authService: AuthService,
    private orderService: OrderService
  ) {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  loginDemo(): void {
    // Simulación de login exitoso mediante IDaaS entregando un token JWT
    this.authService.loginWithIdaaS('cliente@faster360.com', 'Usuario Faster 360');
  }

  logout(): void {
    this.authService.logout();
    this.lastOrderResponse = null;
  }

  addToCart(product: Product): void {
    const item = this.cart.find(i => i.product.id === product.id);
    if (item) {
      item.quantity++;
    } else {
      this.cart.push({ product, quantity: 1 });
    }
  }

  removeFromCart(index: number): void {
    this.cart.splice(index, 1);
  }

  get totalCart(): number {
    return this.cart.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
  }

  checkout(): void {
    if (!this.currentUser) {
      alert('Debes iniciar sesión con IDaaS antes de finalizar la compra.');
      return;
    }

    if (this.cart.length === 0) {
      alert('El carrito está vacío.');
      return;
    }

    this.isProcessing = true;
    this.errorMessage = '';
    this.lastOrderResponse = null;

    const payload = {
      customerName: this.currentUser.name,
      customerEmail: this.currentUser.email,
      totalAmount: this.totalCart
    };

    // Llamada al microservicio de órdenes con el JWT añadido por el interceptor
    this.orderService.createOrder(payload).subscribe({
      next: (res) => {
        this.lastOrderResponse = res;
        this.cart = []; // Limpiar carrito tras éxito
        this.isProcessing = false;
      },
      error: (err) => {
        this.errorMessage = 'Hubo un error al procesar el pedido. Verifica que order-service esté activo.';
        this.isProcessing = false;
        console.error(err);
      }
    });
  }
}
