import {
  POST_MODERATION_LIST_MODERATION_STATUS_OPTIONS,
  POST_MODERATION_LIST_SORT_OPTIONS,
  POST_MODERATION_LIST_STATUS_OPTIONS,
} from "../constants/postModerationListConstants.js";
import {
  getPostModerationStatusLabel,
  getPostStatusLabel,
} from "../constants/postModerationDisplayLabels.js";

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value;
}

export function buildPostModerationActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.status) {
    chips.push({ key: "status", label: `Trạng thái: ${getPostStatusLabel(filters.status)}` });
  }
  if (filters.moderation_status) {
    chips.push({
      key: "moderation_status",
      label: `Kiểm duyệt: ${getPostModerationStatusLabel(filters.moderation_status)}`,
    });
  }
  if (filters.sort && filters.sort !== "created_at") {
    chips.push({
      key: "sort",
      label: `Sắp xếp: ${optionLabel(POST_MODERATION_LIST_SORT_OPTIONS, filters.sort)}`,
    });
  }

  return chips;
}

export function removePostModerationFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "q") next.q = "";
  if (chipKey === "status") next.status = "";
  if (chipKey === "moderation_status") next.moderation_status = "";
  if (chipKey === "sort") next.sort = "created_at";

  return next;
}

export function buildPostModerationQuickFilter(preset) {
  const base = {
    q: "",
    sort: "created_at",
    page: "1",
  };

  if (preset === "hidden") {
    return { ...base, status: "", moderation_status: "HIDDEN" };
  }
  if (preset === "removed") {
    return { ...base, status: "", moderation_status: "REMOVED" };
  }
  if (preset === "deleted") {
    return { ...base, status: "DELETED", moderation_status: "" };
  }
  if (preset === "active") {
    return { ...base, status: "ACTIVE", moderation_status: "NONE" };
  }

  return { ...base, status: "", moderation_status: "" };
}

export function isPostModerationQuickPresetActive(filters, preset) {
  if (preset === "hidden") {
    return filters?.moderation_status === "HIDDEN" && !filters?.status && !filters?.q;
  }
  if (preset === "removed") {
    return filters?.moderation_status === "REMOVED" && !filters?.status && !filters?.q;
  }
  if (preset === "deleted") {
    return filters?.status === "DELETED" && !filters?.moderation_status && !filters?.q;
  }
  if (preset === "active") {
    return (
      filters?.status === "ACTIVE" &&
      filters?.moderation_status === "NONE" &&
      !filters?.q
    );
  }
  return false;
}
