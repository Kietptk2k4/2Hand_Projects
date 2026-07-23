import { describe, expect, it } from "vitest";
import {
  buildPaymentSupportActiveFilterChips,
  buildPaymentSupportQuickFilter,
  isPaymentSupportQuickPresetActive,
  removePaymentSupportFilterChip,
} from "./paymentSupportFilterHelpers.js";
import { formatReconciliationStatusLabel } from "./orderSupportDisplayUtils.js";

describe("buildPaymentSupportQuickFilter", () => {
  it("returns paid preset", () => {
    expect(buildPaymentSupportQuickFilter("paid")).toMatchObject({
      status: "PAID",
      page: "1",
    });
  });
});

describe("isPaymentSupportQuickPresetActive", () => {
  it("detects paid preset", () => {
    expect(isPaymentSupportQuickPresetActive({ status: "PAID" }, "paid")).toBe(true);
  });
});

describe("buildPaymentSupportActiveFilterChips", () => {
  it("builds reconciliation chip", () => {
    const chips = buildPaymentSupportActiveFilterChips({
      reconciliation_status: "OUTSTANDING",
    });
    expect(chips).toHaveLength(1);
    expect(chips[0].key).toBe("reconciliation_status");
  });
});

describe("removePaymentSupportFilterChip", () => {
  it("removes q filter", () => {
    const next = removePaymentSupportFilterChip({ q: "abc", page: "2" }, "q");
    expect(next.q).toBe("");
    expect(next.page).toBe("1");
  });
});

describe("formatReconciliationStatusLabel", () => {
  it("maps OUTSTANDING", () => {
    expect(formatReconciliationStatusLabel("OUTSTANDING")).toBe("Chưa đối soát");
  });
});
