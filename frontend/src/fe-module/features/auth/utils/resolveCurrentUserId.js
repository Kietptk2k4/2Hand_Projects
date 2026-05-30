const USER_STORAGE_KEY = "twohands_user";

export function readStoredUser() {
  try {
    const raw = localStorage.getItem(USER_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function persistStoredUser(user) {
  if (user?.id) {
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
  } else {
    localStorage.removeItem(USER_STORAGE_KEY);
  }
}

export function resolveUserIdFromAccessToken(token) {
  if (!token || typeof token !== "string") return null;
  if (token.startsWith("mock-access-")) {
    const id = token.slice("mock-access-".length);
    return id || null;
  }
  return null;
}

/** Resolve viewer UUID from session user, localStorage, or mock JWT shape. */
export function resolveCurrentUserId(sessionUser) {
  if (sessionUser?.id) return sessionUser.id;

  const stored = readStoredUser();
  if (stored?.id) return stored.id;

  const token = localStorage.getItem("twohands_access_token");
  return resolveUserIdFromAccessToken(token);
}
