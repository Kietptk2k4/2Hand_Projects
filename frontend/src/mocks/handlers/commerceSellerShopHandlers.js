import { delay, http, HttpResponse } from "msw";
import { createShopForUser } from "../data/commerceSellerShopData";
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

export const commerceSellerShopHandlers = [
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
    if (result.error) {
      const payload = apiError(result.error, result.message || "Co loi xay ra.");
      if (result.data) {
        payload.data = result.data;
      }
      return HttpResponse.json(payload, { status: result.status });
    }

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
