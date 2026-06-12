import { ROUTES } from "../../../../shared/constants/routes";

export const ACCOUNT_HUB_MENU_ITEMS = [
  {
    id: "info",
    label: "Thông tin tài khoản",
    route: ROUTES.accountInfo,
    icon: "person-outline",
  },
  {
    id: "edit",
    label: "Chỉnh sửa hồ sơ",
    route: ROUTES.accountEdit,
    icon: "create-outline",
  },
  {
    id: "avatar",
    label: "Ảnh đại diện",
    route: ROUTES.accountAvatar,
    icon: "image-outline",
  },
  {
    id: "privacy",
    label: "Quyền riêng tư",
    route: ROUTES.accountPrivacy,
    icon: "lock-closed-outline",
  },
  {
    id: "settings",
    label: "Cài đặt",
    route: ROUTES.accountSettings,
    icon: "settings-outline",
  },
  {
    id: "delete",
    label: "Xóa tài khoản",
    route: ROUTES.accountDelete,
    icon: "trash-outline",
    danger: true,
  },
];

export const ACCOUNT_HUB_LOGOUT_ITEM = {
  id: "logout",
  label: "Đăng xuất",
  icon: "log-out-outline",
};
