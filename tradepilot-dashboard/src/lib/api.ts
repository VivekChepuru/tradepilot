import axios from 'axios';
import { Order, DashboardStats } from '@/types';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const ordersApi = {
  getAll: () => api.get<Order[]>('/api/orders'),
  getByStatus: (status: string) =>
    api.get<Order[]>(`/api/orders?status=${status}`),
  updateStatus: (id: number, status: string) =>
    api.patch(`/api/orders/${id}/status`, { status }),
};

export const statsApi = {
  get: () => api.get<DashboardStats>('/api/dashboard/stats'),
};

export default api;