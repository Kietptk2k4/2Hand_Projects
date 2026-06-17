import * as SecureStore from "expo-secure-store";

const STORAGE_KEY = "commerce:search-history";
const MAX_ITEMS = 10;

async function readRaw() {
  try {
    const raw = await SecureStore.getItemAsync(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.filter((item) => typeof item === "string") : [];
  } catch {
    return [];
  }
}

async function writeRaw(items) {
  try {
    await SecureStore.setItemAsync(STORAGE_KEY, JSON.stringify(items));
  } catch {
    // ignore storage errors
  }
}

export async function getCommerceSearchHistory() {
  return readRaw();
}

export async function addCommerceSearchHistory(keyword) {
  const trimmed = keyword?.trim();
  if (!trimmed) return;

  const existing = await readRaw();
  const next = [
    trimmed,
    ...existing.filter((item) => item.toLowerCase() !== trimmed.toLowerCase()),
  ].slice(0, MAX_ITEMS);
  await writeRaw(next);
}

export async function clearCommerceSearchHistory() {
  try {
    await SecureStore.deleteItemAsync(STORAGE_KEY);
  } catch {
    // ignore storage errors
  }
}
