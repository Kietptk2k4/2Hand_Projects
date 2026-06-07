export const ENFORCEMENT_REASON_OPTIONS = [
  { value: "POLICY_VIOLATION", label: "Vi phạm chính sách" },
  { value: "SPAM", label: "Spam / nội dung rác" },
  { value: "FRAUD", label: "Gian lận / lừa đảo" },
  { value: "TOS_VIOLATION", label: "Vi phạm điều khoản" },
  { value: "OTHER", label: "Khác" },
];

export const REVOKE_REASON_OPTIONS = [
  { value: "APPEAL_ACCEPTED", label: "Khiếu nại được chấp nhận" },
  { value: "SYSTEM_ERROR", label: "Nhầm lẫn hệ thống" },
  { value: "AMNESTY", label: "Ân xá" },
  { value: "OTHER", label: "Khác" },
];

export const ENFORCEMENT_APPLY_CONFIG = {
  suspend: {
    title: "Đình chỉ người dùng",
    warning:
      "Hành động này sẽ ngăn người dùng đăng nhập và vô hiệu hóa các phiên làm việc hiện tại.",
    reasonLabel: "Lý do đình chỉ",
    durationLabel: "Thời hạn đình chỉ",
    confirmLabel: "Xác nhận đình chỉ",
    confirmClass: "bg-error text-on-error hover:opacity-90",
    supportsTemporary: true,
  },
  restrict: {
    title: "Hạn chế người dùng",
    warning:
      "Người dùng vẫn đăng nhập được nhưng bị chặn các hành động ghi (đăng bài, bình luận, v.v.).",
    reasonLabel: "Lý do hạn chế",
    durationLabel: "Thời hạn hạn chế",
    confirmLabel: "Xác nhận hạn chế",
    confirmClass: "bg-primary text-on-primary hover:opacity-90",
    supportsTemporary: true,
  },
  ban: {
    title: "Cấm người dùng",
    warning:
      "Hành động này cấm người dùng khỏi nền tảng. Thường áp dụng vĩnh viễn cho vi phạm nghiêm trọng.",
    reasonLabel: "Lý do cấm",
    durationLabel: "Thời hạn cấm",
    confirmLabel: "Xác nhận cấm",
    confirmClass: "bg-error text-on-error hover:opacity-90",
    supportsTemporary: true,
  },
};