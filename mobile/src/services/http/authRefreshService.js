import axios from "axios";
import {
  clearSessionTokens,
  getAccessToken,
  getRefreshToken,
  setSessionTokens,
} from "../auth/tokenStorage";
import { resolveServiceBaseUrl } from "./resolveServiceBaseUrl";

const AUTH_BASE_URL = resolveServiceBaseUrl(process.env.EXPO_PUBLIC_AUTH_SERVICE_BASE_URL);
const DEFAULT_SESSION_EXPIRED_MESSAGE = "Phien dang nhap da het han, vui long dang nhap lai.";

let onSessionExpiredHandler = () => {};

let refreshPromise = null;

export function configureAuthRefreshService({ onSessionExpired } = {}) {
  if (typeof onSessionExpired === "function") {
    onSessionExpiredHandler = onSessionExpired;
  }
}

export async function getStoredAccessToken() {
  return getAccessToken();
}

function normalizeRefreshError(error) {
  const status = error?.response?.status || 500;
  const payload = error?.response?.data;
  return {
    code: payload?.code || status,
    message: payload?.message || DEFAULT_SESSION_EXPIRED_MESSAGE,
    errors: payload?.errors || null,
  };
}

function triggerSessionExpired(message) {
  onSessionExpiredHandler(message || DEFAULT_SESSION_EXPIRED_MESSAGE);
}

export async function refreshAccessTokenOnce() {
  if (refreshPromise) {
    return refreshPromise;
  }

  refreshPromise = (async () => {
    const refreshToken = await getRefreshToken();
    if (!refreshToken) {
      const missingTokenError = {
        code: 401,
        message: DEFAULT_SESSION_EXPIRED_MESSAGE,
      };
      await clearSessionTokens();
      triggerSessionExpired(missingTokenError.message);
      throw missingTokenError;
    }

    try {
      const response = await axios.post(
        `${AUTH_BASE_URL}/api/v1/auth/refresh`,
        { refresh_token: refreshToken },
        { headers: { "Content-Type": "application/json" } }
      );
      const payload = response?.data;

      if (!payload?.success || !payload?.data?.access_token) {
        throw {
          response: {
            status: payload?.code || 500,
            data: payload,
          },
        };
      }

      await setSessionTokens({
        accessToken: payload.data.access_token,
        refreshToken: payload.data.refresh_token || refreshToken,
      });
      return payload.data.access_token;
    } catch (error) {
      const mapped = normalizeRefreshError(error);
      if (mapped.code === 401) {
        await clearSessionTokens();
        triggerSessionExpired(mapped.message);
      }
      throw mapped;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}
