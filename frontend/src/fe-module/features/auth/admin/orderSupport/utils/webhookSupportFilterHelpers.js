function startOfTodayLocal() {
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  return now.toISOString();
}

function endOfTodayLocal() {
  const now = new Date();
  now.setHours(23, 59, 59, 999);
  return now.toISOString();
}

export function buildWebhookSupportQuickFilter(presetId, currentFilters = {}) {
  const base = {
    provider: "",
    reference_id: "",
    q: "",
    event_type: "",
    status: "",
    from: "",
    to: "",
    page: 1,
    size: currentFilters.size || "20",
  };

  switch (presetId) {
    case "today":
      return { ...base, from: startOfTodayLocal(), to: endOfTodayLocal() };
    case "pending":
      return { ...base, status: "PENDING" };
    case "invalid_signature":
      return { ...base, status: "INVALID_SIGNATURE", provider: "PAYOS" };
    case "payos":
      return { ...base, provider: "PAYOS" };
    case "ghn":
      return { ...base, provider: "GHN" };
    default:
      return base;
  }
}

export function isWebhookSupportQuickPresetActive(filters, presetId) {
  const preset = buildWebhookSupportQuickFilter(presetId, filters);
  return (
    (filters.provider || "") === (preset.provider || "") &&
    (filters.status || "") === (preset.status || "") &&
    (filters.from || "") === (preset.from || "") &&
    (filters.to || "") === (preset.to || "")
  );
}

export function buildWebhookSupportFilterChips(filters) {
  const chips = [];
  if (filters.provider) {
    chips.push({ key: "provider", label: `NCC: ${filters.provider}` });
  }
  if (filters.reference_id) {
    chips.push({ key: "reference_id", label: `Mã: ${filters.reference_id}` });
  }
  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.event_type) {
    chips.push({ key: "event_type", label: `Sự kiện: ${filters.event_type}` });
  }
  if (filters.status) {
    chips.push({ key: "status", label: `Trạng thái: ${filters.status}` });
  }
  if (filters.from) {
    chips.push({ key: "from", label: `Từ: ${filters.from}` });
  }
  if (filters.to) {
    chips.push({ key: "to", label: `Đến: ${filters.to}` });
  }
  return chips;
}

export function removeWebhookSupportFilterChip(filters, chipKey) {
  return {
    ...filters,
    [chipKey]: "",
    page: 1,
  };
}
