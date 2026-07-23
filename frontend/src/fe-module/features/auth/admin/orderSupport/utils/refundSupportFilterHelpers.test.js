import { describe, expect, it } from "vitest";
import {
  buildRefundSupportActiveFilterChips,
  buildRefundSupportQuickFilter,
  formatRefundRequestedByLabel,
  formatRefundStatusLabel,
  isRefundSupportQuickPresetActive,
  removeRefundSupportFilterChip,
} from "./refundSupportFilterHelpers.js";

describe("refundSupportFilterHelpers", () => {
  it("builds active filter chips", () => {
    const chips = buildRefundSupportActiveFilterChips({
      q: "abc",
      status: "REQUESTED",
      requested_by: "BUYER",
      payment_method: "VNPAY",
      from: "2026-01-01",
      to: "2026-01-31",
    });
    expect(chips).toHaveLength(6);
    expect(chips[0].key).toBe("q");
  });

  it("removes filter chips", () => {
    const next = removeRefundSupportFilterChip(
      { q: "abc", status: "REQUESTED", page: "2" },
      "status",
    );
    expect(next.status).toBe("");
    expect(next.page).toBe("1");
  });

  it("builds quick filters", () => {
    expect(buildRefundSupportQuickFilter("requested").status).toBe("REQUESTED");
    expect(buildRefundSupportQuickFilter("confirmed").status).toBe("CONFIRMED");
    expect(buildRefundSupportQuickFilter("rejected").status).toBe("REJECTED");
  });

  it("detects active quick presets", () => {
    expect(isRefundSupportQuickPresetActive({ status: "REQUESTED" }, "requested")).toBe(true);
    expect(isRefundSupportQuickPresetActive({ status: "" }, "all")).toBe(true);
  });
});

describe("formatRefundStatusLabel", () => {
  it("maps known statuses", () => {
    expect(formatRefundStatusLabel("REQUESTED")).toBe("Chờ duyệt");
    expect(formatRefundRequestedByLabel("BUYER")).toBe("Người mua");
  });
});
