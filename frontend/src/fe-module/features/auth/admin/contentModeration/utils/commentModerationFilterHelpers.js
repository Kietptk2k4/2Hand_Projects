import {
  COMMENT_MODERATION_LIST_MODERATION_STATUS_OPTIONS,
  COMMENT_MODERATION_LIST_SORT_OPTIONS,
  COMMENT_MODERATION_LIST_STATUS_OPTIONS,
} from "../constants/commentModerationListConstants.js";
import {
  getCommentModerationStatusLabel,
  getCommentStatusLabel,
} from "../constants/commentModerationDisplayLabels.js";

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value;
}

export function buildCommentModerationActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.post_id) {
    chips.push({ key: "post_id", label: `Bài viết: ${filters.post_id}` });
  }
  if (filters.status) {
    chips.push({ key: "status", label: `Trạng thái: ${getCommentStatusLabel(filters.status)}` });
  }
  if (filters.moderation_status) {
    chips.push({
      key: "moderation_status",
      label: `Kiểm duyệt: ${getCommentModerationStatusLabel(filters.moderation_status)}`,
    });
  }
  if (filters.sort && filters.sort !== "created_at") {
    chips.push({
      key: "sort",
      label: `Sắp xếp: ${optionLabel(COMMENT_MODERATION_LIST_SORT_OPTIONS, filters.sort)}`,
    });
  }

  return chips;
}

export function removeCommentModerationFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "q") next.q = "";
  if (chipKey === "post_id") next.post_id = "";
  if (chipKey === "status") next.status = "";
  if (chipKey === "moderation_status") next.moderation_status = "";
  if (chipKey === "sort") next.sort = "created_at";

  return next;
}

export function buildCommentModerationQuickFilter(preset) {
  const base = {
    q: "",
    post_id: "",
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

export function isCommentModerationQuickPresetActive(filters, preset) {
  if (preset === "hidden") {
    return filters?.moderation_status === "HIDDEN" && !filters?.status && !filters?.q && !filters?.post_id;
  }
  if (preset === "removed") {
    return filters?.moderation_status === "REMOVED" && !filters?.status && !filters?.q && !filters?.post_id;
  }
  if (preset === "deleted") {
    return filters?.status === "DELETED" && !filters?.moderation_status && !filters?.q && !filters?.post_id;
  }
  if (preset === "active") {
    return (
      filters?.status === "ACTIVE" &&
      filters?.moderation_status === "NONE" &&
      !filters?.q &&
      !filters?.post_id
    );
  }
  return false;
}
