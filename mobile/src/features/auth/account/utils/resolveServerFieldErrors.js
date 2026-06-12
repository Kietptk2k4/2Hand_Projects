const REASON_MESSAGES = {
  INVALID_FORMAT: "Định dạng không hợp lệ.",
  REQUIRED: "Trường này là bắt buộc.",
};

function mapReason(reason) {
  if (!reason) return "Trường dữ liệu không hợp lệ.";
  return REASON_MESSAGES[reason] || String(reason);
}

export function resolveServerFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (item?.field && !acc[item.field]) {
      acc[item.field] = mapReason(item.reason);
    }
    return acc;
  }, {});
}