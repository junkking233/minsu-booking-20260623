import http from '@/api/http';
import type { AuthUser } from '@/utils/auth';

export interface LoginResponse {
  token: string;
  user: AuthUser;
  expiresAt: number;
}

export interface RegisterResponse {
  id: number;
  username: string;
  name: string;
  role: string;
  status: number;
}

export const authApi = {
  login(data: { username: string; password: string }): Promise<LoginResponse> {
    return http.post<LoginResponse>('/auth/login', data);
  },

  register(data: { username: string; password: string; name?: string }): Promise<RegisterResponse> {
    return http.post<RegisterResponse>('/auth/register', data);
  },

  me(): Promise<AuthUser> {
    return http.get<AuthUser>('/auth/me');
  },

  changePassword(data: { oldPassword: string; newPassword: string }): Promise<void> {
    return http.put<void>('/auth/password', data);
  },

  updateProfile(data: { name?: string; phone?: string; avatar?: string }): Promise<AuthUser> {
    return http.put<AuthUser>('/auth/profile', data);
  },
};
