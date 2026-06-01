import { delay, http, HttpResponse } from "msw";
import {
  buildCheckoutQuote,
  buildShippingFee,
  processCheckout,
} from "../data/commerceCheckoutData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

const UUID_REGEX =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

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

function parseCheckoutBody(body) {
  const cartItemIds = body?.cart_item_ids;
  const addressId = body?.address_id;

  if (!Array.isArray(cartItemIds) || cartItemIds.length === 0) {
    return {
      error: HttpResponse.json(
        apiError("COMMERCE-400-VALIDATION", "cart_item_ids khong hop le."),
        { status: 400 }
      ),
    };
  }

  if (!addressId || !UUID_REGEX.test(addressId)) {
    return {
      error: HttpResponse.json(
        apiError("COMMERCE-400-VALIDATION", "address_id khong hop le."),
        { status: 400 }
      ),
    };
  }

  for (const id of cartItemIds) {
    if (!UUID_REGEX.test(id)) {
      return {
        error: HttpResponse.json(
          apiError("COMMERCE-400-VALIDATION", "cart_item_id khong hop le."),
          { status: 400 }
        ),
      };
    }
  }

  return {
    payload: {
      cartItemIds,
      addressId,
      shipmentType: body?.shipment_type || "STANDARD",
      paymentMethod: body?.payment_method,
      idempotencyKey: body?.idempotency_key,
    },
  };
}

function errorResponse(result) {
  const messages = {
    "COMMERCE-404-CART-ITEM": "Khong tim thay san pham trong gio hang.",
    "COMMERCE-404-ADDRESS": "Dia chi khong ton tai.",
    "COMMERCE-409-NOT-PURCHASABLE": "San pham khong the thanh toan.",
    "COMMERCE-400-VALIDATION": "Du lieu khong hop le.",
    "COMMERCE-400-PAYMENT-METHOD": "Phuong thuc thanh toan khong hop le.",
  };
  return HttpResponse.json(
    apiError(result.error, messages[result.error] || "Loi checkout."),
    { status: result.status }
  );
}

export const commerceCheckoutHandlers = [
  http.post("*/commerce/api/v1/checkout/quote", async ({ request }) => {
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

    const parsed = parseCheckoutBody(body);
    if (parsed.error) return parsed.error;

    const result = buildCheckoutQuote(auth.user.id, parsed.payload);
    if (result.error) return errorResponse(result);

    return HttpResponse.json(
      apiSuccess(200, "Tinh tong tien don hang thanh cong.", result.data),
      { status: 200 }
    );
  }),

  http.post("*/commerce/api/v1/shipping/fee", async ({ request }) => {
    await delay(350);
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

    const parsed = parseCheckoutBody(body);
    if (parsed.error) return parsed.error;

    const result = buildShippingFee(auth.user.id, parsed.payload);
    if (result.error) return errorResponse(result);

    return HttpResponse.json(
      apiSuccess(200, "Tinh phi van chuyen thanh cong.", result.data),
      { status: 200 }
    );
  }),

  http.post("*/commerce/api/v1/checkout", async ({ request }) => {
    await delay(500);
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

    const parsed = parseCheckoutBody(body);
    if (parsed.error) return parsed.error;

    const result = processCheckout(auth.user.id, body);
    if (result.error) return errorResponse(result);

    const message = result.idempotent
      ? "Don hang da duoc tao truoc do (idempotency)."
      : "Checkout thanh cong.";

    return HttpResponse.json(apiSuccess(200, message, result.data), { status: 200 });
  }),
];
