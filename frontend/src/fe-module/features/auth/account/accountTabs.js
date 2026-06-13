export const ACCOUNT_TABS = [
  {
    id: "info",
    labelEn: "Account Info",
    labelVn: "Thông tin tài khoản",
    icon: "account",
  },
  {
    id: "edit",
    labelEn: "Edit Profile",
    labelVn: "Chỉnh sửa hồ sơ",
    icon: "edit",
  },
  {
    id: "avatar",
    labelEn: "Update Avatar",
    labelVn: "Ảnh hồ sơ",
    icon: "avatar",
  },
  {
    id: "privacy",
    labelEn: "Privacy",
    labelVn: "Quyền riêng tư",
    icon: "privacy",
  },
  {
    id: "settings",
    labelEn: "Settings",
    labelVn: "Cài đặt",
    icon: "settings",
  },
  {
    id: "notifications",
    labelEn: "Notifications",
    labelVn: "Thông báo",
    icon: "notifications",
  },
  {
    id: "delete",
    labelEn: "Delete Account",
    labelVn: "Xóa tài khoản",
    icon: "delete",
    danger: true,
  },
];

export const ACCOUNT_TAB_IDS = ACCOUNT_TABS.map((tab) => tab.id);
