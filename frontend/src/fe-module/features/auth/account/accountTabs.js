export const ACCOUNT_TABS = [
  {
    id: "info",
    labelEn: "Account Info",
    labelVn: "Thong tin tai khoan",
    icon: "account",
  },
  {
    id: "edit",
    labelEn: "Edit Profile",
    labelVn: "Chinh sua ho so",
    icon: "edit",
  },
  {
    id: "avatar",
    labelEn: "Update Avatar",
    labelVn: "Anh dai dien",
    icon: "avatar",
  },
  {
    id: "privacy",
    labelEn: "Privacy",
    labelVn: "Quyen rieng tu",
    icon: "privacy",
  },
  {
    id: "settings",
    labelEn: "Settings",
    labelVn: "Cai dat",
    icon: "settings",
  },
  {
    id: "delete",
    labelEn: "Delete Account",
    labelVn: "Xoa tai khoan",
    icon: "delete",
    danger: true,
  },
];

export const ACCOUNT_TAB_IDS = ACCOUNT_TABS.map((tab) => tab.id);
