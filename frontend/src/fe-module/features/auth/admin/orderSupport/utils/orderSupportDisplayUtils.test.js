import { describe, expect, it, vi } from "vitest";
import {
  buildOrderSupportActiveFilterChips,
  buildOrderSupportQuickFilter,
  isOrderSupportQuickPresetActive,
  removeOrderSupportFilterChip,
} from "./orderSupportFilterHelpers.js";
import {
  formatOrderStatusLabel,
  formatPaymentMethodLabel,
  formatPaymentStatusLabel,
  truncateUuid,
} from "./orderSupportDisplayUtils.js";

describe("formatOrderStatusLabel", () => {
  it("maps known order statuses to Vietnamese labels", () => {
    expect(formatOrderStatusLabel("PROCESSING")).toBe("Đang xử lý");
    expect(formatOrderStatusLabel("AWAITING_PAYMENT")).toBe("Chờ thanh toán");
  });

  it("returns raw value for unknown statuses", () => {
    expect(formatOrderStatusLabel("CUSTOM")).toBe("CUSTOM");
  });
});

describe("formatPaymentStatusLabel", () => {
  it("maps known payment statuses", () => {
    expect(formatPaymentStatusLabel("PAID")).toBe("Đã thanh toán");
    expect(formatPaymentStatusLabel("PENDING")).toBe("Chờ thanh toán");
  });
});

describe("formatPaymentMethodLabel", () => {
  it("maps VNPAY", () => {
    expect(formatPaymentMethodLabel("VNPAY")).toBe("VNPay");
  });
});

describe("truncateUuid", () => {
  it("truncates long UUIDs", () => {
    const uuid = "c8433a9a-1111-4111-8111-111111111101";
    expect(truncateUuid(uuid)).toBe("c8433a9a…1101");
  });

  it("returns short values unchanged", () => {
    expect(truncateUuid("short")).toBe("short");
  });
});

describe("buildOrderSupportQuickFilter", () => {
  it("returns processing preset", () => {
    expect(buildOrderSupportQuickFilter("processing")).toMatchObject({
      status: "PROCESSING",
      page: "1",
    });
  });

  it("clears filters for all preset", () => {
    expect(buildOrderSupportQuickFilter("all")).toMatchObject({
      status: "",
      payment_status: "",
      q: "",
    });
  });
});

describe("isOrderSupportQuickPresetActive", () => {
  it("detects active processing preset", () => {
    expect(
      isOrderSupportQuickPresetActive({ status: "PROCESSING" }, "processing"),
    ).toBe(true);
  });

  it("detects all preset when no filters", () => {
    expect(isOrderSupportQuickPresetActive({}, "all")).toBe(true);
  });
});

describe("buildOrderSupportActiveFilterChips", () => {
  it("builds chips for q and payment_status", () => {
    const chips = buildOrderSupportActiveFilterChips({
      q: "abc",
      payment_status: "PAID",
    });
    expect(chips).toHaveLength(2);
    expect(chips[0].key).toBe("q");
    expect(chips[1].key).toBe("payment_status");
  });
});

describe("removeOrderSupportFilterChip", () => {
  it("removes q and resets page", () => {
    const next = removeOrderSupportFilterChip({ q: "x", page: "3" }, "q");
    expect(next.q).toBe("");
    expect(next.page).toBe("1");
  });
});

describe("copyToClipboard", () => {
  it("writes to clipboard when available", async () => {
    const writeText = vi.fn().mockResolvedValue(undefined);
    vi.stubGlobal("navigator", { clipboard: { writeText } });

    const { copyToClipboard } = await import("./orderSupportDisplayUtils.js");
    await expect(copyToClipboard("uuid")).resolves.toBe(true);
    expect(writeText).toHaveBeenCalledWith("uuid");

    vi.unstubAllGlobals();
  });
});
