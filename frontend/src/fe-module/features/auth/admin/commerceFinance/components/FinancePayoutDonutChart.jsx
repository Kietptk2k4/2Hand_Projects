import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { PAYOUT_STATUS_LABELS } from "../constants/financeOverviewConstants.js";
import { FinanceChartPanel } from "./FinanceChartPanel.jsx";

const COLORS = {
  REQUESTED: "#c4a574",
  APPROVED: "#5b7c6a",
  PAID: "#8b7355",
  REJECTED: "#b45353",
  CANCELLED: "#9ca3af",
};

export function FinancePayoutDonutChart({
  payoutOverview,
  status,
  errorMessage,
  onRetry,
}) {
  const data = (payoutOverview || [])
    .map((item) => ({
      ...item,
      name: PAYOUT_STATUS_LABELS[item.status] || item.status,
      value: item.totalAmount,
    }))
    .filter((item) => item.value > 0);

  const total = data.reduce((sum, item) => sum + item.value, 0);

  return (
    <FinanceChartPanel
      title="Payout theo trạng thái"
      subtitle="Tổng tiền yêu cầu rút trong kỳ"
      status={status}
      errorMessage={errorMessage}
      onRetry={onRetry}
      empty={!data.length}
    >
      <div className="relative h-64 w-full sm:h-72">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={data}
              dataKey="value"
              nameKey="name"
              innerRadius="58%"
              outerRadius="82%"
              paddingAngle={2}
              stroke="none"
            >
              {data.map((entry) => (
                <Cell
                  key={entry.status}
                  fill={COLORS[entry.status] || "#8b7355"}
                />
              ))}
            </Pie>
            <Tooltip
              formatter={(value, name, props) => [
                `${formatVndPrice(value)} · ${props.payload.requestCount} YC`,
                name,
              ]}
            />
          </PieChart>
        </ResponsiveContainer>
        <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
          <p className="text-[11px] uppercase tracking-wide text-admin-text-muted">Tổng</p>
          <p className="mt-1 max-w-[9rem] truncate text-center text-sm font-semibold tabular-nums text-admin-text">
            {formatVndPrice(total)}
          </p>
        </div>
      </div>
      <ul className="mt-2 space-y-1.5 text-sm">
        {data.map((item) => (
          <li key={item.status} className="flex items-center justify-between gap-2">
            <span className="inline-flex items-center gap-2 text-admin-text-secondary">
              <span
                className="h-2.5 w-2.5 rounded-sm"
                style={{ backgroundColor: COLORS[item.status] || "#8b7355" }}
                aria-hidden="true"
              />
              {item.name}
            </span>
            <span className="tabular-nums text-admin-text">{formatVndPrice(item.value)}</span>
          </li>
        ))}
      </ul>
    </FinanceChartPanel>
  );
}
