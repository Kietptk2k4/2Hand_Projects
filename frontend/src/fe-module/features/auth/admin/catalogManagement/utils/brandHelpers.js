/**
 * Sprint 2 API enrichments (not blocking FE):
 * - is_protected on list items
 * - GET /brands/{id}, total_pages/has_next
 * - block deactivate when product_count > 0
 * - GET /brands/stats, sort query param
 */

export const PROTECTED_BRAND_SLUG = "khac";

export const BRAND_PAGE_SIZE = 20;

export function isProtectedBrand(item) {
  return String(item?.slug ?? "").toLowerCase() === PROTECTED_BRAND_SLUG;
}

export function statusFilterToIsActive(statusFilter) {
  if (statusFilter === "active") return true;
  if (statusFilter === "inactive") return false;
  return undefined;
}

export function parseBrandPage(raw, fallback = 1) {
  const page = Number.parseInt(String(raw ?? ""), 10);
  return Number.isFinite(page) && page > 0 ? page : fallback;
}

export function getBrandEmptyMessage(statusFilter, query) {
  if (query?.trim()) {
    return `Không có thương hiệu khớp "${query.trim()}".`;
  }
  if (statusFilter === "active") {
    return "Không có thương hiệu đang hoạt động.";
  }
  if (statusFilter === "inactive") {
    return "Không có thương hiệu vô hiệu.";
  }
  return "Chưa có thương hiệu nào.";
}

export function buildBrandPaginationSummary({ page, limit, totalItems }) {
  if (!totalItems) return "Không có thương hiệu";
  const start = (page - 1) * limit + 1;
  const end = Math.min(page * limit, totalItems);
  return `Hiển thị ${start}–${end} / ${totalItems} thương hiệu`;
}

export function computeBrandHeroMetrics({ total = 0, active = 0, inactive = 0, totalProducts = 0 }) {
  return {
    total,
    activeCount: active,
    inactiveCount: inactive,
    totalProducts,
  };
}
