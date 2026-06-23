import http from '@/api/http';

export interface UserQueryParams {
  keyword?: string;
  status?: number;
  page?: number;
  pageSize?: number;
}

export const userApi = {
  list(params: UserQueryParams) {
    return http.get('/users', { params });
  },

  toggleStatus(id: number, status: number) {
    return http.put(`/users/${id}/status`, { status });
  },
};
