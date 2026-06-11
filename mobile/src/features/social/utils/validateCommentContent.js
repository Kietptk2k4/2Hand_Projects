import { MAX_COMMENT_LENGTH } from "../constants/commentConstants";

export function validateCommentContent(text) {
  const trimmed = text?.trim() ?? "";
  if (!trimmed) {
    return { valid: false, message: "Nội dung bình luận không được để trống." };
  }
  if (trimmed.length > MAX_COMMENT_LENGTH) {
    return {
      valid: false,
      message: `Bình luận tối đa ${MAX_COMMENT_LENGTH} ký tự.`,
    };
  }
  return { valid: true, value: trimmed };
}
