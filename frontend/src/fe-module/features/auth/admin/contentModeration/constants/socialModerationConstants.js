import { ADMIN_CONTENT_MODERATION_QA } from "../../../../../shared/constants/adminContentModerationQaIds.js";

export const REASON_MAX_LENGTH = 1000;

export const MODERATION_ACTIONS = {
  HIDE: "HIDE",
  REMOVE: "REMOVE",
};

export const ACTION_LABELS = {
  HIDE: "An",
  REMOVE: "Go",
};

export const HIDE_WARNING =
  "Noi dung se an khoi feed cong khai. Trang thai cuoi cung do Social Service quyet dinh sau khi nhan event.";

export const REMOVE_WARNING =
  "Go noi dung vi pham. Admin Service chi ghi log va publish event; Social Service ap dung trang thai.";

export const RESTORE_WARNING =
  "Khoi phuc noi dung da bi moderation. Social Service co the tu choi neu vi pham chinh sach van con.";

export const MOCK_SOCIAL_MODERATION_IDS = {
  POST: ADMIN_CONTENT_MODERATION_QA.post.sample,
  COMMENT: ADMIN_CONTENT_MODERATION_QA.comment.sample,
  NOT_FOUND: ADMIN_CONTENT_MODERATION_QA.post.notFound,
};

export const SOCIAL_MODERATION_ERROR_MESSAGES = {
  "ADMIN-401": "Phien dang nhap khong hop le.",
  "ADMIN-403": "Ban khong co quyen thuc hien thao tac nay.",
  "ADMIN-400-VALIDATION": "Vui long kiem tra du lieu nhap.",
  "ADMIN-404": "Khong tim thay noi dung tren Social.",
  "ADMIN-409": "Social Service tu choi thao tac o trang thai hien tai.",
};

export function mapSocialModerationApiError(error) {
  const code = String(error?.code ?? "");
  return (
    SOCIAL_MODERATION_ERROR_MESSAGES[code] ||
    error?.message ||
    "Co loi xay ra. Vui long thu lai."
  );
}

export function buildModerateSuccessToast(targetLabel, action) {
  const verb = action === MODERATION_ACTIONS.HIDE ? "an" : "go";
  return `Da ${verb} ${targetLabel} thanh cong.`;
}

export function buildRestoreSuccessToast(targetLabel) {
  return `Da khoi phuc ${targetLabel} thanh cong.`;
}