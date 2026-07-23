const SECRET_KEY_FRAGMENTS = ["PASSWORD", "SECRET", "TOKEN", "API_KEY", "PRIVATE_KEY", "CREDENTIAL"];

export function isSecretLikeConfigKey(configKey) {
  const upper = String(configKey || "").trim().toUpperCase();
  if (!upper) return false;
  return SECRET_KEY_FRAGMENTS.some((fragment) => upper.includes(fragment));
}

export function getDisplayConfigValue(item) {
  if (!item) return "—";
  if (item.valueMasked || isSecretLikeConfigKey(item.configKey)) {
    return "********";
  }
  return item.configValue ?? "—";
}

export function mapApiFieldErrors(errors) {
  if (!Array.isArray(errors)) return {};
  return errors.reduce((acc, entry) => {
    const field = String(entry?.field || "").trim();
    if (!field) return acc;
    const reason = Array.isArray(entry?.reason) ? entry.reason[0] : entry?.reason;
    acc[field] = String(reason || "Giá trị không hợp lệ.");
    return acc;
  }, {});
}
