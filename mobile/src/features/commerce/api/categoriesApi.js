import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchActiveCategories({
  minLevel,
  maxLevel,
  leafOnly,
  includeProductCounts = true,
} = {}) {
  try {
    const params = {};
    if (minLevel != null) params.min_level = minLevel;
    if (maxLevel != null) params.max_level = maxLevel;
    if (leafOnly != null) params.leaf_only = leafOnly;
    if (includeProductCounts != null) {
      params.include_product_counts = includeProductCounts;
    }

    const response = await commerceApiClient.get("/commerce/api/v1/categories", { params });
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
