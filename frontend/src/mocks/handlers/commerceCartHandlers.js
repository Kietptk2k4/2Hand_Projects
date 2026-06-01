import { delay, http, HttpResponse } from "msw";
import {
  getCartResponseForUser,
  getOrCreateCartForUser,
  removeCartItem,
  updateCartItemQuantity,
} from "../data/commerceCartData";
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

export const commerceCartHandlers = [
  http.get("*/commerce/api/v1/cart", async ({ request }) => {
    await delay(400);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const data = getCartResponseForUser(auth.user.id);
    return HttpResponse.json(apiSuccess(200, "Lay gio hang thanh cong.", data), { status: 200 });
  }),

  http.patch("*/commerce/api/v1/cart/items/:cartItemId", async ({ params, request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const cartItemId = params.cartItemId;
    if (!UUID_REGEX.test(cartItemId)) {
      return HttpResponse.json(apiError("COMMERCE-400", "cartItemId khong hop le."), {
        status: 400,
      });
    }

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400-VALIDATION", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const quantity = Number(body?.quantity);
    const cart = getOrCreateCartForUser(auth.user.id);
    const result = updateCartItemQuantity(cart, cartItemId, quantity);

    if (result.error) {
      const messages = {
        "COMMERCE-404-CART-ITEM": "Khong tim thay san pham trong gio hang.",
        "COMMERCE-409-NOT-PURCHASABLE": "San pham khong the cap nhat so luong.",
        "COMMERCE-400-VALIDATION": "So luong khong hop le.",
        "COMMERCE-409-STOCK": "So luong vuot ton kho kha dung.",
      };
      return HttpResponse.json(
        apiError(result.error, messages[result.error] || "Loi cap nhat gio hang."),
        { status: result.status }
      );
    }

    return HttpResponse.json(
      apiSuccess(200, "Cap nhat so luong san pham trong gio hang thanh cong.", result.cart),
      { status: 200 }
    );
  }),

  http.delete("*/commerce/api/v1/cart/items/:cartItemId", async ({ params, request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const cartItemId = params.cartItemId;
    if (!UUID_REGEX.test(cartItemId)) {
      return HttpResponse.json(apiError("COMMERCE-400", "cartItemId khong hop le."), {
        status: 400,
      });
    }

    const cart = getOrCreateCartForUser(auth.user.id);
    const result = removeCartItem(cart, cartItemId);

    if (result.error) {
      return HttpResponse.json(
        apiError("COMMERCE-404-CART-ITEM", "Khong tim thay san pham trong gio hang."),
        { status: 404 }
      );
    }

    return HttpResponse.json(
      apiSuccess(200, "Xoa san pham khoi gio hang thanh cong.", result.cart),
      { status: 200 }
    );
  }),
];
