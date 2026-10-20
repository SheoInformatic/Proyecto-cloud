import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterOutlet } from '@angular/router';
import { Product } from './models/product.model';
import { CartItem, OrderResponse, PromoCoupon, ToastMessage } from './models/order.model';
import { AuthService, UserSession } from './core/services/auth.service';
import { OrderService } from './core/services/order.service';
import { AdminQueuesComponent } from './pages/admin-queues/admin-queues.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, AdminQueuesComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  isPopupRoute: boolean = false;
  currentUser: UserSession | null = null;
  isAdminQueuesModalOpen: boolean = false;

  // Catálogo completo y realista de Faster 360
  products: Product[] = [
    {
      id: 1,
      name: 'Sneakers Faster Runner X-Pro',
      category: 'Calzado',
      price: 89990,
      originalPrice: 119990,
      image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80',
      description: 'Zapatillas de ultra alto rendimiento con amortiguación reactiva Boost y suela de agarre dinámico.',
      rating: 4.9,
      reviewsCount: 148,
      badge: 'OFERTA -25%',
      badgeType: 'sale',
      stock: 7,
      features: ['Amortiguación reactiva NitroPulse', 'Tejido respirable 3D Mesh', 'Drop: 8mm'],
      colors: [
        { name: 'Rojo Carmesí', hex: '#e11d48' },
        { name: 'Negro Ónix', hex: '#0f172a' },
        { name: 'Blanco Lunar', hex: '#f8fafc' }
      ],
      sizes: ['39', '40', '41', '42', '43', '44']
    },
    {
      id: 2,
      name: 'Smartwatch Aero 360 Sapphire',
      category: 'Tecnología',
      price: 149990,
      originalPrice: 179990,
      image: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&auto=format&fit=crop&q=80',
      description: 'Cristal de zafiro, sensor biométrico SpO2, GPS multi-banda y autonomía extendida de 12 días.',
      rating: 4.8,
      reviewsCount: 96,
      badge: 'TOP VENTAS',
      badgeType: 'hot',
      stock: 12,
      features: ['Pantalla AMOLED 1.43"', 'Resistente 5 ATM al agua', 'Sincronización en la nube'],
      colors: [
        { name: 'Plata Titanio', hex: '#e2e8f0' },
        { name: 'Negro Carbón', hex: '#1e293b' }
      ]
    },
    {
      id: 3,
      name: 'Mochila Urbana Tech Waterproof 28L',
      category: 'Accesorios',
      price: 45990,
      originalPrice: 59990,
      image: 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&auto=format&fit=crop&q=80',
      description: 'Compartimento acolchado para laptop 16", puerto de carga USB integrado y tela balística anti-desgarro.',
      rating: 4.7,
      reviewsCount: 64,
      badge: 'NUEVO',
      badgeType: 'new',
      stock: 15,
      features: ['Cierre YKK repelente al agua', 'Bolsillo secreto RFID', 'Soporte lumbar ergonómico']
    },
    {
      id: 4,
      name: 'Audífonos Noise Cancelling Studio 360',
      category: 'Audio',
      price: 129990,
      originalPrice: 159990,
      image: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80',
      description: 'Cancelación activa de ruido adaptativa con drivers de neodimio de 40mm y audio Hi-Res inalámbrico.',
      rating: 4.9,
      reviewsCount: 215,
      badge: 'DESTACADO',
      badgeType: 'trending',
      stock: 4,
      features: ['Hasta 45 horas de batería', 'Carga rápida: 10 min = 4 horas', 'Audio espacial 360°'],
      colors: [
        { name: 'Negro Mate', hex: '#090d16' },
        { name: 'Gris Grafito', hex: '#475569' }
      ]
    },
    {
      id: 5,
      name: 'Sneakers Quantum Stealth All-Black',
      category: 'Calzado',
      price: 94990,
      originalPrice: 112990,
      image: 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=800&auto=format&fit=crop&q=80',
      description: 'Diseño minimalista futurista con suela de tracción multi-terreno y plantilla con memoria de forma.',
      rating: 4.8,
      reviewsCount: 112,
      badge: 'OFERTA -16%',
      badgeType: 'sale',
      stock: 8,
      features: ['Plantilla Memory Foam OrthoLite', 'Reflectivo 3M para la noche', 'Super livianas: 240g'],
      colors: [
        { name: 'Negro Puro', hex: '#0f172a' }
      ],
      sizes: ['40', '41', '42', '43', '44']
    },
    {
      id: 6,
      name: 'Smartband Pulse Fit Active',
      category: 'Tecnología',
      price: 39990,
      originalPrice: 49990,
      image: 'https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?w=800&auto=format&fit=crop&q=80',
      description: 'Pulsera deportiva ultra ligera con monitor continuo de sueño, estrés y más de 50 modos deportivos.',
      rating: 4.6,
      reviewsCount: 88,
      badge: 'POPULAR',
      badgeType: 'hot',
      stock: 19,
      features: ['Batería de 14 días', 'Notificaciones de apps en tiempo real', 'Sumergible hasta 50m']
    },
    {
      id: 7,
      name: 'Earbuds True Wireless Bass 360',
      category: 'Audio',
      price: 54990,
      originalPrice: 69990,
      image: 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&auto=format&fit=crop&q=80',
      description: 'Estuche de carga inalámbrica Qi, 4 micrófonos con cancelación de ruido ambiental y modo transparencia.',
      rating: 4.7,
      reviewsCount: 173,
      badge: 'NUEVO',
      badgeType: 'new',
      stock: 11,
      features: ['Bluetooth 5.3 baja latencia', 'Protección IPX5 contra lluvia y sudor', '32 horas de reproducción total']
    },
    {
      id: 8,
      name: 'Lentes Polarizados Sport Vision 360',
      category: 'Accesorios',
      price: 34990,
      originalPrice: 42990,
      image: 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=800&auto=format&fit=crop&q=80',
      description: 'Cristales TAC polarizados con protección UV400 completa y marco ultra flexible de TR90.',
      rating: 4.7,
      reviewsCount: 52,
      badge: 'VERANO',
      badgeType: 'trending',
      stock: 14,
      features: ['Filtro anti-reflejo HD', 'Montura ultra ligera de 19g', 'Incluye estuche rígido y funda']
    },
    {
      id: 9,
      name: 'Botella Smart Termo Digital 750ml',
      category: 'Accesorios',
      price: 24990,
      originalPrice: 29990,
      image: 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&auto=format&fit=crop&q=80',
      description: 'Acero inoxidable grado alimenticio 304 con tapa touch que indica la temperatura en tiempo real.',
      rating: 4.8,
      reviewsCount: 91,
      stock: 22,
      features: ['Mantiene frío por 24h y calor por 12h', 'Libre de BPA', 'Sin baterías (tapa recargable)']
    },
    {
      id: 10,
      name: 'Altavoz Bluetooth Titan Boom 360',
      category: 'Audio',
      price: 69990,
      originalPrice: 89990,
      image: 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=800&auto=format&fit=crop&q=80',
      description: 'Potencia de 30W con radiadores pasivos duales para graves profundos y certificación IP67 contra agua.',
      rating: 4.8,
      reviewsCount: 134,
      badge: 'OFERTA -22%',
      badgeType: 'sale',
      stock: 9,
      features: ['Sonido envolvente 360 grados', 'Función PartySync para emparejar 2 parlantes', 'Powerbank integrado']
    },
    {
      id: 11,
      name: 'Sneakers Trail Matrix Off-Road',
      category: 'Calzado',
      price: 99990,
      originalPrice: 129990,
      image: 'https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=800&auto=format&fit=crop&q=80',
      description: 'Tacos multidireccionales para agarre en tierra, barro y roca. Puntera reforzada anti-impactos.',
      rating: 4.9,
      reviewsCount: 78,
      badge: 'TOP VENTAS',
      badgeType: 'hot',
      stock: 5,
      features: ['Placa de fibra para protección plantar', 'Membrana semi-impermeable', 'Sistema de lazado rápido'],
      sizes: ['40', '41', '42', '43']
    },
    {
      id: 12,
      name: 'Riñonera Crossbody Reflective Gear',
      category: 'Accesorios',
      price: 28990,
      originalPrice: 35990,
      image: 'https://images.unsplash.com/photo-1622560480605-d83c853bc5c3?w=800&auto=format&fit=crop&q=80',
      description: 'Correa ajustable con broche magnético Fidlock, organizador interno y detalles altamente reflectantes.',
      rating: 4.6,
      reviewsCount: 45,
      stock: 16,
      features: ['Broche magnético de alta seguridad', 'Compartimento suave para smartphone', 'Cordura 500D resistente']
    }
  ];

  // Carrito de compras
  cart: CartItem[] = [];
  isCartOpen = false;

  // Filtros y Búsqueda
  selectedCategory = 'all';
  searchQuery = '';
  sortBy: 'featured' | 'price-asc' | 'price-desc' | 'rating' = 'featured';

  // Favoritos / Wishlist
  wishlist = new Set<number>();

  // Modales
  isQuickViewOpen = false;
  selectedProduct: Product | null = null;
  quickViewSelectedColor = '';
  quickViewSelectedSize = '';
  quickViewQty = 1;

  isAuthModalOpen = false;
  authMode: 'gcp' | 'presets' = 'gcp';
  gcpApiKey = 'AIzaSyBR-V_BPNAz7c2ak0kEvaI6ydWeZvbz4pY';
  gcpEmail = '';
  gcpPassword = '';
  gcpAuthAction: 'login' | 'signup' = 'login';
  isGcpLoading = false;
  customAuthEmail = '';
  customAuthName = '';

  isCheckoutModalOpen = false;
  checkoutStep: 1 | 2 | 3 = 1;
  checkoutForm = {
    name: '',
    email: '',
    phone: '+56 9 8765 4321',
    address: 'Av. Providencia 1240, Depto 502',
    city: 'Santiago',
    paymentMethod: 'webpay' as 'webpay' | 'transfer' | 'mercadopago'
  };

  isOrderSuccessOpen = false;
  lastOrderResponse: any = null;
  errorMessage = '';
  isProcessing = false;
  processingStage = '';

  isOrdersHistoryOpen = false;
  ordersHistory: OrderResponse[] = [];
  isLoadingOrders = false;

  isCloudArchitectureOpen = false;

  // Cupones de descuento
  availableCoupons: PromoCoupon[] = [
    { code: 'FASTER10', discountType: 'percentage', discountValue: 10, description: '10% de descuento en el total' },
    { code: 'CLOUD2026', discountType: 'fixed', discountValue: 15000, description: '$15.000 CLP de descuento en compras de arquitectura cloud' },
    { code: 'BIENVENIDO', discountType: 'percentage', discountValue: 15, description: '15% de descuento primera compra' }
  ];
  appliedCoupon: PromoCoupon | null = null;
  couponInput = '';
  couponError = '';

  // Notificaciones Toast
  toasts: ToastMessage[] = [];
  private toastCounter = 0;

  constructor(
    public authService: AuthService,
    private orderService: OrderService
  ) {}

  ngOnInit(): void {
    if (typeof window !== 'undefined') {
      this.isPopupRoute = window.location.pathname.includes('login-popup');
    }

    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      if (user) {
        this.checkoutForm.name = user.name;
        this.checkoutForm.email = user.email;
      }
    });

    this.gcpApiKey = this.authService.getSavedApiKey();

    // Cargar carrito persistente si existe
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      const savedCart = localStorage.getItem('faster360_cart');
      if (savedCart) {
        try {
          this.cart = JSON.parse(savedCart);
        } catch (e) {
          console.error('Error parseando carrito', e);
        }
      }

      const savedWishlist = localStorage.getItem('faster360_wishlist');
      if (savedWishlist) {
        try {
          const arr: number[] = JSON.parse(savedWishlist);
          this.wishlist = new Set(arr);
        } catch (e) {
          console.error('Error parseando wishlist', e);
        }
      }
    }
  }

  // --- FILTROS Y CATÁLOGO ---
  get categories(): string[] {
    return ['all', 'Calzado', 'Tecnología', 'Audio', 'Accesorios'];
  }

  get filteredProducts(): Product[] {
    let result = [...this.products];

    // Filtro por categoría
    if (this.selectedCategory !== 'all') {
      result = result.filter(p => p.category === this.selectedCategory);
    }

    // Filtro por búsqueda
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase();
      result = result.filter(p =>
        p.name.toLowerCase().includes(query) ||
        p.description.toLowerCase().includes(query) ||
        p.category.toLowerCase().includes(query)
      );
    }

    // Ordenamiento
    switch (this.sortBy) {
      case 'price-asc':
        result.sort((a, b) => a.price - b.price);
        break;
      case 'price-desc':
        result.sort((a, b) => b.price - a.price);
        break;
      case 'rating':
        result.sort((a, b) => (b.rating || 0) - (a.rating || 0));
        break;
      default:
        // 'featured'
        result.sort((a, b) => a.id - b.id);
        break;
    }

    return result;
  }

  selectCategory(category: string): void {
    this.selectedCategory = category;
  }

  clearSearch(): void {
    this.searchQuery = '';
  }

  // --- FAVORITOS / WISHLIST ---
  toggleWishlist(product: Product, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    if (this.wishlist.has(product.id)) {
      this.wishlist.delete(product.id);
      this.showToast('info', 'Favoritos', `Removido de tu lista: ${product.name}`);
    } else {
      this.wishlist.add(product.id);
      this.showToast('success', 'Favoritos', `¡Agregado a tus favoritos!`);
    }
    this.saveWishlist();
  }

  isInWishlist(productId: number): boolean {
    return this.wishlist.has(productId);
  }

  private saveWishlist(): void {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      localStorage.setItem('faster360_wishlist', JSON.stringify(Array.from(this.wishlist)));
    }
  }

  // --- CARRITO DE COMPRAS ---
  addToCart(product: Product, color?: string, size?: string, qty = 1, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    const item = this.cart.find(i =>
      i.product.id === product.id &&
      i.selectedColor === color &&
      i.selectedSize === size
    );

    if (item) {
      item.quantity += qty;
    } else {
      this.cart.push({
        product,
        quantity: qty,
        selectedColor: color || (product.colors ? product.colors[0]?.name : undefined),
        selectedSize: size || (product.sizes ? product.sizes[0] : undefined)
      });
    }

    this.saveCart();
    this.showToast('success', 'Agregado al carrito', `${product.name} añadido correctamente.`);
  }

  updateCartQty(index: number, delta: number): void {
    if (!this.cart[index]) return;
    this.cart[index].quantity += delta;
    if (this.cart[index].quantity <= 0) {
      this.removeFromCart(index);
    } else {
      this.saveCart();
    }
  }

  removeFromCart(index: number): void {
    const removed = this.cart.splice(index, 1);
    this.saveCart();
    if (removed.length > 0) {
      this.showToast('info', 'Carrito', `Producto removido.`);
    }
  }

  clearCart(): void {
    this.cart = [];
    this.appliedCoupon = null;
    this.saveCart();
    this.showToast('info', 'Carrito', 'Has vaciado tu carrito de compras.');
  }

  private saveCart(): void {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      localStorage.setItem('faster360_cart', JSON.stringify(this.cart));
    }
  }

  get totalItemsCount(): number {
    return this.cart.reduce((sum, item) => sum + item.quantity, 0);
  }

  get subtotalCart(): number {
    return this.cart.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
  }

  get discountAmount(): number {
    if (!this.appliedCoupon || this.subtotalCart === 0) return 0;
    if (this.appliedCoupon.discountType === 'percentage') {
      return Math.round((this.subtotalCart * this.appliedCoupon.discountValue) / 100);
    } else {
      return Math.min(this.appliedCoupon.discountValue, this.subtotalCart);
    }
  }

  get shippingCost(): number {
    if (this.subtotalCart === 0) return 0;
    return this.subtotalCart >= 50000 ? 0 : 3990;
  }

  get freeShippingThreshold(): number {
    return 50000;
  }

  get freeShippingRemaining(): number {
    return Math.max(0, this.freeShippingThreshold - this.subtotalCart);
  }

  get freeShippingProgress(): number {
    if (this.subtotalCart >= this.freeShippingThreshold) return 100;
    return Math.round((this.subtotalCart / this.freeShippingThreshold) * 100);
  }

  get totalCart(): number {
    return Math.max(0, this.subtotalCart - this.discountAmount + this.shippingCost);
  }

  toggleCart(): void {
    this.isCartOpen = !this.isCartOpen;
  }

  openCart(): void {
    this.isCartOpen = true;
  }

  closeCart(): void {
    this.isCartOpen = false;
  }

  // --- CUPONES DE DESCUENTO ---
  applyCoupon(): void {
    this.couponError = '';
    const code = this.couponInput.trim().toUpperCase();
    if (!code) {
      this.couponError = 'Ingresa un código de cupón';
      return;
    }

    const found = this.availableCoupons.find(c => c.code === code);
    if (found) {
      this.appliedCoupon = found;
      this.couponInput = '';
      this.showToast('success', 'Cupón aplicado', `¡Cupón ${found.code} aplicado con éxito!`);
    } else {
      this.couponError = 'Código de cupón inválido o expirado';
      this.showToast('error', 'Cupón inválido', 'El cupón no es válido. Prueba con FASTER10.');
    }
  }

  removeCoupon(): void {
    this.appliedCoupon = null;
    this.showToast('info', 'Cupón', 'Cupón removido del pedido.');
  }

  // --- VISTA RÁPIDA / QUICK VIEW MODAL ---
  openQuickView(product: Product, event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    this.selectedProduct = product;
    this.quickViewSelectedColor = product.colors && product.colors.length > 0 ? product.colors[0].name : '';
    this.quickViewSelectedSize = product.sizes && product.sizes.length > 0 ? product.sizes[0] : '';
    this.quickViewQty = 1;
    this.isQuickViewOpen = true;
  }

  closeQuickView(): void {
    this.isQuickViewOpen = false;
    this.selectedProduct = null;
  }

  addQuickViewToCart(): void {
    if (!this.selectedProduct) return;
    this.addToCart(
      this.selectedProduct,
      this.quickViewSelectedColor,
      this.quickViewSelectedSize,
      this.quickViewQty
    );
    this.closeQuickView();
    this.openCart();
  }

  // --- AUTENTICACIÓN IDaaS (VENTANA EMERGENTE OAUTH + MODAL GCP) ---
  @HostListener('window:message', ['$event'])
  onOAuthMessage(event: MessageEvent): void {
    if (typeof window === 'undefined') return;
    if (event.origin !== window.location.origin) return;
    if (event.data && event.data.type === 'FASTER360_AUTH_SUCCESS') {
      const session = event.data.session as UserSession;
      if (session) {
        this.authService.saveSession(session);
        this.currentUser = session;
        this.checkoutForm.name = session.name;
        this.checkoutForm.email = session.email;
        this.closeAuthModal();
        this.showToast('success', 'Sesión Iniciada con IDaaS', `¡Bienvenido(a), ${session.name}!`);
      }
    }
  }

  openAuthPopup(): void {
    if (typeof window === 'undefined') return;
    const width = 520;
    const height = 720;
    const left = Math.max(0, Math.round(window.screenX + (window.outerWidth - width) / 2));
    const top = Math.max(0, Math.round(window.screenY + (window.outerHeight - height) / 2));
    const popup = window.open(
      '/login-popup',
      'Faster360_IDaaS_Auth',
      `width=${width},height=${height},left=${left},top=${top},status=no,toolbar=no,menubar=no,location=no,resizable=yes,scrollbars=yes`
    );
    if (!popup || popup.closed || typeof popup.closed === 'undefined') {
      // Fallback si el navegador bloquea la ventana emergente
      this.isAuthModalOpen = true;
      this.showToast('info', 'Ventana emergente', 'Tu navegador bloqueó el popup. Puedes acceder desde aquí.');
    } else {
      popup.focus();
    }
  }

  openAuthModal(): void {
    this.isAuthModalOpen = true;
  }

  closeAuthModal(): void {
    this.isAuthModalOpen = false;
  }

  submitGcpAuth(): void {
    if (!this.gcpApiKey.trim()) {
      this.showToast('warning', 'GCP Identity Platform', 'Por favor ingresa la Web API Key de tu proyecto GCP.');
      return;
    }
    if (!this.gcpEmail.trim() || !this.gcpPassword.trim()) {
      this.showToast('warning', 'Credenciales incompletas', 'Ingresa tu correo y contraseña.');
      return;
    }

    this.isGcpLoading = true;
    this.authService.saveApiKey(this.gcpApiKey);

    const authCall$ = this.gcpAuthAction === 'login'
      ? this.authService.loginWithGcpIdentity(this.gcpApiKey, this.gcpEmail, this.gcpPassword)
      : this.authService.signUpWithGcpIdentity(this.gcpApiKey, this.gcpEmail, this.gcpPassword);

    authCall$.subscribe({
      next: (res) => {
        this.isGcpLoading = false;
        this.closeAuthModal();
        const actionText = this.gcpAuthAction === 'login' ? 'Sesión iniciada' : 'Cuenta registrada e iniciada';
        this.showToast('success', 'GCP Identity Platform (IDaaS)', `${actionText} exitosamente como ${res.email}`);
      },
      error: (err) => {
        this.isGcpLoading = false;
        const msg = err.error?.error?.message || err.message || 'Error de conexión con Google Cloud Identity Platform';
        let humanMsg = msg;
        if (msg === 'EMAIL_NOT_FOUND') humanMsg = 'El correo no está registrado en Identity Platform.';
        else if (msg === 'INVALID_PASSWORD') humanMsg = 'Contraseña incorrecta.';
        else if (msg === 'EMAIL_EXISTS') humanMsg = 'El correo ya se encuentra registrado. Cambia a "Iniciar Sesión".';
        else if (msg.includes('WEAK_PASSWORD')) humanMsg = 'La contraseña debe tener al menos 6 caracteres.';
        else if (msg.includes('API key not valid')) humanMsg = 'La Web API Key de GCP no es válida. Verifícala en tu consola de Google Cloud.';

        this.showToast('error', 'Error en Google Cloud IDaaS', humanMsg);
      }
    });
  }

  loginWithPreset(preset: 'cliente' | 'arquitecto' | 'admin'): void {
    if (preset === 'cliente') {
      this.authService.loginWithIdaaS('sergio.velasquez@faster360.com', 'Sergio Velásquez (Cliente VIP)');
    } else if (preset === 'arquitecto') {
      this.authService.loginWithIdaaS('omar.nieto@faster360.com', 'Omar Nieto (Cloud Architect)');
    } else {
      this.authService.loginWithIdaaS('admin.queues@faster360.com', 'Omar Nieto (RabbitMQ Admin Specialist)');
    }
    this.closeAuthModal();
    this.showToast('success', 'IDaaS Autenticado', `Bienvenido(a), ${this.currentUser?.name}`);
  }

  loginCustom(): void {
    if (!this.customAuthEmail.trim() || !this.customAuthName.trim()) {
      alert('Por favor ingresa nombre y correo');
      return;
    }
    this.authService.loginWithIdaaS(this.customAuthEmail.trim(), this.customAuthName.trim());
    this.closeAuthModal();
    this.showToast('success', 'IDaaS Autenticado', `Sesión iniciada como ${this.customAuthName}`);
  }

  logout(): void {
    this.authService.logout();
    this.lastOrderResponse = null;
    this.showToast('info', 'Sesión cerrada', 'Has cerrado sesión correctamente.');
  }

  // --- GESTIÓN DE 2 USUARIOS Y RENOVACIÓN DE TOKENS ---
  isRenewingToken = false;

  loadPredefinedUser(userId: 'cliente' | 'admin'): void {
    const user = this.authService.PREDEFINED_USERS.find(u => u.id === userId);
    if (user) {
      this.gcpEmail = user.email;
      this.gcpPassword = user.defaultPassword;
      this.customAuthEmail = user.email;
      this.customAuthName = user.name;
      this.showToast('info', 'Usuario Seleccionado', `Datos de ${user.name} (${user.role}) cargados.`);
    }
  }

  quickSwitchUser(): void {
    if (!this.currentUser) return;
    const isCurrentlyClient = this.currentUser.email.includes('cliente') || this.currentUser.email.includes('sergio');
    const targetUserId = isCurrentlyClient ? 'admin' : 'cliente';
    const targetUser = this.authService.PREDEFINED_USERS.find(u => u.id === targetUserId);
    if (!targetUser) return;

    if (this.currentUser.provider === 'gcp-identity-platform') {
      this.isGcpLoading = true;
      this.authService.loginWithGcpIdentity(this.gcpApiKey, targetUser.email, targetUser.defaultPassword).subscribe({
        next: () => {
          this.isGcpLoading = false;
          this.showToast('success', 'Usuario Cambiado (GCP)', `Sesión activa como ${targetUser.name} [${targetUser.role}]`);
        },
        error: () => {
          this.isGcpLoading = false;
          this.loadPredefinedUser(targetUserId);
          this.openAuthModal();
          this.showToast('info', 'Autenticación requerida', `Registra o inicia sesión con ${targetUser.email}`);
        }
      });
    } else {
      this.loginWithPreset(targetUserId === 'cliente' ? 'cliente' : 'arquitecto');
      this.showToast('success', 'Usuario Cambiado', `Sesión activa como ${targetUser.name} [${targetUser.role}]`);
    }
  }

  renewTokenManual(): void {
    if (!this.currentUser) {
      this.showToast('warning', 'Sin sesión', 'No hay sesión activa para renovar token.');
      return;
    }
    this.isRenewingToken = true;
    this.authService.refreshToken().subscribe({
      next: (newToken) => {
        this.isRenewingToken = false;
        this.showToast('success', 'Token Renovado', 'El token JWT se ha renovado exitosamente con Google Cloud.');
      },
      error: (err) => {
        this.isRenewingToken = false;
        this.showToast('error', 'Error al renovar', err.error?.error?.message || err.message || 'No se pudo renovar.');
      }
    });
  }

  get tokenExpiresInMinutes(): number {
    if (!this.currentUser?.expiresAt) return 60;
    const diff = this.currentUser.expiresAt - Date.now();
    return Math.max(0, Math.round(diff / 60000));
  }

  // --- CHECKOUT FLOW ---
  startCheckout(): void {
    if (this.cart.length === 0) {
      this.showToast('warning', 'Carrito vacío', 'Agrega productos antes de realizar el checkout.');
      return;
    }

    if (!this.currentUser) {
      this.closeCart();
      this.openAuthModal();
      this.showToast('info', 'Inicio de Sesión Requerido', 'Inicia sesión con IDaaS para continuar tu compra.');
      return;
    }

    this.checkoutStep = 1;
    this.checkoutForm.name = this.currentUser.name;
    this.checkoutForm.email = this.currentUser.email;
    this.isCheckoutModalOpen = true;
    this.closeCart();
  }

  closeCheckoutModal(): void {
    if (this.isProcessing) return;
    this.isCheckoutModalOpen = false;
  }

  proceedToPayment(): void {
    if (!this.checkoutForm.name || !this.checkoutForm.email || !this.checkoutForm.address) {
      alert('Por favor completa todos los campos de envío requeridos.');
      return;
    }
    this.checkoutStep = 2;
  }

  backToShipping(): void {
    this.checkoutStep = 1;
  }

  executeOrder(): void {
    if (!this.currentUser) {
      this.openAuthModal();
      return;
    }

    this.isProcessing = true;
    this.errorMessage = '';
    this.lastOrderResponse = null;
    this.processingStage = 'Validando token IDaaS y preparando payload...';

    const payload = {
      customerName: this.checkoutForm.name || this.currentUser.name,
      customerEmail: this.checkoutForm.email || this.currentUser.email,
      totalAmount: this.totalCart
    };

    setTimeout(() => {
      this.processingStage = 'Enviando orden a Order-Service (Spring Boot :8081)...';

      this.orderService.createOrder(payload).subscribe({
        next: (res) => {
          this.processingStage = '¡Publicado en RabbitMQ (faster360.orders.exchange)!';
          setTimeout(() => {
            this.lastOrderResponse = res;
            this.cart = [];
            this.appliedCoupon = null;
            this.saveCart();
            this.isProcessing = false;
            this.isCheckoutModalOpen = false;
            this.isOrderSuccessOpen = true;
            this.showToast('success', '¡Compra Exitosa!', `Orden #${res.orderNumber} creada y enviada a colas RabbitMQ.`);
          }, 800);
        },
        error: (err) => {
          this.isProcessing = false;
          console.error('Error al procesar orden:', err);
          this.errorMessage = 'No se pudo conectar con el microservicio order-service (:8081). ¿Deseas generar una orden en modo simulación Cloud para testing?';
        }
      });
    }, 700);
  }

  // Generador de orden simulada en caso de que el backend local no esté levantado
  simulateOrderSuccess(): void {
    const randomNum = Math.floor(100000 + Math.random() * 900000);
    const mockOrder: OrderResponse = {
      id: Date.now(),
      orderNumber: `ORD-${randomNum}`,
      customerName: this.checkoutForm.name || this.currentUser?.name || 'Usuario Faster',
      customerEmail: this.checkoutForm.email || this.currentUser?.email || 'cliente@faster360.com',
      totalAmount: this.totalCart,
      status: 'CONFIRMED',
      createdAt: new Date().toISOString()
    };

    this.lastOrderResponse = mockOrder;
    this.cart = [];
    this.appliedCoupon = null;
    this.saveCart();
    this.errorMessage = '';
    this.isCheckoutModalOpen = false;
    this.isOrderSuccessOpen = true;
    this.showToast('success', 'Simulación Exitosa', `Orden #${mockOrder.orderNumber} confirmada.`);
  }

  closeOrderSuccessModal(): void {
    this.isOrderSuccessOpen = false;
  }

  // --- HISTORIAL DE PEDIDOS ---
  openOrdersHistory(): void {
    this.isOrdersHistoryOpen = true;
    this.loadOrders();
  }

  closeOrdersHistory(): void {
    this.isOrdersHistoryOpen = false;
  }

  loadOrders(): void {
    this.isLoadingOrders = true;
    this.orderService.getOrders().subscribe({
      next: (orders) => {
        this.ordersHistory = orders;
        this.isLoadingOrders = false;
      },
      error: (err) => {
        this.isLoadingOrders = false;
        console.warn('Backend order-service no alcanzable para getOrders', err);
        // Si hay una orden reciente en memoria, mostrarla
        if (this.lastOrderResponse && this.ordersHistory.length === 0) {
          this.ordersHistory = [this.lastOrderResponse];
        }
      }
    });
  }

  // --- CLOUD MONITOR DRAWER ---
  openCloudArchitecture(): void {
    this.isCloudArchitectureOpen = true;
  }

  closeCloudArchitecture(): void {
    this.isCloudArchitectureOpen = false;
  }

  // --- RABBITMQ ADMIN MODAL ---
  openAdminQueuesModal(): void {
    this.isAdminQueuesModalOpen = true;
  }

  closeAdminQueuesModal(): void {
    this.isAdminQueuesModalOpen = false;
  }

  get decodedToken(): any {
    if (!this.currentUser?.token) return null;
    try {
      const parts = this.currentUser.token.split('.');
      if (parts.length >= 2) {
        return JSON.parse(atob(parts[1]));
      }
    } catch (e) {
      return null;
    }
    return null;
  }

  // --- TOASTS ---
  showToast(type: 'success' | 'info' | 'warning' | 'error', title: string, message: string, duration = 3500): void {
    const id = ++this.toastCounter;
    this.toasts.push({ id, type, title, message });

    setTimeout(() => {
      this.dismissToast(id);
    }, duration);
  }

  dismissToast(id: number): void {
    this.toasts = this.toasts.filter(t => t.id !== id);
  }
}
