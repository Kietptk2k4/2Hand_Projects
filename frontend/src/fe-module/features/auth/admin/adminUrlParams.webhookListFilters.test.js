import { describe, expect, it } from "vitest";
import {
  buildAdminSearchParams,
  parseOrderSupportWebhookFilters,
  parseOrderSupportWebhookLogId,
} from "../adminUrlParams.js";

describe("adminUrlParams webhook list filters", () => {
  it("parses wh_* params with legacy fallback", () => {
    const params = new URLSearchParams(
      "wh_provider=PAYOS&wh_reference_id=ABC&wh_q=12&wh_status=PENDING&wh_page=2&wh_size=50",
    );
    expect(parseOrderSupportWebhookFilters(params)).toEqual({
      provider: "PAYOS",
      reference_id: "ABC",
      q: "12",
      event_type: "",
      status: "PENDING",
      from: "",
      to: "",
      page: "2",
      size: "50",
    });
  });

  it("builds wh_* params from webhook filters", () => {
    const next = buildAdminSearchParams({
      section: "orderSupport",
      tab: "webhook-logs",
      webhookFilters: {
        provider: "GHN",
        reference_id: "GHN-1",
        q: "GHN",
        event_type: "delivered",
        status: "PROCESSED",
        from: "2026-05-20T00:00:00.000Z",
        to: "2026-05-21T00:00:00.000Z",
        page: 1,
        size: 20,
      },
      webhookLogId: "f7777777-7777-4777-8777-777777777707",
      webhookLogProvider: "PAYOS",
    });

    expect(next.get("wh_provider")).toBe("GHN");
    expect(next.get("wh_reference_id")).toBe("GHN-1");
    expect(next.get("wh_q")).toBe("GHN");
    expect(next.get("wh_event_type")).toBe("delivered");
    expect(next.get("wh_log_id")).toBe("f7777777-7777-4777-8777-777777777707");
    expect(next.get("wh_log_provider")).toBe("PAYOS");
  });

  it("parses webhook log selection params", () => {
    const params = new URLSearchParams("wh_log_id=abc&wh_log_provider=GHN");
    expect(parseOrderSupportWebhookLogId(params)).toBe("abc");
  });
});
