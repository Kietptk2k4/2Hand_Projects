import axios from "axios";
import { getStoredAccessToken, refreshAccessTokenOnce } from "./authRefreshService";
import { attachNgrokGatewayInterceptor } from "./ngrokGatewayHeaders";
import { resolveServiceBaseUrl } from "./resolveServiceBaseUrl";

const ADMIN_BASE_URL = resolveServiceBaseUrl(import.meta.env.VITE_ADMIN_SERVICE_BASE_URL);

export const adminApiClient = axios.create({
  baseURL: ADMIN_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

attachNgrokGatewayInterceptor(adminApiClient);

adminApiClient.interceptors.request.use((config) => {
  const token = getStoredAccessToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

adminApiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error?.config;
    const status = error?.response?.status;

    if (!originalRequest || status !== 401 || originalRequest._retryAttempted) {
      return Promise.reject(error);
    }

    const url = originalRequest.url || "";
    if (url.includes("/auth/") && url.includes("/refresh")) {
      return Promise.reject(error);
    }

    originalRequest._retryAttempted = true;

    try {
      const newToken = await refreshAccessTokenOnce();
      originalRequest.headers = originalRequest.headers || {};
      originalRequest.headers.Authorization = `Bearer ${newToken}`;
      return adminApiClient(originalRequest);
    } catch (refreshError) {
      return Promise.reject(refreshError);
    }
  },
);
