import { delay, http, HttpResponse } from "msw";
import { mockCommerceCategories } from "../data/commerceCategoryData";
import { mockCommerceProducts } from "../data/commerceProductListData";
import { apiError, apiSuccess } from "../utils/response";

const VALID_SORTS = ["NEWEST", "PRICE_ASC", "PRICE_DESC"];
const MIN_KEYWORD_LENGTH = 2;
const MAX_KEYWORD_LENGTH = 255;

const categoryNameById = new Map(
  mockCommerceCategories.map((cat) => [cat.category_id, cat.category_name])
);

function normalizeKeyword(raw) {
  if (raw == null) return "";
  return String(raw).trim().replace(/\s+/g, " ");
}

function parseKeyword(url) {
  const raw = url.searchParams.get("q");
  const keyword = normalizeKeyword(raw);

  if (!keyword || keyword.length < MIN_KEYWORD_LENGTH || keyword.length > MAX_KEYWORD_LENGTH) {
    return {
      error: apiError("COMMERCE-400-SEARCH-KEYWORD", "Tu khoa tim kiem khong hop le.", [
        { field: "q", reason: "INVALID_KEYWORD_LENGTH" },
      ]),
    };
  }

  return { keyword };
}

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

function enrichProduct(product) {
  return {
    ...product,
    description: product.description || "",
    category_name: product.category_name || categoryNameById.get(product.category_id) || "",
  };
}

function matchesSearch(product, needle) {
  const q = needle.toLowerCase();
  const fields = [
    product.title,
    product.description,
    product.shop_name,
    product.category_name,
  ];
  return fields.some((field) => (field || "").toLowerCase().includes(q));
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

const searchableProducts = mockCommerceProducts.map(enrichProduct);

export const commerceProductSearchHandlers = [
  http.get("*/commerce/api/v1/products/search", async ({ request }) => {
    await delay(400);

    const url = new URL(request.url);
    const keywordResult = parseKeyword(url);
    if (keywordResult.error) {
      return HttpResponse.json(keywordResult.error, { status: 400 });
    }

    const pagination = parsePagination(url);
    if (pagination.error) {
      return HttpResponse.json(pagination.error, { status: 400 });
    }

    const sortResult = parseSort(url);
    if (sortResult.error) {
      return HttpResponse.json(sortResult.error, { status: 400 });
    }

    const { keyword } = keywordResult;
    const { page, limit } = pagination;
    const { sort } = sortResult;

    const filtered = searchableProducts.filter((product) => matchesSearch(product, keyword));
    const sorted = sortProducts(filtered, sort);
    const { items, pagination: paginationResult } = paginateProducts(sorted, page, limit);

    const data = {
      keyword,
      items,
      pagination: paginationResult,
    };

    return HttpResponse.json(apiSuccess(200, "Tim kiem san pham thanh cong.", data), { status: 200 });
  }),
];
