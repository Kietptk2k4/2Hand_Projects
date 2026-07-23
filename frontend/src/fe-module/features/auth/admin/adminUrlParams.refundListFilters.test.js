import { describe, expect, it } from "vitest";
import {
  buildAdminSearchParams,
  parseOrderSupportRefundListFilters,
} from "./adminUrlParams.js";

describe("parseOrderSupportRefundListFilters", () => {
  it("defaults to REQUESTED when ref_status is absent", () => {
    const params = new URLSearchParams("section=orderSupport&tab=refund-approvals&ref_page=1");
    expect(parseOrderSupportRefundListFilters(params).status).toBe("REQUESTED");
  });

  it("returns empty status when ref_status is ALL", () => {
    const params = new URLSearchParams("ref_status=ALL&ref_page=1");
    expect(parseOrderSupportRefundListFilters(params).status).toBe("");
  });

  it("returns empty status when ref_status is blank", () => {
    const params = new URLSearchParams("ref_status=&ref_page=1");
    expect(parseOrderSupportRefundListFilters(params).status).toBe("");
  });

  it("returns explicit status when ref_status is set", () => {
    const params = new URLSearchParams("ref_status=CONFIRMED");
    expect(parseOrderSupportRefundListFilters(params).status).toBe("CONFIRMED");
  });
});

describe("buildAdminSearchParams refund list filters", () => {
  it("writes ALL sentinel when filtering all statuses", () => {
    const preserve = new URLSearchParams("section=orderSupport&tab=refund-approvals&ref_page=1");
    const next = buildAdminSearchParams({
      section: "orderSupport",
      tab: "refund-approvals",
      refundListFilters: {
        q: "",
        status: "",
        requested_by: "",
        payment_method: "",
        from: "",
        to: "",
        page: "1",
        limit: "20",
      },
      preserve,
    });

    expect(next.get("ref_status")).toBe("ALL");
    expect(parseOrderSupportRefundListFilters(next).status).toBe("");
  });
});
