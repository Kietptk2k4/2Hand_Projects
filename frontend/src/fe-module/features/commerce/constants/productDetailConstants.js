export const CONDITION_LABELS = {
  LIKE_NEW: "Như mới",
  GOOD: "Tốt",
  FAIR: "Khá",
  USED: "Đã qua sử dụng",
};

export function getConditionLabel(condition) {
  if (!condition) return "";
  return CONDITION_LABELS[condition] || condition;
}
