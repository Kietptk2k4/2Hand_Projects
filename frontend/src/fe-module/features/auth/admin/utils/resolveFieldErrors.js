export function resolveFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (item?.field && !acc[item.field]) {
      acc[item.field] = item.reason || "Truong dữ liệu không hợp lệ.";
    }
    return acc;
  }, {});
}
