import axios, { type AxiosError, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios';
import { clearAuthState, getAuthToken } from '@/utils/auth';

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
}

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAuthToken();
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

instance.interceptors.response.use(
  (res) => {
    const body = res.data as ApiResult<unknown>;
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) {
        return body.data;
      }
      return Promise.reject(new Error(body.message || '请求失败'));
    }
    return res.data;
  },
  (err: AxiosError<ApiResult<unknown>>) => {
    if (err.response?.status === 401) {
      clearAuthState();
    }
    const message = err.response?.data?.message || err.message || '请求失败';
    return Promise.reject(new Error(message));
  },
);

const http = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.get(url, config) as unknown as Promise<T>;
  },
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return instance.post(url, data, config) as unknown as Promise<T>;
  },
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return instance.put(url, data, config) as unknown as Promise<T>;
  },
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.delete(url, config) as unknown as Promise<T>;
  },
};

export default http;
