import * as SecureStore from "expo-secure-store";

export const APPEARANCE_STORAGE_KEY = "twohands_appearance_mode";

export const APPEARANCE_MODES = ["LIGHT", "DARK", "SYSTEM"];

export function normalizeAppearanceMode(value) {
  const mode = String(value || "SYSTEM").toUpperCase();
  return APPEARANCE_MODES.includes(mode) ? mode : "SYSTEM";
}

export function resolveIsDark(mode, systemScheme) {
  const normalized = normalizeAppearanceMode(mode);
  if (normalized === "DARK") return true;
  if (normalized === "LIGHT") return false;
  return systemScheme === "dark";
}

export async function readStoredAppearanceMode() {
  try {
    return await SecureStore.getItemAsync(APPEARANCE_STORAGE_KEY);
  } catch {
    return null;
  }
}

export async function persistAppearanceMode(mode) {
  try {
    await SecureStore.setItemAsync(APPEARANCE_STORAGE_KEY, normalizeAppearanceMode(mode));
  } catch {
    // ignore storage errors
  }
}
