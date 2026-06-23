import { getDevMediaHost } from "../../shared/utils/getDevMediaHost";

export const DEV_SERVICE_PORTS = {
  auth: 3001,
  social: 3002,
  commerce: 3003,
  notification: 3005,
};

/**
 * Resolve a dev service base URL from EXPO_PUBLIC_DEV_HOST (via getDevMediaHost).
 * Explicit env URL wins when set (prod/staging override).
 */
export function resolveDevServiceBaseUrl(serviceKey, explicitEnvUrl) {
  const trimmed = String(explicitEnvUrl || "").trim();
  if (trimmed) {
    return trimmed.replace(/\/+$/, "");
  }

  if (!__DEV__) {
    return "";
  }

  const port = DEV_SERVICE_PORTS[serviceKey];
  if (!port) {
    return "";
  }

  const host = getDevMediaHost();
  return `http://${host}:${port}`;
}
