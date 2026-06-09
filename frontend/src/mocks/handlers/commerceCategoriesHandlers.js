import { delay, http, HttpResponse } from "msw";
import {
  countProductsForCategory,
  getSidebarCategories,
  mockCommerceCategories,
} from "../data/commerceCategoryData";
import { apiError, apiSuccess } from "../utils/response";

function parseBoolean(value, defaultValue) {
  if (value === null || value === "") return defaultValue;
  return value === "true";
}

function parseOptionalInt(value) {
  if (value === null || value === "") return null;
  const parsed = Number(value);
  return Number.isInteger(parsed) ? parsed : NaN;
}

function hasActiveChild(categoryId) {
  return mockCommerceCategories.some(
    (cat) => cat.is_active && cat.parent_id === categoryId
  );
}

function buildCategoryItems({ minLevel, maxLevel, leafOnly, includeProductCounts }) {
  return mockCommerceCategories
    .filter((cat) => cat.is_active)
    .filter((cat) => minLevel == null || cat.level >= minLevel)
    .filter((cat) => maxLevel == null || cat.level <= maxLevel)
    .filter((cat) => !leafOnly || !hasActiveChild(cat.category_id))
    .map((cat) => ({
      category_id: cat.category_id,
      category_name: cat.category_name,
      category_slug: cat.category_slug,
      parent_id: cat.parent_id,
      level: cat.level,
      is_leaf: !hasActiveChild(cat.category_id),
      product_count: includeProductCounts
        ? countProductsForCategory(cat.category_id, true)
        : 0,
    }));
}

export const commerceCategoriesHandlers = [
  http.get("*/commerce/api/v1/categories", async ({ request }) => {
    await delay(120);

    const url = new URL(request.url);
    const minLevel = parseOptionalInt(url.searchParams.get("min_level"));
    const maxLevel = parseOptionalInt(url.searchParams.get("max_level"));
    const leafOnly = parseBoolean(url.searchParams.get("leaf_only"), false);
    const includeProductCounts = parseBoolean(
      url.searchParams.get("include_product_counts"),
      true
    );

    if (Number.isNaN(minLevel)) {
      return HttpResponse.json(
        apiError("COMMERCE-400-VALIDATION", "Tham so min_level khong hop le.", [
          { field: "min_level", reason: "INVALID_INTEGER" },
        ]),
        { status: 400 }
      );
    }

    if (Number.isNaN(maxLevel)) {
      return HttpResponse.json(
        apiError("COMMERCE-400-VALIDATION", "Tham so max_level khong hop le.", [
          { field: "max_level", reason: "INVALID_INTEGER" },
        ]),
        { status: 400 }
      );
    }

    if (minLevel != null && maxLevel != null && minLevel > maxLevel) {
      return HttpResponse.json(
        apiError("COMMERCE-400-VALIDATION", "min_level phai nho hon hoac bang max_level.", [
          { field: "min_level", reason: "OUT_OF_RANGE" },
        ]),
        { status: 400 }
      );
    }

    const items = buildCategoryItems({
      minLevel,
      maxLevel,
      leafOnly,
      includeProductCounts,
    });

    if (
      minLevel == null &&
      maxLevel == null &&
      !leafOnly &&
      includeProductCounts
    ) {
      const sidebar = getSidebarCategories();
      return HttpResponse.json(
        apiSuccess({
          items: sidebar.map((item) => ({
            category_id: item.categoryId,
            category_name: item.categoryName,
            category_slug: item.categorySlug,
            parent_id: item.parentId,
            level: mockCommerceCategories.find((cat) => cat.category_id === item.categoryId)?.level ?? 0,
            is_leaf: !hasActiveChild(item.categoryId),
            product_count: item.productCount,
          })),
        })
      );
    }

    return HttpResponse.json(apiSuccess({ items }));
  }),
];
