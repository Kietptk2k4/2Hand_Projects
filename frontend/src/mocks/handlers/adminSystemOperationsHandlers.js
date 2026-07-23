import { delay, http, HttpResponse } from "msw";
import { mockUsers } from "../data/authData";
import { apiError, apiSuccess } from "../utils/response";

const MOCK_CONFIGS = [
  {
    config_id: "11111111-1111-1111-1111-111111111101",
    config_key: "commerce.checkout.max_items",
    config_value: "50",
    value_type: "INTEGER",
    description: "So luong toi da trong gio khi checkout",
    is_active: true,
    created_by: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    created_at: "2026-06-01T08:00:00Z",
    updated_by: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    updated_at: "2026-06-05T10:00:00Z",
  },
  {
    config_id: "11111111-1111-1111-1111-111111111102",
    config_key: "social.feed.page_size",
    config_value: "20",
    value_type: "INTEGER",
    description: "Kich thuoc trang feed mac dinh",
    is_active: true,
    created_by: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    created_at: "2026-06-01T08:00:00Z",
    updated_by: null,
    updated_at: null,
  },
];

const MOCK_ANNOUNCEMENTS = [
  {
    announcement_id: "22222222-2222-2222-2222-222222222201",
    title: "Bao tri he thong",
    content: "He thong se bao tri luc 02:00.",
    severity: "WARNING",
    status: "DRAFT",
    is_pinned: false,
    dismissible: true,
    created_by: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    created_at: "2026-06-06T09:00:00Z",
    sent_at: null,
  },
  {
    announcement_id: "22222222-2222-2222-2222-222222222202",
    title: "Phien ban moi",
    content: "Da cap nhat tinh nang moi.",
    severity: "INFO",
    status: "SENT",
    is_pinned: true,
    dismissible: true,
    created_by: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
    created_at: "2026-06-04T09:00:00Z",
    sent_at: "2026-06-04T10:00:00Z",
  },
];

function getActor(request) {
  const authHeader = request.headers.get("Authorization");
  if (!authHeader?.startsWith("Bearer ")) return null;
  const token = authHeader.replace("Bearer ", "");
  return mockUsers.find((item) => token.includes(item.id)) || null;
}

function paginate(items, page = 1, size = 20) {
  const p = Math.max(1, Number(page) || 1);
  const s = Math.max(1, Number(size) || 20);
  const start = (p - 1) * s;
  const slice = items.slice(start, start + s);
  return {
    page: p,
    size: s,
    total_elements: items.length,
    total_pages: Math.max(1, Math.ceil(items.length / s)),
    items: slice,
  };
}

function maskConfigValue(item) {
  const key = String(item.config_key || "").toUpperCase();
  const secret = ["PASSWORD", "SECRET", "TOKEN", "API_KEY"].some((fragment) => key.includes(fragment));
  if (!secret) {
    return { ...item, value_masked: false };
  }
  return { ...item, config_value: "********", value_masked: true };
}

