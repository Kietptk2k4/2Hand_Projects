import axios from "axios";
import { getStoredAccessToken, refreshAccessTokenOnce } from "./authRefreshService";
import { resolveServiceBaseUrl } from "./resolveServiceBaseUrl";

const AUTH_BASE_URL = resolveServiceBaseUrl(import.meta.env.VITE_AUTH_SERVICE_BASE_URL);

export const apiClient = axios.create({
  baseURL: AUTH_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use((config) => {
  const token = getStoredAccessToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error?.config;
    const status = error?.response?.status;

    if (!originalRequest || status !== 401 || originalRequest._retryAttempted) {
      return Promise.reject(error);
    }

    const url = originalRequest.url || "";
    if (url.includes("/api/v1/auth/refresh")) {
      return Promise.reject(error);
    }

    originalRequest._retryAttempted = true;

    try {
      const nextAccessToken = await refreshAccessTokenOnce();
      originalRequest.headers = originalRequest.headers || {};
      originalRequest.headers.Authorization = `Bearer ${nextAccessToken}`;
      return apiClient(originalRequest);
    } catch (refreshError) {
      return Promise.reject(refreshError);
    }
  }
);

