import axios from "axios";
import { isSuspendedWriteError } from "../../features/social/utils/socialWriteErrors";
import { notifySuspendedWrite } from "../../features/social/utils/socialWriteBlockBridge";
import { getStoredAccessToken, refreshAccessTokenOnce } from "./authRefreshService";
import { resolveServiceBaseUrl } from "./resolveServiceBaseUrl";

const SOCIAL_BASE_URL = resolveServiceBaseUrl(import.meta.env.VITE_SOCIAL_SERVICE_BASE_URL);

export const socialApiClient = axios.create({
  baseURL: SOCIAL_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

socialApiClient.interceptors.request.use((config) => {
  const token = getStoredAccessToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

socialApiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error?.config;
    const status = error?.response?.status;

    if (isSuspendedWriteError(error)) {
      const message = error?.response?.data?.message;
      notifySuspendedWrite(message);
      return Promise.reject(error);
    }

    if (!originalRequest || status !== 401 || originalRequest._retryAttempted) {
      return Promise.reject(error);
    }

    originalRequest._retryAttempted = true;

    try {
      const nextAccessToken = await refreshAccessTokenOnce();
      originalRequest.headers = originalRequest.headers || {};
      originalRequest.headers.Authorization = `Bearer ${nextAccessToken}`;
      return socialApiClient(originalRequest);
    } catch (refreshError) {
      return Promise.reject(refreshError);
    }
  }
);
