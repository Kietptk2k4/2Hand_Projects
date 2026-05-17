import axios from "axios";

const AUTH_BASE_URL = import.meta.env.VITE_AUTH_SERVICE_BASE_URL || "";
const DEFAULT_SESSION_EXPIRED_MESSAGE = "Phien dang nhap da het han, vui long dang nhap lai.";

let handlers = {
  getAccessToken: () => localStorage.getItem("twohands_access_token"),
  getRefreshToken: () => localStorage.getItem("twohands_refresh_token"),
  setAccessToken: (token) => {
    if (token) {
      localStorage.setItem("twohands_access_token", token);
    } else {
      localStorage.removeItem("twohands_access_token");
    }
  },
  clearSession: () => {
    localStorage.removeItem("twohands_access_token");
    localStorage.removeItem("twohands_refresh_token");
  },
  onSessionExpired: () => {},
};

let refreshPromise = null;

export function configureAuthRefreshService(nextHandlers = {}) {
  handlers = {
    ...handlers,
    ...nextHandlers,
  };
}

export function getStoredAccessToken() {
  return handlers.getAccessToken?.() || null;
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
  handlers.onSessionExpired?.(message || DEFAULT_SESSION_EXPIRED_MESSAGE);
}

export async function refreshAccessTokenOnce() {
  if (refreshPromise) {
    return refreshPromise;
  }

  refreshPromise = (async () => {
    const refreshToken = handlers.getRefreshToken?.() || null;
    if (!refreshToken) {
      const missingTokenError = {
        code: 401,
        message: DEFAULT_SESSION_EXPIRED_MESSAGE,
      };
      handlers.clearSession?.();
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

      handlers.setAccessToken?.(payload.data.access_token);
      return payload.data.access_token;
    } catch (error) {
      const mapped = normalizeRefreshError(error);
      if (mapped.code === 401) {
        handlers.clearSession?.();
        triggerSessionExpired(mapped.message);
      }
      throw mapped;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}
