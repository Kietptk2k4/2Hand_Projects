import axios from "axios";
import { getStoredAccessToken, refreshAccessTokenOnce } from "./authRefreshService";
import { resolveDevServiceBaseUrl } from "./resolveDevServiceBaseUrl";

const AUTH_BASE_URL = resolveDevServiceBaseUrl("auth", process.env.EXPO_PUBLIC_AUTH_SERVICE_BASE_URL);

export const authApiClient = axios.create({
  baseURL: AUTH_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

authApiClient.interceptors.request.use(async (config) => {
  const token = await getStoredAccessToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

authApiClient.interceptors.response.use(
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
      return authApiClient(originalRequest);
    } catch (refreshError) {
      return Promise.reject(refreshError);
    }
  }
);
