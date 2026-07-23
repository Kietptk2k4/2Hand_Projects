import {
  REVIEW_MODERATION_LIST_SORT_OPTIONS,
  REVIEW_MODERATION_RATING_OPTIONS,
  REVIEW_MODERATION_STATUS_OPTIONS,
} from "../constants/reviewModerationListConstants.js";
import { getReviewStatusLabel } from "../constants/reviewModerationDisplayLabels.js";

function optionLabel(options, value) {
  return options.find((item) => item.value === value)?.label || value;
}

export function buildReviewModerationActiveFilterChips(filters) {
  if (!filters) return [];

  const chips = [];

  if (filters.q) {
    chips.push({ key: "q", label: `Tìm: ${filters.q}` });
  }
  if (filters.status) {
    chips.push({ key: "status", label: `Trạng thái: ${getReviewStatusLabel(filters.status)}` });
  }
  if (filters.rating) {
    chips.push({ key: "rating", label: `Sao: ${optionLabel(REVIEW_MODERATION_RATING_OPTIONS, filters.rating)}` });
  }
  if (filters.sort && filters.sort !== "NEWEST") {
    chips.push({
      key: "sort",
      label: `Sắp xếp: ${optionLabel(REVIEW_MODERATION_LIST_SORT_OPTIONS, filters.sort)}`,
    });
  }

  return chips;
}

export function removeReviewModerationFilterChip(filters, chipKey) {
  const next = { ...filters, page: "1" };

  if (chipKey === "q") next.q = "";
  if (chipKey === "status") next.status = "";
  if (chipKey === "rating") next.rating = "";
  if (chipKey === "sort") next.sort = "NEWEST";

  return next;
}

export function buildReviewModerationQuickFilter(preset) {
  const base = {
    q: "",
    sort: "NEWEST",
    rating: "",
    page: "1",
  };

  if (preset === "visible") {
    return { ...base, status: "VISIBLE" };
  }
  if (preset === "needs_attention") {
    return { ...base, status: "HIDDEN" };
  }
  if (preset === "low_rating") {
    return { ...base, status: "", rating: "1" };
  }

  return { ...base, status: "" };
}

export function isReviewModerationQuickPresetActive(filters, preset) {
  if (preset === "visible") {
    return filters?.status === "VISIBLE" && !filters?.q && !filters?.rating;
  }
  if (preset === "needs_attention") {
    return filters?.status === "HIDDEN" && !filters?.q && !filters?.rating;
  }
  if (preset === "low_rating") {
    return filters?.rating === "1" && !filters?.q && !filters?.status;
  }
  if (preset === "all") {
    return !filters?.status && !filters?.q && !filters?.rating;
  }
  return false;
}
