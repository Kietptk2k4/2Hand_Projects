import { delay, http, HttpResponse } from "msw";
import {
  listSellerOrdersForUser,
  validateSellerOrderListQuery,
} from "../data/commerceSellerOrderData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

function requireAuth(request) {
  const user = getUserByToken(request);
  if (!user) {
    return {
      error: HttpResponse.json(apiError("COMMERCE-401", "Authentication required."), {
        status: 401,
      }),
    };
  }
  return { user };
}

function mapError(result) {
  return HttpResponse.json(apiError(result.error, result.message || "Co loi xay ra."), {
    status: result.status,
  });
}

export const commerceSellerOrderHandlers = [
  http.get("*/commerce/api/v1/seller/orders", async ({ request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const validated = validateSellerOrderListQuery({
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
      status: url.searchParams.get("status") || undefined,
      shipment_status: url.searchParams.get("shipment_status") || undefined,
    });

    if (validated.error) return mapError(validated);

    const result = listSellerOrdersForUser(auth.user.id, validated);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach don seller thanh cong.", result.data),
      { status: 200 },
    );
  }),
];
