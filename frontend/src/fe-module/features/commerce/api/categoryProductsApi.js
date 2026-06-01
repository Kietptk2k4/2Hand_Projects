import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchCategoryProducts({
  categoryId,
  page = 1,
  limit = 20,
  sort = "NEWEST",
  includeChildren = true,
} = {}) {
  try {
    const response = await commerceApiClient.get(
      `/commerce/api/v1/categories/${categoryId}/products`,
      {
        params: {
          page,
          limit,
          sort,
          include_children: includeChildren,
        },
      }
    );
    return unwrapResponse(response);
  } catch (error) {
    throw mapAxiosError(error);
  }
}
