import { delay, http, HttpResponse } from "msw";
import {
  createShipmentForSeller,
  getSellerShipmentForUser,
  listSellerShipmentsForUser,
  updateSellerShipmentForSeller,
  validateSellerShipmentListQuery,
} from "../data/commerceSellerShipmentData";
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
  return HttpResponse.json(apiError(result.error, result.message || "Co loi xay ra."), {
    status: result.status,
  });
}

export const commerceSellerShipmentHandlers = [
  /** GET list seller shipments — mirrors backend GET /commerce/api/v1/seller/shipments */
  http.get("*/commerce/api/v1/seller/shipments", async ({ request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const url = new URL(request.url);
    const validated = validateSellerShipmentListQuery({
      page: url.searchParams.get("page") || "1",
      limit: url.searchParams.get("limit") || "20",
      status: url.searchParams.get("status") || undefined,
      q: url.searchParams.get("q") || undefined,
    });

    if (validated.error) return mapError(validated);

    const result = listSellerShipmentsForUser(auth.user.id, validated);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay danh sach van don seller thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.get("*/commerce/api/v1/seller/shipments/:shipmentId", async ({ params, request }) => {
    await delay(250);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const result = getSellerShipmentForUser(auth.user.id, params.shipmentId);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Lay chi tiet van don thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.post("*/commerce/api/v1/seller/shipments", async ({ request }) => {
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

    const result = createShipmentForSeller(auth.user.id, body);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Tao shipment thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.patch("*/commerce/api/v1/seller/shipments/:shipmentId", async ({ params, request }) => {
    await delay(400);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    let body;
    try {
      body = await request.json();
    } catch {
      return HttpResponse.json(apiError("COMMERCE-400", "Du lieu khong hop le."), {
        status: 400,
      });
    }

    const result = updateSellerShipmentForSeller(auth.user.id, params.shipmentId, body);
    if (result.error) return mapError(result);

    return HttpResponse.json(
      apiSuccess(200, "Cap nhat shipment thanh cong.", result.data),
      { status: 200 },
    );
  }),
];
