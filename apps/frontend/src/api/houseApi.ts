import http from '@/api/http';
import { formatDateParam } from '@/utils/date';

export interface HouseSearchParams {
  keyword?: string;
  city?: string;
  checkIn?: string;
  checkOut?: string;
  guests?: number;
  minPrice?: number;
  maxPrice?: number;
  type?: string;
  sort?: string;
  page?: number;
  pageSize?: number;
}

export interface AdminHouseParams {
  keyword?: string;
  status?: string;
  page?: number;
  pageSize?: number;
}

export interface HouseFormData {
  name: string;
  city: string;
  price: number;
  cleanFee: number;
  type: string;
  rooms: string;
  area: string;
  capacity: number;
  address: string;
  host: string;
  coverImage?: string;
  facilities?: string[];
  description?: string;
  rule?: string;
}

export const houseApi = {
  search(params: HouseSearchParams) {
    return http.get('/houses', {
      params: {
        ...params,
        checkIn: params.checkIn ? formatDateParam(params.checkIn) : undefined,
        checkOut: params.checkOut ? formatDateParam(params.checkOut) : undefined,
      },
    });
  },

  getDetail(id: number, params?: { checkIn?: string; checkOut?: string }) {
    return http.get(`/houses/${id}`, {
      params: params ? {
        ...params,
        checkIn: params.checkIn ? formatDateParam(params.checkIn) : undefined,
        checkOut: params.checkOut ? formatDateParam(params.checkOut) : undefined,
      } : undefined,
    });
  },

  checkAvailability(id: number, params: { checkIn: string; checkOut: string; guests: number }) {
    return http.get(`/houses/${id}/availability`, {
      params: {
        ...params,
        checkIn: formatDateParam(params.checkIn),
        checkOut: formatDateParam(params.checkOut),
      },
    });
  },

  adminList(params: AdminHouseParams) {
    return http.get('/admin/houses', { params });
  },

  adminCreate(data: HouseFormData) {
    return http.post('/admin/houses', data);
  },

  adminUpdate(id: number, data: HouseFormData) {
    return http.put(`/admin/houses/${id}`, data);
  },

  adminToggleStatus(id: number, status: string) {
    return http.put(`/admin/houses/${id}/status`, { status });
  },

  adminDelete(id: number) {
    return http.delete(`/admin/houses/${id}`);
  },
};