export const adminSystemOperationsHandlers = [
  http.get("*/admin/api/v1/system-configs", async ({ request }) => {
    await delay(250);
    const actor = getActor(request);
    if (!actor?.is_admin) {
      return HttpResponse.json(apiError("ADMIN-403", "Missing permission: SYSTEM_CONFIG_VIEW"), { status: 403 });
    }
    const url = new URL(request.url);
    let items = [...MOCK_CONFIGS];
    const q = (url.searchParams.get("q") || "").toLowerCase();
    if (q) {
      items = items.filter(
        (item) => item.config_key.toLowerCase().includes(q) || (item.description || "").toLowerCase().includes(q),
      );
    }
    const valueType = url.searchParams.get("value_type");
    if (valueType) items = items.filter((item) => item.value_type === valueType);
    const active = url.searchParams.get("is_active");
    if (active === "true" || active === "false") {
      const flag = active === "true";
      items = items.filter((item) => item.is_active === flag);
    }
    const data = paginate(items.map(maskConfigValue), url.searchParams.get("page"), url.searchParams.get("size"));
    return HttpResponse.json(apiSuccess(200, "OK", data));
  }),

  http.get("*/admin/api/v1/system-configs/:configId", async ({ request, params }) => {
    await delay(200);
    const actor = getActor(request);
    if (!actor?.is_admin) {
      return HttpResponse.json(apiError("ADMIN-403", "Missing permission: SYSTEM_CONFIG_VIEW"), { status: 403 });
    }
    const item = MOCK_CONFIGS.find((c) => c.config_id === params.configId);
    if (!item) return HttpResponse.json(apiError("ADMIN-404", "Not found"), { status: 404 });
    return HttpResponse.json(apiSuccess(200, "OK", maskConfigValue(item)));
  }),

  http.post("*/admin/api/v1/system-configs", async ({ request }) => {
    await delay(300);
    const actor = getActor(request);
    if (!actor?.is_admin) {
      return HttpResponse.json(apiError("ADMIN-403", "Forbidden"), { status: 403 });
    }
    const body = await request.json();
    const created = {
      config_id: crypto.randomUUID(),
      config_key: body.config_key,
      config_value: body.config_value,
      value_type: body.value_type,
      description: body.description,
      is_active: body.is_active ?? true,
      created_by: actor.id,
      created_at: new Date().toISOString(),
      history_id: crypto.randomUUID(),
      outbox_event_id: crypto.randomUUID(),
    };
    MOCK_CONFIGS.unshift({
      ...created,
      updated_by: null,
      updated_at: null,
    });
    return HttpResponse.json(apiSuccess(201, "Created", created), { status: 201 });
  }),

  http.patch("*/admin/api/v1/system-configs/:configId", async ({ request, params }) => {
    await delay(250);
    const item = MOCK_CONFIGS.find((c) => c.config_id === params.configId);
    if (!item) return HttpResponse.json(apiError("ADMIN-404", "Not found"), { status: 404 });
    const body = await request.json();
    item.config_value = body.config_value;
    item.description = body.description;
    item.updated_at = new Date().toISOString();
    return HttpResponse.json(apiSuccess(200, "Updated", { ...item, history_id: crypto.randomUUID() }));
  }),

  http.patch("*/admin/api/v1/system-configs/:configId/toggle", async ({ request, params }) => {
    await delay(250);
    const item = MOCK_CONFIGS.find((c) => c.config_id === params.configId);
    if (!item) return HttpResponse.json(apiError("ADMIN-404", "Not found"), { status: 404 });
    const body = await request.json();
    item.is_active = Boolean(body.is_active);
    item.updated_at = new Date().toISOString();
    return HttpResponse.json(
      apiSuccess(200, "Toggled", { ...item, state_changed: true, history_id: crypto.randomUUID() }),
    );
  }),

  http.get("*/admin/api/v1/system-configs/:configId/history", async ({ params }) => {
    await delay(200);
    const item = MOCK_CONFIGS.find((c) => c.config_id === params.configId);
    if (!item) return HttpResponse.json(apiError("ADMIN-404", "Not found"), { status: 404 });
    const history = [
      {
        history_id: crypto.randomUUID(),
        config_key: item.config_key,
        old_value: "-",
        new_value: item.config_value,
        changed_by: item.updated_by || item.created_by,
        reason: "Khoi tao",
        created_at: item.updated_at || item.created_at,
        values_masked: false,
      },
    ];
    return HttpResponse.json(
      apiSuccess(200, "OK", {
        config_id: item.config_id,
        config_key: item.config_key,
        page: 1,
        size: 20,
        total_elements: history.length,
        total_pages: 1,
        values_masked: false,
        history,
      }),
    );
  }),

  http.get("*/admin/api/v1/system-announcements", async ({ request }) => {
    await delay(250);
    const actor = getActor(request);
    if (!actor?.is_admin) {
      return HttpResponse.json(apiError("ADMIN-403", "Forbidden"), { status: 403 });
    }
    const url = new URL(request.url);
    let items = [...MOCK_ANNOUNCEMENTS];
    const q = (url.searchParams.get("q") || "").toLowerCase();
    if (q) {
      items = items.filter(
        (item) => item.title.toLowerCase().includes(q) || item.content.toLowerCase().includes(q),
      );
    }
    const status = url.searchParams.get("status");
    if (status) items = items.filter((item) => item.status === status);
    const severity = url.searchParams.get("severity");
    if (severity) items = items.filter((item) => item.severity === severity);
    const data = paginate(items, url.searchParams.get("page"), url.searchParams.get("size"));
    data.items = data.items;
    return HttpResponse.json(apiSuccess(200, "OK", data));
  }),

  http.get("*/admin/api/v1/system-announcements/:announcementId", async ({ request, params }) => {
    await delay(200);
    const actor = getActor(request);
    if (!actor?.is_admin) {
      return HttpResponse.json(apiError("ADMIN-403", "Forbidden"), { status: 403 });
    }
    const item = MOCK_ANNOUNCEMENTS.find((a) => a.announcement_id === params.announcementId);
    if (!item) return HttpResponse.json(apiError("ADMIN-404", "Not found"), { status: 404 });
    return HttpResponse.json(apiSuccess(200, "OK", item));
  }),

  http.post("*/admin/api/v1/system-announcements", async ({ request }) => {
    await delay(300);
    const body = await request.json();
    const created = {
      announcement_id: crypto.randomUUID(),
      title: body.title,
      content: body.content,
      severity: body.severity,
      status: "DRAFT",
      is_pinned: Boolean(body.is_pinned),
      dismissible: body.dismissible !== false,
      created_by: "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
      created_at: new Date().toISOString(),
      sent_at: null,
    };
    MOCK_ANNOUNCEMENTS.unshift(created);
    return HttpResponse.json(apiSuccess(201, "Created", created), { status: 201 });
  }),

  http.patch("*/admin/api/v1/system-announcements/:announcementId", async ({ request, params }) => {
    await delay(250);
    const item = MOCK_ANNOUNCEMENTS.find((a) => a.announcement_id === params.announcementId);
    if (!item) return HttpResponse.json(apiError("ADMIN-404", "Not found"), { status: 404 });
    if (item.status !== "DRAFT") {
      return HttpResponse.json(apiError("ADMIN-409", "Only draft can be updated"), { status: 409 });
    }
    const body = await request.json();
    item.title = body.title ?? item.title;
    item.content = body.content ?? item.content;
    item.severity = body.severity ?? item.severity;
    item.is_pinned = Boolean(body.is_pinned ?? item.is_pinned);
    item.dismissible = body.dismissible !== false;
    return HttpResponse.json(apiSuccess(200, "Updated", item));
  }),

  http.post("*/admin/api/v1/system-announcements/:announcementId/publish", async ({ params }) => {
    await delay(250);
    const item = MOCK_ANNOUNCEMENTS.find((a) => a.announcement_id === params.announcementId);
    if (!item) return HttpResponse.json(apiError("ADMIN-404", "Not found"), { status: 404 });
    item.status = "SENT";
    item.sent_at = new Date().toISOString();
    return HttpResponse.json(apiSuccess(200, "Published", { ...item, outbox_event_id: crypto.randomUUID() }));
  }),

  http.patch("*/admin/api/v1/system-announcements/:announcementId/pin", async ({ request, params }) => {
    await delay(200);
    const item = MOCK_ANNOUNCEMENTS.find((a) => a.announcement_id === params.announcementId);
    if (!item) return HttpResponse.json(apiError("ADMIN-404", "Not found"), { status: 404 });
    const body = await request.json();
    item.is_pinned = Boolean(body.is_pinned);
    return HttpResponse.json(apiSuccess(200, "Pinned", { ...item, state_changed: true }));
  }),

  http.post("*/admin/api/v1/system-announcements/:announcementId/cancel", async ({ params }) => {
    await delay(200);
    const item = MOCK_ANNOUNCEMENTS.find((a) => a.announcement_id === params.announcementId);
    if (!item) return HttpResponse.json(apiError("ADMIN-404", "Not found"), { status: 404 });
    item.status = "CANCELLED";
    return HttpResponse.json(apiSuccess(200, "Cancelled", { ...item, state_changed: true }));
  }),
];