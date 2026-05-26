export function resolveFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (item?.field && !acc[item.field]) {
      acc[item.field] = item.reason || "Truong du lieu khong hop le.";
    }
    return acc;
  }, {});
}
