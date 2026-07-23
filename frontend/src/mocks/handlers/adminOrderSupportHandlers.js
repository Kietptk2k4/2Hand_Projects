import { delay, http, HttpResponse } from "msw";
import { mockUsers } from "../data/authData";
import {
  getMockOrderSupportDetail,
  getMockOrdersForSupport,
  getMockPaymentsForSupport,
  getMockPaymentSupportDetail,
  getMockShipmentSupportDetail,
  getMockShipmentSupportList,
  getMockWebhookLogs,
  getMockWebhookLogStats,
} from "../data/adminOrderSupportData";
import { apiError, apiSuccess } from "../utils/response";

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function getActorFromRequest(request) {
  const authHeader = request.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) return null;
  const token = authHeader.replace("Bearer ", "");
  if (token.includes("expired-access")) return null;
  return mockUsers.find((item) => token.includes(item.id)) || null;
}

function isAdminActor(actor) {
  return Boolean(actor?.is_admin);
}

function isValidUuid(value) {
  return UUID_RE.test(value || "");
}

export const adminOrderSupportHandlers = [
  http.get("*/admin/api/v1/support/orders", async ({ request }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const url = new URL(request.url);
    const q = url.searchParams.get("q") || "";
    const status = url.searchParams.get("status") || "";
    const paymentStatus = url.searchParams.get("payment_status") || "";
    const paymentMethod = url.searchParams.get("payment_method") || "";
    const from = url.searchParams.get("from") || "";
    const to = url.searchParams.get("to") || "";
    const sort = url.searchParams.get("sort") || "created_at";
    const page = Number(url.searchParams.get("page") || 1);
    const size = Number(url.searchParams.get("size") || 20);

    const data = getMockOrdersForSupport({
      q,
      status,
      payment_status: paymentStatus,
      payment_method: paymentMethod,
      from,
      to,
      sort,
      page,
      size,
    });

    return HttpResponse.json(apiSuccess(200, "Orders retrieved successfully", data), { status: 200 });
  }),

  http.get("*/admin/api/v1/support/orders/:orderId", async ({ request, params }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const orderId = params.orderId;
    if (!isValidUuid(orderId)) {
      return HttpResponse.json(apiError(400, "Du lieu khong hop le."), { status: 400 });
    }

    const detail = getMockOrderSupportDetail(orderId);
    if (!detail) {
      return HttpResponse.json(apiError(404, "Khong tim thay don hang."), { status: 404 });
    }

    return HttpResponse.json(
      apiSuccess(200, "Order support detail retrieved successfully", detail),
      { status: 200 },
    );
  }),

  http.get("*/admin/api/v1/support/payments", async ({ request }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const url = new URL(request.url);
    const q = url.searchParams.get("q") || "";
    const status = url.searchParams.get("status") || "";
    const reconciliationStatus = url.searchParams.get("reconciliation_status") || "";
    const paymentMethod = url.searchParams.get("payment_method") || "";
    const orderId = url.searchParams.get("order_id") || "";
    const from = url.searchParams.get("from") || "";
    const to = url.searchParams.get("to") || "";
    const page = Number(url.searchParams.get("page") || 1);
    const size = Number(url.searchParams.get("size") || 20);

    const data = getMockPaymentsForSupport({
      q,
      status,
      reconciliation_status: reconciliationStatus,
      payment_method: paymentMethod,
      order_id: orderId,
      from,
      to,
      page,
      size,
    });

    return HttpResponse.json(
      apiSuccess(200, "Payments retrieved successfully", data),
      { status: 200 },
    );
  }),

  http.get("*/admin/api/v1/support/payments/:paymentId", async ({ request, params }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const paymentId = params.paymentId;
    if (!isValidUuid(paymentId)) {
      return HttpResponse.json(apiError(400, "Du lieu khong hop le."), { status: 400 });
    }

    const detail = getMockPaymentSupportDetail(paymentId);
    if (!detail) {
      return HttpResponse.json(apiError(404, "Khong tim thay thanh toan."), { status: 404 });
    }

    return HttpResponse.json(
      apiSuccess(200, "Payment support detail retrieved successfully", detail),
      { status: 200 },
    );
  }),

  http.get("*/admin/api/v1/support/shipments", async ({ request }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const url = new URL(request.url);
    const q = url.searchParams.get("q") || "";
    const status = url.searchParams.get("status") || "";
    const carrier = url.searchParams.get("carrier") || "";
    const order_id = url.searchParams.get("order_id") || "";
    const from = url.searchParams.get("from") || "";
    const to = url.searchParams.get("to") || "";
    const sort = url.searchParams.get("sort") || "updated_at";
    const page = Number(url.searchParams.get("page") || 1);
    const size = Number(url.searchParams.get("size") || 20);

    const data = getMockShipmentSupportList({
      q,
      status,
      carrier,
      order_id,
      from,
      to,
      sort,
      page,
      size,
    });

    return HttpResponse.json(
      apiSuccess(200, "Shipment support list retrieved successfully", data),
      { status: 200 },
    );
  }),

  http.get("*/admin/api/v1/support/shipments/:shipmentId", async ({ request, params }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const shipmentId = params.shipmentId;
    if (!isValidUuid(shipmentId)) {
      return HttpResponse.json(apiError(400, "Du lieu khong hop le."), { status: 400 });
    }

    const detail = getMockShipmentSupportDetail(shipmentId);
    if (!detail) {
      return HttpResponse.json(apiError(404, "Khong tim thay van don."), { status: 404 });
    }

    return HttpResponse.json(
      apiSuccess(200, "Shipment support detail retrieved successfully", detail),
      { status: 200 },
    );
  }),

  http.get("*/admin/api/v1/support/webhook-logs/stats", async ({ request }) => {
    await delay(200);
    const actor = getActorFromRequest(request);
    if (!actor || !isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Forbidden"), { status: 403 });
    }
    const url = new URL(request.url);
    const data = getMockWebhookLogStats(
      getMockWebhookLogs({
        provider: url.searchParams.get("provider") || "",
        reference_id: url.searchParams.get("reference_id") || "",
        q: url.searchParams.get("q") || "",
        status: url.searchParams.get("status") || "",
        page: 1,
        size: 100,
      }).logs,
    );
    return HttpResponse.json(apiSuccess(200, "Webhook log stats retrieved successfully", data));
  }),

  http.get("*/admin/api/v1/support/webhook-logs/:logId", async ({ request, params }) => {
    await delay(200);
    const actor = getActorFromRequest(request);
    if (!actor || !isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Forbidden"), { status: 403 });
    }
    const log = getMockWebhookLogs({ page: 1, size: 100 }).logs.find(
      (item) => item.log_id === params.logId,
    );
    if (!log) {
      return HttpResponse.json(apiError(404, "Not found"), { status: 404 });
    }
    return HttpResponse.json(apiSuccess(200, "Webhook log retrieved successfully", log));
  }),

  http.get("*/admin/api/v1/support/webhook-logs", async ({ request }) => {
    await delay(300);
    const actor = getActorFromRequest(request);
    if (!actor) {
      return HttpResponse.json(apiError(401, "Authentication required"), { status: 401 });
    }
    if (!isAdminActor(actor)) {
      return HttpResponse.json(apiError(403, "Ban khong co quyen truy cap."), { status: 403 });
    }

    const url = new URL(request.url);
    const provider = url.searchParams.get("provider") || "";
    const referenceId = url.searchParams.get("reference_id") || "";
    const status = url.searchParams.get("status") || "";
    const page = Number(url.searchParams.get("page") || 1);
    const size = Number(url.searchParams.get("size") || 20);

    const data = getMockWebhookLogs({
      provider,
      reference_id: referenceId,
      status,
      page,
      size,
    });

    return HttpResponse.json(
      apiSuccess(200, "Webhook logs retrieved successfully", data),
      { status: 200 },
    );
  }),
];
