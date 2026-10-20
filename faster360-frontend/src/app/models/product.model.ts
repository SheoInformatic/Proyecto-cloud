export interface Product {
  id: number;
  name: string;
  category: string;
  price: number;
  originalPrice?: number;
  image: string;
  gallery?: string[];
  description: string;
  rating?: number;
  reviewsCount?: number;
  badge?: string;
  badgeType?: 'sale' | 'new' | 'hot' | 'trending';
  stock?: number;
  features?: string[];
  colors?: { name: string; hex: string }[];
  sizes?: string[];
}