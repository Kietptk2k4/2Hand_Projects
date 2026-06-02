import { delay, http, HttpResponse } from "msw";
import {
  addAddressForUser,
  deleteAddressForUser,
  getAddressesForUser,
  setDefaultAddressForUser,
  toAddressMutationResponse,
  updateAddressForUser,
} from "../data/commerceAddressData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

const ADDRESS_ID_REGEX =
  /^a[0-9a-f]{7}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

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

function mapMutationError(result) {
  const messages = {
    "COMMERCE-400-VALIDATION": "Du lieu khong hop le.",
    "COMMERCE-400-PHONE": "So dien thoai khong hop le.",
    "COMMERCE-404-ADDRESS": "Khong tim thay dia chi.",
    "COMMERCE-409-ADDRESS-DEFAULT": "Khong the cap nhat dia chi mac dinh.",
  };
  return HttpResponse.json(
    apiError(result.error, messages[result.error] || "Co loi xay ra."),
    { status: result.status },
  );
}

function isValidAddressId(addressId) {
  return typeof addressId === "string" && ADDRESS_ID_REGEX.test(addressId);
}

export const commerceAddressHandlers = [
  http.get("*/commerce/api/v1/addresses", async ({ request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const addresses = getAddressesForUser(auth.user.id);
    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach dia chi thanh cong.", { addresses }),
      { status: 200 },
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
    if (result.error) return mapMutationError(result);

    return HttpResponse.json(
      apiSuccess(
        200,
        "Them dia chi giao hang thanh cong.",
        toAddressMutationResponse(result.address, auth.user.id),
      ),
      { status: 200 },
    );
  }),

  http.patch("*/commerce/api/v1/addresses/:addressId/default", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const addressId = params.addressId;
    if (!isValidAddressId(addressId)) {
      return HttpResponse.json(apiError("COMMERCE-404-ADDRESS", "Khong tim thay dia chi."), {
        status: 404,
      });
    }

    const result = setDefaultAddressForUser(auth.user.id, addressId);
    if (result.error) return mapMutationError(result);

    return HttpResponse.json(
      apiSuccess(
        200,
        "Dat dia chi mac dinh thanh cong.",
        toAddressMutationResponse(result.address, auth.user.id),
      ),
      { status: 200 },
    );
  }),

  http.patch("*/commerce/api/v1/addresses/:addressId", async ({ params, request }) => {
    await delay(400);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const addressId = params.addressId;
    if (!isValidAddressId(addressId)) {
      return HttpResponse.json(apiError("COMMERCE-404-ADDRESS", "Khong tim thay dia chi."), {
        status: 404,
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

    const result = updateAddressForUser(auth.user.id, addressId, body);
    if (result.error) return mapMutationError(result);

    return HttpResponse.json(
      apiSuccess(
        200,
        "Cap nhat dia chi giao hang thanh cong.",
        toAddressMutationResponse(result.address, auth.user.id),
      ),
      { status: 200 },
    );
  }),

  http.delete("*/commerce/api/v1/addresses/:addressId", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const addressId = params.addressId;
    if (!isValidAddressId(addressId)) {
      return HttpResponse.json(apiError("COMMERCE-404-ADDRESS", "Khong tim thay dia chi."), {
        status: 404,
      });
    }

    const result = deleteAddressForUser(auth.user.id, addressId);
    if (result.error) return mapMutationError(result);

    return HttpResponse.json(
      apiSuccess(200, "Xoa dia chi giao hang thanh cong.", result.data),
      { status: 200 },
    );
  }),
];
