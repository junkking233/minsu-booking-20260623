import http from '@/api/http';

export const favoriteApi = {
  add(houseId: number) {
    return http.post(`/favorites/${houseId}`);
  },

  remove(houseId: number) {
    return http.delete(`/favorites/${houseId}`);
  },

  myList(params: { page?: number; pageSize?: number }) {
    return http.get('/favorites/my', { params });
  },

  check(houseId: number) {
    return http.get(`/favorites/check/${houseId}`);
  },
};
