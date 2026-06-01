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

    const result = getPaymentStatus(paymentId, auth.user.id, { mockPaid });
    if (result.error) return errorResponse(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay trang thai thanh toan thanh cong.", result.data),
      { status: 200 }
    );
  }),
];
