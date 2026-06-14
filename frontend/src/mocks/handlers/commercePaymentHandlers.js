import { delay, http, HttpResponse } from "msw";
import {
  createOrGetPayOsUrl,
  getPaymentStatus,
  UUID_REGEX,
} from "../data/commercePaymentData";
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

function errorResponse(result) {
  const messages = {
    "COMMERCE-404-PAYMENT": "Khong tim thay thanh toan.",
    "COMMERCE-409-PAYMENT-STATE": "Trang thai thanh toan khong hop le.",
    "COMMERCE-409-ORDER-AWAITING-PAYMENT": "Don hang khong cho thanh toan.",
    "COMMERCE-400-PAYMENT-METHOD": "Phuong thuc thanh toan khong hop le.",
    "COMMERCE-503-PAYOS": "PayOS tam thoi khong kha dung.",
  };
  return HttpResponse.json(
    apiError(result.error, messages[result.error] || "Loi thanh toan."),
    { status: result.status }
  );
}

export const commercePaymentHandlers = [
  http.post(
    "*/commerce/api/v1/payments/:paymentId/vnpay-checkout-url",
    async ({ params, request }) => {
      await delay(400);
      const auth = requireAuth(request);
      if (auth.error) return auth.error;

      const paymentId = params.paymentId;
      if (!UUID_REGEX.test(paymentId)) {
        return HttpResponse.json(apiError("COMMERCE-404-PAYMENT", "Payment khong hop le."), {
          status: 404,
        });
      }

      const result = createOrGetPayOsUrl(paymentId, auth.user.id);
      if (result.error) return errorResponse(result);

      return HttpResponse.json(
        apiSuccess(200, "Tao link thanh toan VNPay thanh cong.", {
          payment_id: paymentId,
          order_id: result.data.order_id,
          txn_ref: `mock-txn-${Date.now()}`,
          redirect: `https://mock.vnpay.local/checkout?payment_id=${paymentId}`,
        }),
        { status: 200 }
      );
    }
  ),

  http.post(
    "*/commerce/api/v1/orders/:orderId/payments/vnpay/retry",
    async ({ params, request }) => {
      await delay(400);
      const auth = requireAuth(request);
      if (auth.error) return auth.error;

      const orderId = params.orderId;
      if (!UUID_REGEX.test(orderId)) {
        return HttpResponse.json(apiError("COMMERCE-404-ORDER", "Don hang khong hop le."), {
          status: 404,
        });
      }

      return HttpResponse.json(
        apiSuccess(200, "Tao lai link thanh toan VNPay thanh cong.", {
          order_id: orderId,
          payment_id: `p1000000-0000-4000-8000-${Date.now().toString().slice(-11)}`,
          txn_ref: `${orderId}-${Date.now()}`,
          redirect: `https://mock.vnpay.local/checkout?order_id=${orderId}`,
        }),
        { status: 200 }
      );
    }
  ),

  http.post(
    "*/commerce/api/v1/payments/:paymentId/payos-checkout-url",
    async ({ params, request }) => {
      await delay(400);
      const auth = requireAuth(request);
      if (auth.error) return auth.error;

      const paymentId = params.paymentId;
      if (!UUID_REGEX.test(paymentId)) {
        return HttpResponse.json(apiError("COMMERCE-404-PAYMENT", "Payment khong hop le."), {
          status: 404,
        });
      }

      const result = createOrGetPayOsUrl(paymentId, auth.user.id);
      if (result.error) return errorResponse(result);

      const message = result.reused
        ? "Lay lai link thanh toan payOS thanh cong."
        : "Tao link thanh toan payOS thanh cong.";

      return HttpResponse.json(apiSuccess(200, message, result.data), { status: 200 });
    }
  ),

  http.get("*/commerce/api/v1/payments/:paymentId/status", async ({ params, request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const paymentId = params.paymentId;
    if (!UUID_REGEX.test(paymentId)) {
      return HttpResponse.json(apiError("COMMERCE-404-PAYMENT", "Payment khong hop le."), {
        status: 404,
      });
    }

    const url = new URL(request.url);
    const mockPaid = url.searchParams.get("mockPaid") === "1";
    const mockFailed = url.searchParams.get("mockFailed") === "1";
    const mockExpired = url.searchParams.get("mockExpired") === "1";

    const result = getPaymentStatus(paymentId, auth.user.id, {
      mockPaid,
      mockFailed,
      mockExpired,
    });
    if (result.error) return errorResponse(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay trang thai thanh toan thanh cong.", result.data),
      { status: 200 }
    );
  }),
];
