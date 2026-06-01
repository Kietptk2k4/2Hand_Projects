/**
 * When MSW is on, use same-origin relative URLs so the service worker intercepts
 * instead of passthrough to localhost backends that may be offline.
 */
export function resolveServiceBaseUrl(configuredUrl) {
  if (import.meta.env.VITE_USE_MOCK === "true") {
    return "";
  }
  return configuredUrl || "";
}
