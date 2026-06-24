import http from '@/api/http';

export interface CreateReviewData {
  orderNo: string;
  rating: number;
  cleanScore: number;
  locationScore: number;
  serviceScore: number;
  valueScore: number;
  content: string;
  images?: string[];
  anonymous?: boolean;
}

export interface AdminReviewParams {
  status?: string;
  page?: number;
  pageSize?: number;
}

export const reviewApi = {
  create(data: CreateReviewData) {
    return http.post('/reviews', data);
  },

  /** 用户端：查询指定房源的可见评价列表 */
  listByHouse(houseId: number) {
    return http.get(`/reviews/house/${houseId}`);
  },

  adminList(params: AdminReviewParams) {
    return http.get('/admin/reviews', { params });
  },

  adminReply(id: number, reply: string) {
    return http.put(`/admin/reviews/${id}/reply`, { reply });
  },

  adminToggleStatus(id: number, status: string) {
    return http.put(`/admin/reviews/${id}/status`, { status });
  },

  adminDelete(id: number) {
    return http.delete(`/admin/reviews/${id}`);
  },
};
