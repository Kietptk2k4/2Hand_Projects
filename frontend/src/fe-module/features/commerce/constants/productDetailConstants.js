export const CONDITION_LABELS = {
  NEW: "Mới",
  LIKE_NEW: "Như mới",
  GOOD: "Tốt",
  FAIR: "Khá",
};

export function getConditionLabel(condition) {
  if (!condition) return "";
  return CONDITION_LABELS[condition] || condition;
}
