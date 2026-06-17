import { INVALID_FIELD_MESSAGE } from "../constants/authUiStrings";

export function resolveFieldErrors(errors = []) {
  return errors.reduce((acc, item) => {
    if (item?.field && !acc[item.field]) {
      acc[item.field] = item.reason || INVALID_FIELD_MESSAGE;
    }
    return acc;
  }, {});
}
