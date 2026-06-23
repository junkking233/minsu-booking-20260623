import http from '@/api/http';

export const uploadApi = {
  uploadImage(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return http.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};
