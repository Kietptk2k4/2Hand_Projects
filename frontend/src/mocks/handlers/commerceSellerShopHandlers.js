import { delay, http, HttpResponse } from "msw";
import {
  createShopForUser,
  getMyShopForSeller,
  updateShopProfileForSeller,
  updateShopVacationForSeller,
} from "../data/commerceSellerShopData";
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
  const payload = apiError(result.error, result.message || "Co loi xay ra.");
  if (result.data) {
    payload.data = result.data;
  }
  return HttpResponse.json(payload, { status: result.status });
}

export const commerceSellerShopHandlers = [
  /** FE-only until backend exposes GET /seller/shop */
  http.get("*/commerce/api/v1/seller/shop", async ({ request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const result = getMyShopForSeller(auth.user.id);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay thong tin shop thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.patch("*/commerce/api/v1/seller/shop/vacation", async ({ request }) => {
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

    const result = updateShopVacationForSeller(auth.user.id, body);
    if (result.error) return mapError(result);

    const message = result.data.is_vacation
      ? "Bat che do nghi shop thanh cong."
      : "Tat che do nghi shop thanh cong.";

    return HttpResponse.json(apiSuccess(200, message, result.data), { status: 200 });
  }),

  http.patch("*/commerce/api/v1/seller/shop", async ({ request }) => {
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

    const result = updateShopProfileForSeller(auth.user.id, body);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Cap nhat thong tin shop thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.post("*/commerce/api/v1/seller/shop", async ({ request }) => {
    await delay(450);
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

    const result = createShopForUser(auth.user.id, body);
    if (result.error) return mapError(result);

    const { data } = result;
    return HttpResponse.json(
      apiSuccess(200, "Tao shop thanh cong.", {
        shop_id: data.shop_id,
        seller_id: data.seller_id,
        shop_name: data.shop_name,
        description: data.description,
        avatar_url: data.avatar_url,
        cover_url: data.cover_url,
        status: data.status,
        is_vacation: data.is_vacation,
        shipping_profile_created: data.shipping_profile_created,
        created_at: data.created_at,
        updated_at: data.updated_at,
      }),
      { status: 200 },
    );
  }),
];
