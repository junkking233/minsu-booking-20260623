import http from '@/api/http';

export interface CreateOrderData {
  houseId: number;
  checkIn: string;
  checkOut: string;
  guests: number;
  guestName: string;
  phone: string;
  note?: string;
}

export interface OrderQueryParams {
  status?: string;
  page?: number;
  pageSize?: number;
}

export interface AdminOrderParams extends OrderQueryParams {
  keyword?: string;
}

export const orderApi = {
  create(data: CreateOrderData) {
    return http.post('/orders', data);
  },

  myOrders(params: OrderQueryParams) {
    return http.get('/orders/my', { params });
  },

  getDetail(orderNo: string) {
    return http.get(`/orders/${orderNo}`);
  },

  cancel(orderNo: string) {
    return http.put(`/orders/${orderNo}/cancel`);
  },

  pay(orderNo: string) {
    return http.put(`/orders/${orderNo}/pay`);
  },

  adminList(params: AdminOrderParams) {
    return http.get('/admin/orders', { params });
  },

  adminConfirm(orderNo: string) {
    return http.put(`/admin/orders/${orderNo}/confirm`);
  },

  adminReject(orderNo: string) {
    return http.put(`/admin/orders/${orderNo}/reject`);
  },

  adminCheckin(orderNo: string) {
    return http.put(`/admin/orders/${orderNo}/checkin`);
  },

  adminComplete(orderNo: string) {
    return http.put(`/admin/orders/${orderNo}/complete`);
  },

  adminRefund(orderNo: string) {
    return http.put(`/admin/orders/${orderNo}/refund`);
  },

  adminRefundApprove(orderNo: string) {
    return http.put(`/admin/orders/${orderNo}/refund/approve`);
  },

  adminRefundReject(orderNo: string) {
    return http.put(`/admin/orders/${orderNo}/refund/reject`);
  },
};
