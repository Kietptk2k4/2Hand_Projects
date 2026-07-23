import { FINANCE_DEFAULT_RANGE_DAYS } from "../constants/financeOverviewConstants.js";

function startOfUtcDay(date) {
  return new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate()));
}

/** Exclusive end = tomorrow UTC midnight (matches commerce default). */
export function buildFinanceRange(days = FINANCE_DEFAULT_RANGE_DAYS, endExclusive = null) {
  const toExclusive = endExclusive ? new Date(endExclusive) : startOfUtcDay(new Date());
  toExclusive.setUTCDate(toExclusive.getUTCDate() + (endExclusive ? 0 : 1));
  const from = new Date(toExclusive);
  from.setUTCDate(from.getUTCDate() - days);
  return {
    from: from.toISOString(),
    to: toExclusive.toISOString(),
    days,
  };
}

export function shiftFinanceRangeBack(fromIso, toIso) {
  const from = new Date(fromIso);
  const to = new Date(toIso);
  const durationMs = to.getTime() - from.getTime();
  if (!Number.isFinite(durationMs) || durationMs <= 0) {
    return buildFinanceRange(FINANCE_DEFAULT_RANGE_DAYS);
  }
  const prevTo = new Date(from.getTime());
  const prevFrom = new Date(from.getTime() - durationMs);
  return {
    from: prevFrom.toISOString(),
    to: prevTo.toISOString(),
  };
}

export function detectFinanceRangePreset(fromIso, toIso) {
  if (!fromIso || !toIso) return "30d";
  const from = new Date(fromIso);
  const to = new Date(toIso);
  const days = Math.round((to.getTime() - from.getTime()) / (24 * 60 * 60 * 1000));
  if (days === 7) return "7d";
  if (days === 90) return "90d";
  if (days === 30) return "30d";
  return "custom";
}

export function percentDelta(current, previous) {
  const curr = Number(current) || 0;
  const prev = Number(previous) || 0;
  if (prev === 0) {
    if (curr === 0) return 0;
    return 100;
  }
  return ((curr - prev) / Math.abs(prev)) * 100;
}

export function formatDeltaPercent(delta) {
  if (!Number.isFinite(delta)) return "—";
  const rounded = Math.round(delta * 10) / 10;
  const sign = rounded > 0 ? "+" : "";
  return `${sign}${rounded}%`;
}

export function feeRatePercent(gmv, fee) {
  const g = Number(gmv) || 0;
  const f = Number(fee) || 0;
  if (g <= 0) return 0;
  return (f / g) * 100;
}

/**
 * Fill missing DAY periods with zeros between from and to (exclusive).
 */
export function zeroFillTrendPoints(points, fromIso, toIso, granularity = "DAY") {
  const existing = new Map(
    (points || []).map((point) => [String(point.periodStart), point]),
  );

  if (granularity !== "DAY" || !fromIso || !toIso) {
    return points || [];
  }

  const start = startOfUtcDay(new Date(fromIso));
  const end = new Date(toIso);
  const filled = [];

  for (let cursor = new Date(start); cursor < end; cursor.setUTCDate(cursor.getUTCDate() + 1)) {
    const key = cursor.toISOString();
    const dayKey = key.slice(0, 10);
    const match =
      existing.get(key) ||
      [...existing.entries()].find(([k]) => k.startsWith(dayKey))?.[1];

    filled.push(
      match || {
        periodStart: key,
        gmvAmount: 0,
        platformFeeAmount: 0,
        itemCount: 0,
      },
    );
  }

  return filled.length ? filled : points || [];
}
