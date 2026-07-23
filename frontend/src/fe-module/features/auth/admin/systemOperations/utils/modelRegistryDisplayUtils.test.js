import { describe, expect, it } from "vitest";
import {
  ARTIFACT_STATUS,
  deriveArtifactStatus,
  filterModelRegistryItems,
  computeModelRegistryStats,
} from "./modelRegistryDisplayUtils.js";

describe("deriveArtifactStatus", () => {
  it("returns active when isActive is true", () => {
    expect(deriveArtifactStatus({ isActive: true, metrics: {} })).toBe(ARTIFACT_STATUS.ACTIVE);
  });

  it("returns rejected when gate.status is rejected_by_metrics", () => {
    expect(
      deriveArtifactStatus({
        isActive: false,
        metrics: { gate: { status: "rejected_by_metrics" } },
      }),
    ).toBe(ARTIFACT_STATUS.REJECTED);
  });

  it("returns rejected when gate.reason is rejected_by_metrics", () => {
    expect(
      deriveArtifactStatus({
        isActive: false,
        metrics: { gate: { passed: false, reason: "rejected_by_metrics" } },
      }),
    ).toBe(ARTIFACT_STATUS.REJECTED);
  });

  it("returns inactive for other inactive artifacts", () => {
    expect(
      deriveArtifactStatus({
        isActive: false,
        metrics: { gate: { passed: true, status: "passed" } },
      }),
    ).toBe(ARTIFACT_STATUS.INACTIVE);
  });
});

describe("filterModelRegistryItems", () => {
  const items = [
    { version: 3, isActive: true, metrics: {} },
    { version: 2, isActive: false, metrics: { gate: { status: "rejected_by_metrics" } } },
    { version: 1, isActive: false, metrics: {} },
  ];

  it("filters by status preset", () => {
    expect(filterModelRegistryItems(items, ARTIFACT_STATUS.REJECTED)).toHaveLength(1);
    expect(filterModelRegistryItems(items, ARTIFACT_STATUS.ACTIVE)).toHaveLength(1);
  });
});

describe("computeModelRegistryStats", () => {
  it("counts artifact statuses", () => {
    const stats = computeModelRegistryStats([
      { isActive: true, metrics: {} },
      { isActive: false, metrics: { gate: { status: "rejected_by_metrics" } } },
      { isActive: false, metrics: {} },
    ]);

    expect(stats.total).toBe(3);
    expect(stats.active).toBe(1);
    expect(stats.rejected).toBe(1);
    expect(stats.inactive).toBe(1);
  });
});
