/** Format raw digits as VND display with dot thousands separators (e.g. 800000 → "800.000"). */
export function formatVndInputValue(raw) {
  const digits = String(raw ?? "").replace(/\D/g, "");
  if (!digits) return "";
  return digits.replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

/** Parse formatted VND input to a number, or null when empty/invalid. */
export function parseVndInputToNumber(formatted) {
  if (formatted == null || formatted === "") return null;
  const digits = String(formatted).replace(/\D/g, "");
  if (!digits) return null;
  const n = Number(digits);
  return Number.isFinite(n) ? n : null;
}
