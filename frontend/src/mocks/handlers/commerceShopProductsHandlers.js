import { delay, http, HttpResponse } from "msw";
import { getPublicShopSummary } from "../data/commerceShopData";
import { mockCommerceProducts } from "../data/commerceProductListData";
import { apiError, apiSuccess } from "../utils/response";

const VALID_SORTS = ["NEWEST", "PRICE_ASC", "PRICE_DESC"];

function parsePagination(url) {
  const pageParam = url.searchParams.get("page");
  const limitParam = url.searchParams.get("limit");

  const page = pageParam === null || pageParam === "" ? 1 : Number(pageParam);
  const limit = limitParam === null || limitParam === "" ? 20 : Number(limitParam);

  if (!Number.isInteger(page) || page < 1) {
    return {
      error: apiError("COMMERCE-400-PAGINATION", "Tham so phan trang khong hop le.", [
        { field: "page", reason: "MUST_BE_GREATER_THAN_OR_EQUAL_TO_1" },
      ]),
    };
  }

  if (!Number.isInteger(limit) || limit < 1 || limit > 50) {
    return {
      error: apiError("COMMERCE-400-PAGINATION", "Tham so phan trang khong hop le.", [
        { field: "limit", reason: "MUST_BE_BETWEEN_1_AND_50" },
      ]),
    };
  }

  return { page, limit };
}

function parseSort(url) {
  const sort = url.searchParams.get("sort") || "NEWEST";
  if (!VALID_SORTS.includes(sort)) {
    return {
      error: apiError("COMMERCE-400-VALIDATION", "Tham so sort khong hop le.", [
        { field: "sort", reason: "INVALID_SORT_VALUE" },
      ]),
    };
  }
  return { sort };
}

function sortProducts(items, sort) {
  const copy = [...items];
  if (sort === "PRICE_ASC") {
    copy.sort((a, b) => a.effective_price - b.effective_price);
    return copy;
  }
  if (sort === "PRICE_DESC") {
    copy.sort((a, b) => b.effective_price - a.effective_price);
    return copy;
  }
  copy.sort((a, b) => new Date(b.created_at) - new Date(a.created_at));
  return copy;
}

function toApiItem(item) {
  const { created_at: _createdAt, ...rest } = item;
  return rest;
}

function paginateProducts(items, page, limit) {
  const totalItems = items.length;
  const totalPages = totalItems === 0 ? 0 : Math.ceil(totalItems / limit);
  const start = (page - 1) * limit;
  const slice = items.slice(start, start + limit).map(toApiItem);

  return {
    items: slice,
    pagination: {
      page,
      limit,
      total_items: totalItems,
      total_pages: totalPages,
      has_next: start + limit < totalItems,
    },
  };
}

export const commerceShopProductsHandlers = [
  http.get("*/commerce/api/v1/shops/:shopId/products", async ({ params, request }) => {
    await delay(400);

    const shopId = params.shopId;
    const shop = getPublicShopSummary(shopId);

    if (!shop) {
      return HttpResponse.json(
        apiError("COMMERCE-404-SHOP", "Shop khong ton tai hoac khong kha dung."),
        { status: 404 }
      );
    }

    const url = new URL(request.url);
    const pagination = parsePagination(url);
    if (pagination.error) {
      return HttpResponse.json(pagination.error, { status: 400 });
    }

    const sortResult = parseSort(url);
    if (sortResult.error) {
      return HttpResponse.json(sortResult.error, { status: 400 });
    }

    const filtered = mockCommerceProducts.filter((product) => product.shop_id === shopId);
    const sorted = sortProducts(filtered, sortResult.sort);
    const { page, limit } = pagination;
    const { items, pagination: paginationResult } = paginateProducts(sorted, page, limit);

    const data = {
      shop,
      items,
      pagination: paginationResult,
    };

    return HttpResponse.json(
      apiSuccess(200, "Lay san pham cua shop thanh cong.", data),
      { status: 200 }
    );
  }),
];
