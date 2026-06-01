import { delay, http, HttpResponse } from "msw";
import {
  getOrdersForUser,
  validateOrderListQuery,
} from "../data/commerceOrderListData";
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

export const commerceOrderHandlers = [
  http.get("*/commerce/api/v1/orders", async ({ request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const validation = validateOrderListQuery({
      page: url.searchParams.get("page") ?? "1",
      limit: url.searchParams.get("limit") ?? "20",
      status: url.searchParams.get("status") || undefined,
    });

    if (validation.error) {
      const message =
        validation.error === "COMMERCE-400-VALIDATION"
          ? "Trang thai don hang khong hop le."
          : "Tham so phan trang khong hop le.";
      return HttpResponse.json(apiError(validation.error, message), { status: validation.status });
    }

    const data = getOrdersForUser(auth.user.id, validation);
    return HttpResponse.json(apiSuccess(200, "Lay danh sach don hang thanh cong.", data), {
      status: 200,
    });
  }),
];
