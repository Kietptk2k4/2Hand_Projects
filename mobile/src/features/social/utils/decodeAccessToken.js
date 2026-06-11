export function decodeAccessTokenPayload(token) {
  if (!token || typeof token !== "string") return null;

  const parts = token.split(".");
  if (parts.length < 2) return null;

  try {
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64 + "=".repeat((4 - (base64.length % 4)) % 4);
    if (typeof globalThis.atob !== "function") return null;
    const json = globalThis.atob(padded);
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function resolveUserIdFromAccessToken(token) {
  const payload = decodeAccessTokenPayload(token);
  return payload?.sub || null;
}
