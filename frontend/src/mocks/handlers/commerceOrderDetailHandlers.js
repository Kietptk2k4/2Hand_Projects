import { delay, http, HttpResponse } from "msw";
import {
  cancelOrderForUser,
  confirmOrderReceivedForUser,
  getOrderDetailForUser,
  getOrderTrackStatusForUser,
} from "../data/commerceOrderDetailData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

// Seed/checkout mock IDs use prefix o/p (e.g. o2000000-0000-4000-8000-000000000005)
const ORDER_ID_REGEX =
  /^[0-9a-op][0-9a-f]{7}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function isValidOrderId(orderId) {
  return typeof orderId === "string" && ORDER_ID_REGEX.test(orderId);
}

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

function notFoundOrder() {
  return HttpResponse.json(apiError("COMMERCE-404-ORDER", "Khong tim thay don hang."), {
    status: 404,
  });
}

export const commerceOrderDetailHandlers = [
  http.get("*/commerce/api/v1/orders/:orderId", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const orderId = params.orderId;
    if (!isValidOrderId(orderId)) {
      return notFoundOrder();
    }

    const result = getOrderDetailForUser(auth.user.id, orderId);
    if (result.error) return notFoundOrder();

    return HttpResponse.json(apiSuccess(200, "Lay chi tiet don hang thanh cong.", result.data), {
      status: 200,
    });
  }),

  http.get("*/commerce/api/v1/orders/:orderId/status", async ({ params, request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const orderId = params.orderId;
    if (!isValidOrderId(orderId)) {
      return notFoundOrder();
    }

    const result = getOrderTrackStatusForUser(auth.user.id, orderId);
    if (result.error) return notFoundOrder();

    return HttpResponse.json(apiSuccess(200, "Lay trang thai don hang thanh cong.", result.data), {
      status: 200,
    });
  }),

  http.post("*/commerce/api/v1/orders/:orderId/cancel", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const orderId = params.orderId;
    if (!isValidOrderId(orderId)) {
      return notFoundOrder();
    }

    let body = {};
    try {
      const text = await request.text();
      if (text) body = JSON.parse(text);
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = cancelOrderForUser(auth.user.id, orderId, body?.reason);
    if (result.error) {
      return HttpResponse.json(apiError(result.error, "Khong the huy don hang."), {
        status: result.status,
      });
    }

    return HttpResponse.json(apiSuccess(200, result.message, result.data), { status: 200 });
  }),

  http.post(
    "*/commerce/api/v1/orders/:orderId/confirm-received",
    async ({ params, request }) => {
      await delay(350);
      const auth = requireAuth(request);
      if (auth.error) return auth.error;

      const orderId = params.orderId;
      if (!isValidOrderId(orderId)) {
        return notFoundOrder();
      }

      const result = confirmOrderReceivedForUser(auth.user.id, orderId);
      if (result.error) {
        const messages = {
          "COMMERCE-409-ORDER-ITEMS": "Khong co san pham nao dang cho xac nhan nhan hang.",
          "COMMERCE-409-PAYMENT-STATE": "Thanh toan chua hoan tat. Khong the xac nhan nhan hang.",
          "COMMERCE-409-ORDER-NOT-CANCELLABLE": "Don hang da bi huy.",
        };
        return HttpResponse.json(
          apiError(result.error, messages[result.error] || "Khong the xac nhan nhan hang."),
          { status: result.status },
        );
      }

      return HttpResponse.json(apiSuccess(200, result.message, result.data), { status: 200 });
    },
  ),
];
