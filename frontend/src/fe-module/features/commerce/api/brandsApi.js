import { commerceApiClient } from "../../../services/http/commerceApiClient";
import { mapAxiosError, unwrapResponse } from "./commerceApiResponse";

export async function fetchActiveBrands() {
  try {
    const response = await commerceApiClient.get("/commerce/api/v1/brands");
    const data = unwrapResponse(response);
    return (data?.items || []).map((item) => ({
      id: item.brand_id ?? item.brandId ?? item.id,
      name: item.brand_name ?? item.brandName ?? item.name,
      slug: item.brand_slug ?? item.brandSlug ?? item.slug,
    }));
  } catch (error) {
    throw mapAxiosError(error);
  }
}
