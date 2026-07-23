import {
  Area,
  CartesianGrid,
  ComposedChart,
  Legend,
  Line,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { FinanceChartPanel } from "./FinanceChartPanel.jsx";

function formatPeriodLabel(periodStart, granularity) {
  if (!periodStart) return "";
  const date = new Date(periodStart);
  if (Number.isNaN(date.getTime())) return "";
  if (granularity === "MONTH") {
    return date.toLocaleDateString("vi-VN", { month: "short", year: "numeric" });
  }
  if (granularity === "WEEK") {
    return `T${date.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit" })}`;
  }
  return date.toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit" });
}

function ChartTooltip({ active, payload, label, granularity }) {
  if (!active || !payload?.length) return null;
  const point = payload[0]?.payload;
  return (
    <div className="rounded-lg border border-admin-border bg-admin-surface px-3 py-2 shadow-[var(--shadow-admin-surface)]">
      <p className="text-xs font-medium text-admin-text-muted">
        {formatPeriodLabel(label || point?.periodStart, granularity)}
      </p>
      <p className="mt-1 text-sm tabular-nums text-admin-text">
        GMV: <strong>{formatVndPrice(point?.gmvAmount)}</strong>
      </p>
      <p className="text-sm tabular-nums text-admin-text">
        Phí: <strong>{formatVndPrice(point?.platformFeeAmount)}</strong>
      </p>
      <p className="text-sm tabular-nums text-admin-text-secondary">
        {point?.itemCount ?? 0} mặt hàng
      </p>
    </div>
  );
}

function compactVnd(value) {
  const n = Number(value) || 0;
  if (n >= 1_000_000) return `${Math.round(n / 100_000) / 10}Tr`;
  if (n >= 1_000) return `${Math.round(n / 100) / 10}N`;
  return String(n);
}

export function FinanceGmvFeeChart({ trend, status, errorMessage, onRetry, granularity }) {
  const points = trend?.points ?? [];
  const chartData = points.map((point) => ({
    ...point,
    label: formatPeriodLabel(point.periodStart, granularity),
  }));

  return (
    <FinanceChartPanel
      title="GMV & phí sàn"
      subtitle="Area GMV + line phí sàn (trục kép)"
      status={status}
      errorMessage={errorMessage}
      onRetry={onRetry}
      empty={!chartData.length}
    >
      <div className="h-72 w-full sm:h-80">
        <ResponsiveContainer width="100%" height="100%">
          <ComposedChart data={chartData} margin={{ top: 8, right: 12, left: 0, bottom: 0 }}>
            <defs>
              <linearGradient id="gmvAreaFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="#8b7355" stopOpacity={0.35} />
                <stop offset="100%" stopColor="#8b7355" stopOpacity={0.02} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" vertical={false} />
            <XAxis
              dataKey="label"
              tick={{ fontSize: 11, fill: "#6b7280" }}
              axisLine={false}
              tickLine={false}
              minTickGap={24}
            />
            <YAxis
              yAxisId="gmv"
              tickFormatter={compactVnd}
              tick={{ fontSize: 11, fill: "#6b7280" }}
              axisLine={false}
              tickLine={false}
              width={48}
            />
            <YAxis
              yAxisId="fee"
              orientation="right"
              tickFormatter={compactVnd}
              tick={{ fontSize: 11, fill: "#6b7280" }}
              axisLine={false}
              tickLine={false}
              width={48}
            />
            <Tooltip content={<ChartTooltip granularity={granularity} />} />
            <Legend wrapperStyle={{ fontSize: 12 }} />
            <Area
              yAxisId="gmv"
              type="monotone"
              dataKey="gmvAmount"
              name="GMV"
              stroke="#8b7355"
              fill="url(#gmvAreaFill)"
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 4 }}
            />
            <Line
              yAxisId="fee"
              type="monotone"
              dataKey="platformFeeAmount"
              name="Phí sàn"
              stroke="#c4a574"
              strokeWidth={2}
              dot={false}
              activeDot={{ r: 4 }}
            />
          </ComposedChart>
        </ResponsiveContainer>
      </div>
    </FinanceChartPanel>
  );
}
