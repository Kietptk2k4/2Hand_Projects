export const NOTIFICATION_LIST_TABS = {
  ALL: "all",
  UNREAD: "unread",
};

export const NOTIFICATION_POLL_INTERVAL_MS = 60_000;

export const NOTIFICATION_NO_DEEP_LINK_MESSAGE =
  "Thông báo này không có liên kết chi tiết.";

export const NOTIFICATION_EVENT_GROUPS = [
  {
    id: "social",
    label: "M\u1ea1ng x\u00e3 h\u1ed9i",
    eventTypes: [
      "POST_LIKED",
      "USER_FOLLOWED",
      "COMMENT_CREATED",
      "COMMENT_REPLIED",
      "COMMENT_LIKED",
    ],
  },
  {
    id: "commerce",
    label: "Mua s\u1eafm",
    eventTypes: [
      "ORDER_CREATED",
      "PAYMENT_SUCCESS",
      "PAYMENT_FAILED",
      "SHIPMENT_CREATED",
      "SHIPMENT_READY_TO_SHIP",
      "SHIPMENT_SHIPPED",
      "SHIPMENT_DELIVERED",
      "ORDER_COMPLETED",
      "REVIEW_REMINDER",
      "REVIEW_REPLIED",
    ],
  },
  {
    id: "account",
    label: "T\u00e0i kho\u1ea3n & b\u1ea3o m\u1eadt",
    eventTypes: [
      "PASSWORD_CHANGED",
      "USER_SUSPENDED",
      "USER_RESTRICTED",
      "USER_ENFORCEMENT_REVOKED",
      "USER_ENFORCEMENT_EXPIRED",
    ],
  },
  {
    id: "seller",
    label: "Ng\u01b0\u1eddi b\u00e1n",
    eventTypes: ["PRODUCT_REMOVED", "REVIEW_HIDDEN", "SHOP_SUSPENDED", "SHOP_CLOSED"],
  },
  {
    id: "system",
    label: "H\u1ec7 th\u1ed1ng",
    eventTypes: ["SYSTEM_ANNOUNCEMENT_SENT", "SYSTEM_ANNOUNCEMENT_CANCELLED"],
  },
];

export const NOTIFICATION_EVENT_LABELS = {
  USER_CREATED: "T\u00e0i kho\u1ea3n \u0111\u01b0\u1ee3c t\u1ea1o",
  EMAIL_VERIFICATION_REQUESTED: "X\u00e1c minh email",
  PASSWORD_RESET_REQUESTED: "\u0110\u1eb7t l\u1ea1i m\u1eadt kh\u1ea9u",
  PASSWORD_CHANGED: "\u0110\u1ed5i m\u1eadt kh\u1ea9u",
  POST_LIKED: "Th\u00edch b\u00e0i vi\u1ebft",
  USER_FOLLOWED: "Theo d\u00f5i",
  COMMENT_CREATED: "B\u00ecnh lu\u1eadn m\u1edbi",
  COMMENT_REPLIED: "Tr\u1ea3 l\u1eddi b\u00ecnh lu\u1eadn",
  COMMENT_LIKED: "Th\u00edch b\u00ecnh lu\u1eadn",
  ORDER_CREATED: "\u0110\u01a1n h\u00e0ng m\u1edbi",
  PAYMENT_SUCCESS: "Thanh to\u00e1n th\u00e0nh c\u00f4ng",
  PAYMENT_FAILED: "Thanh to\u00e1n th\u1ea5t b\u1ea1i",
  SHIPMENT_CREATED: "T\u1ea1o v\u1eadn \u0111\u01a1n",
  SHIPMENT_READY_TO_SHIP: "\u0110\u01a1n s\u1eb5n s\u00e0ng giao",
  SHIPMENT_SHIPPED: "\u0110\u00e3 giao cho \u0111\u01a1n v\u1ecb v\u1eadn chuy\u1ec3n",
  SHIPMENT_DELIVERED: "Giao h\u00e0ng th\u00e0nh c\u00f4ng",
  ORDER_COMPLETED: "Ho\u00e0n t\u1ea5t \u0111\u01a1n h\u00e0ng",
  REVIEW_REMINDER: "Nh\u1eafc \u0111\u00e1nh gi\u00e1",
  REVIEW_REPLIED: "Shop ph\u1ea3n h\u1ed3i \u0111\u00e1nh gi\u00e1",
  USER_SUSPENDED: "T\u00e0i kho\u1ea3n b\u1ecb \u0111\u00ecnh ch\u1ec9",
  USER_RESTRICTED: "T\u00e0i kho\u1ea3n b\u1ecb h\u1ea1n ch\u1ebf",
  PRODUCT_REMOVED: "S\u1ea3n ph\u1ea9m b\u1ecb g\u1ee1",
  REVIEW_HIDDEN: "\u0110\u00e1nh gi\u00e1 b\u1ecb \u1ea9n",
  SHOP_SUSPENDED: "C\u1eeda h\u00e0ng b\u1ecb \u0111\u00ecnh ch\u1ec9",
  SHOP_CLOSED: "C\u1eeda h\u00e0ng \u0111\u00f3ng",
  SYSTEM_ANNOUNCEMENT_SENT: "Th\u00f4ng b\u00e1o h\u1ec7 th\u1ed1ng",
  SYSTEM_ANNOUNCEMENT_CANCELLED: "H\u1ee7y th\u00f4ng b\u00e1o h\u1ec7 th\u1ed1ng",
  USER_ENFORCEMENT_REVOKED: "G\u1ee1 h\u1ea1n ch\u1ebf t\u00e0i kho\u1ea3n",
  USER_ENFORCEMENT_EXPIRED: "H\u1ebft h\u1ea1n h\u1ea1n ch\u1ebf",
};

export const ANNOUNCEMENT_SEVERITY_STYLES = {
  INFO: "border-primary/30 bg-primary/5 text-on-surface",
  WARNING: "border-amber-400/50 bg-amber-50 text-on-surface",
  CRITICAL: "border-error/40 bg-error-container text-on-error-container",
};
