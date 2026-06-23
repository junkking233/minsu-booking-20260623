import http from '@/api/http';

export interface SystemSettings {
  defaultDiscount: number;
  payTimeoutMinutes: number;
  freeCancelHours: number;
  reviewAvailableDays: number;
}

export const configApi = {
  cities() {
    return http.get('/config/cities');
  },

  tags() {
    return http.get('/config/tags');
  },

  adminAddCity(name: string) {
    return http.post('/admin/config/cities', { name });
  },

  adminDeleteCity(id: number) {
    return http.delete(`/admin/config/cities/${id}`);
  },

  adminAddTag(name: string) {
    return http.post('/admin/config/tags', { name });
  },

  adminDeleteTag(id: number) {
    return http.delete(`/admin/config/tags/${id}`);
  },

  adminGetSettings() {
    return http.get('/admin/config/settings');
  },

  adminUpdateSettings(data: SystemSettings) {
    return http.put('/admin/config/settings', data);
  },
};
