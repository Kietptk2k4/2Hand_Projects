import { APP_ROUTES } from "../../../shared/constants/routes";
import { normalizeSearchKeyword } from "./normalizeSearchKeyword";

export function buildCommerceSearchPath(keyword) {
  const normalized = normalizeSearchKeyword(keyword);
  if (!normalized) return APP_ROUTES.commerceSearch;
  return `${APP_ROUTES.commerceSearch}?q=${encodeURIComponent(normalized)}`;
}
