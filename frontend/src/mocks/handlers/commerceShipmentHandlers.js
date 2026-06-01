import { delay, http, HttpResponse } from "msw";
import {
  getShipmentDetailForUser,
  getShipmentTrackingForUser,
} from "../data/commerceShipmentData";
import { getUserByToken } from "../utils/socialMockAuth";
import { apiError, apiSuccess } from "../utils/response";

const SHIPMENT_ID_REGEX = /^sh-\d{6}$/;

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

function notFoundShipment() {
  return HttpResponse.json(
    apiError("COMMERCE-404-SHIPMENT", "Khong tim thay thong tin van chuyen."),
    { status: 404 },
  );
}

export const commerceShipmentHandlers = [
  http.get("*/commerce/api/v1/shipments/:shipmentId", async ({ params, request }) => {
    await delay(350);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const shipmentId = params.shipmentId;
    if (!SHIPMENT_ID_REGEX.test(shipmentId)) {
      return notFoundShipment();
    }

    const result = getShipmentDetailForUser(auth.user.id, shipmentId);
    if (result.error) return notFoundShipment();

    return HttpResponse.json(
      apiSuccess(200, "Lay chi tiet shipment thanh cong.", result.data),
      { status: 200 },
    );
  }),

  http.get("*/commerce/api/v1/shipments/:shipmentId/tracking", async ({ params, request }) => {
    await delay(300);
    const auth = requireAuth(request);
    if (auth.error) return auth.error;

    const shipmentId = params.shipmentId;
    if (!SHIPMENT_ID_REGEX.test(shipmentId)) {
      return notFoundShipment();
    }

    const result = getShipmentTrackingForUser(auth.user.id, shipmentId);
    if (result.error) return notFoundShipment();

    return HttpResponse.json(
      apiSuccess(200, "Lay thong tin tracking shipment thanh cong.", result.data),
      { status: 200 },
    );
  }),
];
