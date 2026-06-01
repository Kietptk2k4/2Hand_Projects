import axios from "axios";
import { getStoredAccessToken, refreshAccessTokenOnce } from "./authRefreshService";
import { resolveServiceBaseUrl } from "./resolveServiceBaseUrl";

const COMMERCE_BASE_URL = resolveServiceBaseUrl(import.meta.env.VITE_COMMERCE_SERVICE_BASE_URL);

export const commerceApiClient = axios.create({
  baseURL: COMMERCE_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

commerceApiClient.interceptors.request.use((config) => {
  const token = getStoredAccessToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

commerceApiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error?.config;
    const status = error?.response?.status;

    if (!originalRequest || status !== 401 || originalRequest._retryAttempted) {
      return Promise.reject(error);
    }

    originalRequest._retryAttempted = true;

    try {
      const nextAccessToken = await refreshAccessTokenOnce();
      originalRequest.headers = originalRequest.headers || {};
      originalRequest.headers.Authorization = `Bearer ${nextAccessToken}`;
      return commerceApiClient(originalRequest);
    } catch (refreshError) {
      return Promise.reject(refreshError);
    }
  }
);
