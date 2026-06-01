const STORAGE_KEY = "commerce:search-history";
const MAX_ITEMS = 10;

function readRaw() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.filter((item) => typeof item === "string") : [];
  } catch {
    return [];
  }
}

export function getCommerceSearchHistory() {
  return readRaw();
}

export function addCommerceSearchHistory(keyword) {
  const trimmed = keyword?.trim();
  if (!trimmed) return;

  const next = [trimmed, ...readRaw().filter((item) => item.toLowerCase() !== trimmed.toLowerCase())].slice(
    0,
    MAX_ITEMS
  );
  localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
}

export function clearCommerceSearchHistory() {
  localStorage.removeItem(STORAGE_KEY);
}
