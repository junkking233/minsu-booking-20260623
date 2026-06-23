import http from '@/api/http';

export const dashboardApi = {
  overview() {
    return http.get('/admin/dashboard/overview');
  },

  orderTrend() {
    return http.get('/admin/dashboard/order-trend');
  },

  pendingOrders() {
    return http.get('/admin/dashboard/pending-orders');
  },
};
