import { adminApiClient } from "../../../../../services/http/adminApiClient";
import { mapAxiosError, unwrapResponse } from "../../../../commerce/api/commerceApiResponse";

export async function fetchAdminCategories({ isActive, q } = {}) {
  try {
    const params = {};
    if (isActive !== undefined && isActive !== null && isActive !== "") {
      params.is_active = isActive;
    }
    if (q) params.q = q;
    const response = await adminApiClient.get("/admin/api/v1/catalog/categories", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function createAdminCategory(payload) {
  try {
    const response = await adminApiClient.post("/admin/api/v1/catalog/categories", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateAdminCategory(categoryId, payload) {
  try {
    const response = await adminApiClient.put(`/admin/api/v1/catalog/categories/${categoryId}`, payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function activateAdminCategory(categoryId) {
  try {
    const response = await adminApiClient.post(`/admin/api/v1/catalog/categories/${categoryId}/activate`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function deactivateAdminCategory(categoryId) {
  try {
    const response = await adminApiClient.post(`/admin/api/v1/catalog/categories/${categoryId}/deactivate`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function fetchAdminBrands({ isActive, q, page = 1, limit = 20 } = {}) {
  try {
    const params = { page, limit };
    if (isActive !== undefined && isActive !== null && isActive !== "") {
      params.is_active = isActive;
    }
    if (q) params.q = q;
    const response = await adminApiClient.get("/admin/api/v1/catalog/brands", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function createAdminBrand(payload) {
  try {
    const response = await adminApiClient.post("/admin/api/v1/catalog/brands", payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function updateAdminBrand(brandId, payload) {
  try {
    const response = await adminApiClient.put(`/admin/api/v1/catalog/brands/${brandId}`, payload);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function activateAdminBrand(brandId) {
  try {
    const response = await adminApiClient.post(`/admin/api/v1/catalog/brands/${brandId}/activate`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}

export async function deactivateAdminBrand(brandId) {
  try {
    const response = await adminApiClient.post(`/admin/api/v1/catalog/brands/${brandId}/deactivate`);
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
