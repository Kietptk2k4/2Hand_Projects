import { delay, http, HttpResponse } from "msw";
import {
  cancelSellerOrderForUser,
  getSellerOrderDetailForUser,
  listSellerOrdersForUser,
  processSellerOrderItemsForUser,
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
  http.get("*/commerce/api/v1/seller/orders/:orderId", async ({ params, request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const result = getSellerOrderDetailForUser(auth.user.id, params.orderId);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay chi tiet don hang seller thanh cong.", result.data),
      { status: 200 },
    );
  }),

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

  http.post("*/commerce/api/v1/seller/order-items/process", async ({ request }) => {
    await delay(400);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = processSellerOrderItemsForUser(auth.user.id, body?.order_item_ids);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Danh dau chuan bi hang thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.post("*/commerce/api/v1/seller/orders/:orderId/cancel", async ({ params, request }) => {
    await delay(400);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    let body = {};
    try {
      body = await request.json();
    } catch {
      body = {};
    }

    const result = cancelSellerOrderForUser(auth.user.id, params.orderId, body?.reason);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, result.message || "Huy don hang thanh cong.", result.data),
      { status: 200 },
    );
  }),
];
