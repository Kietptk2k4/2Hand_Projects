export const APPEARANCE_STORAGE_KEY = "twohands_appearance_mode";

export const APPEARANCE_MODES = ["LIGHT", "DARK", "SYSTEM"];

export function normalizeAppearanceMode(value) {
  const mode = String(value || "SYSTEM").toUpperCase();
  return APPEARANCE_MODES.includes(mode) ? mode : "SYSTEM";
}

export function resolveIsDark(mode) {
  const normalized = normalizeAppearanceMode(mode);
  if (normalized === "DARK") return true;
  if (normalized === "LIGHT") return false;
  if (typeof window === "undefined" || !window.matchMedia) {
    return false;
  }
  return window.matchMedia("(prefers-color-scheme: dark)").matches;
}

export function applyAppearanceToDocument(mode) {
  if (typeof document === "undefined") return;

  const normalized = normalizeAppearanceMode(mode);
  const root = document.documentElement;
  const isDark = resolveIsDark(normalized);

  root.classList.toggle("dark", isDark);
  root.style.colorScheme = isDark ? "dark" : "light";
  root.dataset.appearanceMode = normalized;
}

export function readStoredAppearanceMode() {
  try {
    return localStorage.getItem(APPEARANCE_STORAGE_KEY);
  } catch {
    return null;
  }
}

export function persistAppearanceMode(mode) {
  try {
    localStorage.setItem(APPEARANCE_STORAGE_KEY, normalizeAppearanceMode(mode));
  } catch {
    // ignore quota / private mode
  }
}

export function bootstrapAppearanceFromStorage() {
  const stored = readStoredAppearanceMode();
  if (stored) {
    applyAppearanceToDocument(stored);
  }
}