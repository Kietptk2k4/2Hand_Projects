import { delay, http, HttpResponse } from "msw";
import { addAddressForUser, getAddressesForUser } from "../data/commerceAddressData";
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

export const commerceAddressHandlers = [
  http.get("*/commerce/api/v1/addresses", async ({ request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const addresses = getAddressesForUser(auth.user.id);
    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach dia chi thanh cong.", { addresses }),
      { status: 200 }
    );
  }),

  http.post("*/commerce/api/v1/addresses", async ({ request }) => {
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

    const result = addAddressForUser(auth.user.id, body);
    if (result.error) {
      const messages = {
        "COMMERCE-400-VALIDATION": "Du lieu khong hop le.",
        "COMMERCE-400-PHONE": "So dien thoai khong hop le.",
      };
      return HttpResponse.json(
        apiError(result.error, messages[result.error] || "Loi them dia chi."),
        { status: result.status }
      );
    }

    const { address } = result;
    return HttpResponse.json(
      apiSuccess(200, "Them dia chi giao hang thanh cong.", {
        address_id: address.id,
        user_id: auth.user.id,
        receiver_name: address.receiver_name,
        phone: address.phone,
        province_code: address.province_code,
        district_code: address.district_code,
        ward_code: address.ward_code,
        address_detail: address.address_detail,
        is_default: address.is_default,
        created_at: address.created_at,
        updated_at: address.updated_at,
      }),
      { status: 200 }
    );
  }),
];
