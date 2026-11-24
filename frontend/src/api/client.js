import axios from 'axios';

// All requests go to /api (proxied to Spring Boot in dev) unless VITE_API_URL is set.
const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || '/api' });

// Attach the JWT to every request.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('devtrack_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// If the token is rejected, log out and go to the login page.
api.interceptors.response.use(
  (res) => res,
  (error) => {
    const isAuthCall = error.config?.url?.startsWith('/auth/');
    if (error.response?.status === 401 && !isAuthCall) {
      localStorage.removeItem('devtrack_token');
      localStorage.removeItem('devtrack_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/** Turns an axios error into a readable message (uses the backend's JSON error when present). */
export function errorMessage(error) {
  const data = error.response?.data;
  if (data?.fieldErrors && Object.keys(data.fieldErrors).length) {
    return Object.entries(data.fieldErrors).map(([f, m]) => `${f}: ${m}`).join(', ');
  }
  return data?.message || (error.request ? 'Cannot reach the server. Is the backend running?' : error.message);
}

export default api;
