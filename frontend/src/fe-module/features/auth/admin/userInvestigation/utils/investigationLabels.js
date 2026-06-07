const USER_STATUS_LABELS = {
  ACTIVE: "Đang hoạt động",
  SUSPENDED: "Đình chỉ",
  BANNED: "Bị cấm",
  PENDING_VERIFICATION: "Chờ xác thực",
  DELETED: "Đã xóa",
};

const USER_STATUS_CLASS = {
  ACTIVE: "bg-green-50 text-green-800 border-green-200",
  SUSPENDED: "bg-amber-50 text-amber-900 border-amber-200",
  BANNED: "bg-red-50 text-red-800 border-red-200",
  PENDING_VERIFICATION: "bg-gray-100 text-gray-800 border-gray-200",
  DELETED: "bg-gray-100 text-gray-600 border-gray-200",
};

const ENFORCEMENT_ACTION_LABELS = {
  SUSPEND: "Đình chỉ",
  BAN: "Cấm",
  RESTRICT: "Hạn chế",
  WARNING: "Cảnh báo",
};

const ENFORCEMENT_ACTION_CLASS = {
  SUSPEND: "bg-red-600 text-white",
  BAN: "bg-red-700 text-white",
  RESTRICT: "bg-amber-600 text-white",
  WARNING: "bg-amber-500 text-white",
};

const ENFORCEMENT_STATUS_LABELS = {
  ACTIVE: "Đang hiệu lực",
  REVOKED: "Đã thu hồi",
  EXPIRED: "Đã hết hạn",
};

const ENFORCEMENT_STATUS_CLASS = {
  ACTIVE: "bg-green-50 text-green-800 border-green-200",
  REVOKED: "bg-gray-100 text-gray-700 border-gray-200",
  EXPIRED: "bg-gray-100 text-gray-600 border-gray-200",
};

export function getUserStatusLabel(status) {
  return USER_STATUS_LABELS[status] || status || "—";
}

export function getUserStatusClass(status) {
  return USER_STATUS_CLASS[status] || "bg-gray-100 text-gray-800 border-gray-200";
}

export function getEnforcementActionLabel(actionType) {
  return ENFORCEMENT_ACTION_LABELS[actionType] || actionType || "—";
}

export function getEnforcementActionClass(actionType) {
  return ENFORCEMENT_ACTION_CLASS[actionType] || "bg-gray-600 text-white";
}

export function getEnforcementStatusLabel(status) {
  return ENFORCEMENT_STATUS_LABELS[status] || status || "—";
}

export function getEnforcementStatusClass(status) {
  return ENFORCEMENT_STATUS_CLASS[status] || "bg-gray-100 text-gray-700 border-gray-200";
}

export function getActorTypeLabel(actorType) {
  if (actorType === "SYSTEM") return "Hệ thống";
  if (actorType === "ADMIN") return "Admin";
  return actorType || "—";
}