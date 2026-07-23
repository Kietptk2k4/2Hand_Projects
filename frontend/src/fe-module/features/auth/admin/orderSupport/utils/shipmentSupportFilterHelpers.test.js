import { describe, expect, it } from "vitest";
import {
  buildShipmentSupportActiveFilterChips,
  buildShipmentSupportQuickFilter,
  isShipmentSupportQuickPresetActive,
  removeShipmentSupportFilterChip,
} from "./shipmentSupportFilterHelpers.js";
import { formatShipmentStatusLabel } from "./orderSupportDisplayUtils.js";

describe("buildShipmentSupportQuickFilter", () => {
  it("returns shipped preset", () => {
    expect(buildShipmentSupportQuickFilter("shipped")).toMatchObject({
      status: "SHIPPED",
      page: "1",
    });
  });
});

describe("isShipmentSupportQuickPresetActive", () => {
  it("detects shipped preset", () => {
    expect(isShipmentSupportQuickPresetActive({ status: "SHIPPED" }, "shipped")).toBe(true);
  });
});

describe("buildShipmentSupportActiveFilterChips", () => {
  it("builds order_id chip", () => {
    const chips = buildShipmentSupportActiveFilterChips({
      order_id: "a1111111-1111-4111-8111-111111111101",
    });
    expect(chips).toHaveLength(1);
    expect(chips[0].key).toBe("order_id");
  });
});

describe("removeShipmentSupportFilterChip", () => {
  it("removes q filter", () => {
    const next = removeShipmentSupportFilterChip({ q: "abc", page: "2" }, "q");
    expect(next.q).toBe("");
    expect(next.page).toBe("1");
  });
});

describe("formatShipmentStatusLabel", () => {
  it("maps DELIVERED", () => {
    expect(formatShipmentStatusLabel("DELIVERED")).toBe("Đã giao");
  });
});
