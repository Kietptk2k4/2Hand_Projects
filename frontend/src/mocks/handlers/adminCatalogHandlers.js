import { delay, http, HttpResponse } from "msw";
import {
  createAdminCatalogBrand,
  createAdminCatalogCategory,
  listAdminCatalogBrands,
  listAdminCatalogCategories,
  setAdminCatalogBrandActive,
  setAdminCatalogCategoryActive,
  updateAdminCatalogBrand,
  updateAdminCatalogCategory,
} from "../data/adminCatalogData";
import { apiError, apiSuccess } from "../utils/response";

function parseOptionalBoolean(value) {
  if (value === null || value === "") return undefined;
  return value === "true";
}

export const adminCatalogHandlers = [
  http.get("*/admin/api/v1/catalog/categories", async ({ request }) => {
    await delay(120);
    const url = new URL(request.url);
    const items = listAdminCatalogCategories({
      isActive: parseOptionalBoolean(url.searchParams.get("is_active")),
      q: url.searchParams.get("q") || undefined,
    });
    return HttpResponse.json(apiSuccess({ items }));
  }),

  http.post("*/admin/api/v1/catalog/categories", async ({ request }) => {
    await delay(150);
    const body = await request.json();
    const result = createAdminCatalogCategory(body);
    if (result.error) {
      return HttpResponse.json(apiError(result.error, result.message), { status: 409 });
    }
    return HttpResponse.json(apiSuccess(201, "Category created successfully", result.data), { status: 201 });
  }),

  http.put("*/admin/api/v1/catalog/categories/:categoryId", async ({ params, request }) => {
    await delay(150);
    const body = await request.json();
    const result = updateAdminCatalogCategory(params.categoryId, body);
    if (result.error) {
      const status = result.error.includes("404") ? 404 : 409;
      return HttpResponse.json(apiError(result.error, result.message), { status });
    }
    return HttpResponse.json(apiSuccess(200, "Category updated successfully", result.data));
  }),

  http.post("*/admin/api/v1/catalog/categories/:categoryId/activate", async ({ params }) => {
    await delay(120);
    const result = setAdminCatalogCategoryActive(params.categoryId, true);
    if (result.error) {
      return HttpResponse.json(apiError(result.error, result.message), { status: 404 });
    }
    return HttpResponse.json(apiSuccess(200, "Category activated successfully", result.data));
  }),

  http.post("*/admin/api/v1/catalog/categories/:categoryId/deactivate", async ({ params }) => {
    await delay(120);
    const result = setAdminCatalogCategoryActive(params.categoryId, false);
    if (result.error) {
      const status = result.error.includes("IN-USE") ? 409 : 404;
      return HttpResponse.json(apiError(result.error, result.message), { status });
    }
    return HttpResponse.json(apiSuccess(200, "Category deactivated successfully", result.data));
  }),

  http.get("*/admin/api/v1/catalog/brands", async ({ request }) => {
    await delay(120);
    const url = new URL(request.url);
    const data = listAdminCatalogBrands({
      isActive: parseOptionalBoolean(url.searchParams.get("is_active")),
      q: url.searchParams.get("q") || undefined,
      page: Number(url.searchParams.get("page") || 1),
      limit: Number(url.searchParams.get("limit") || 20),
    });
    return HttpResponse.json(apiSuccess(data));
  }),

  http.post("*/admin/api/v1/catalog/brands", async ({ request }) => {
    await delay(150);
    const body = await request.json();
    const result = createAdminCatalogBrand(body);
    if (result.error) {
      return HttpResponse.json(apiError(result.error, result.message), { status: 409 });
    }
    return HttpResponse.json(apiSuccess(201, "Brand created successfully", result.data), { status: 201 });
  }),

  http.put("*/admin/api/v1/catalog/brands/:brandId", async ({ params, request }) => {
    await delay(150);
    const body = await request.json();
    const result = updateAdminCatalogBrand(params.brandId, body);
    if (result.error) {
      const status = result.error.includes("404") ? 404 : 409;
      return HttpResponse.json(apiError(result.error, result.message), { status });
    }
    return HttpResponse.json(apiSuccess(200, "Brand updated successfully", result.data));
  }),

  http.post("*/admin/api/v1/catalog/brands/:brandId/activate", async ({ params }) => {
    await delay(120);
    const result = setAdminCatalogBrandActive(params.brandId, true);
    if (result.error) {
      return HttpResponse.json(apiError(result.error, result.message), { status: 404 });
    }
    return HttpResponse.json(apiSuccess(200, "Brand activated successfully", result.data));
  }),

  http.post("*/admin/api/v1/catalog/brands/:brandId/deactivate", async ({ params }) => {
    await delay(120);
    const result = setAdminCatalogBrandActive(params.brandId, false);
    if (result.error) {
      const status = result.error.includes("PROTECTED") ? 409 : 404;
      return HttpResponse.json(apiError(result.error, result.message), { status });
    }
    return HttpResponse.json(apiSuccess(200, "Brand deactivated successfully", result.data));
  }),
];
