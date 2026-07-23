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

export function parseRecipientUserIds(raw) {
  const tokens = String(raw || "")
    .split(/[\s,;]+/)
    .map((item) => item.trim())
    .filter(Boolean);

  const invalid = tokens.filter((token) => !/^[0-9a-f-]{36}$/i.test(token));
  return { tokens, invalid };
}

export function buildPublishPayload({ audienceMode, recipientUserIdsRaw, targetAudience }) {
  if (audienceMode === "ALL_ACTIVE_USERS") {
    return { target_audience: "ALL_ACTIVE_USERS" };
  }
  if (audienceMode === "RECIPIENT_LIST") {
    const { tokens } = parseRecipientUserIds(recipientUserIdsRaw);
    return { recipient_user_ids: tokens };
  }
  if (audienceMode === "DEV_FALLBACK") {
    return {};
  }
  if (targetAudience) {
    return { target_audience: targetAudience };
  }
  return {};
}
