import http from '@/api/http';

export const availabilityApi = {
  getCalendar(houseId: number, month: string) {
    return http.get(`/admin/houses/${houseId}/availability`, { params: { yearMonth: month } });
  },

  toggle(houseId: number, date: string) {
    return http.put(`/admin/houses/${houseId}/availability/toggle`, { date });
  },
};
