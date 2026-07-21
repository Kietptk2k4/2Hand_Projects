import axios from "axios";
import { getStoredAccessToken, refreshAccessTokenOnce } from "./authRefreshService";
import { attachNgrokGatewayInterceptor } from "./ngrokGatewayHeaders";
import { resolveServiceBaseUrl } from "./resolveServiceBaseUrl";

const NOTIFICATION_BASE_URL = resolveServiceBaseUrl(import.meta.env.VITE_NOTIFICATION_SERVICE_BASE_URL);

export const notificationApiClient = axios.create({
  baseURL: NOTIFICATION_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

attachNgrokGatewayInterceptor(notificationApiClient);

notificationApiClient.interceptors.request.use((config) => {
  const token = getStoredAccessToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

notificationApiClient.interceptors.response.use(
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
      return notificationApiClient(originalRequest);
    } catch (refreshError) {
      return Promise.reject(refreshError);
    }
  }
);
