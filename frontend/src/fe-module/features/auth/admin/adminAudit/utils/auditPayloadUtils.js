const HIGHLIGHT_KEYS = new Set([
  "reason",
  "status",
  "message",
  "action",
  "result",
  "enforcement_id",
  "moderation_log_id",
  "before",
  "after",
]);

export function extractAuditPayloadHighlights(payload) {
  if (!payload || typeof payload !== "object" || Array.isArray(payload)) {
    return [];
  }

  return Object.entries(payload)
    .filter(([key]) => HIGHLIGHT_KEYS.has(key))
    .map(([key, value]) => ({
      key,
      value: typeof value === "object" ? JSON.stringify(value) : String(value),
    }));
}
