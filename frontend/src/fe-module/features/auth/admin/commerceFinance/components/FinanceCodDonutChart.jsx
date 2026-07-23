import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { COD_BUCKET_LABELS } from "../constants/financeOverviewConstants.js";
import { FinanceChartPanel } from "./FinanceChartPanel.jsx";

const COLORS = ["#8b7355", "#c4a574", "#5b7c6a"];

export function FinanceCodDonutChart({ codPipeline, status, errorMessage, onRetry }) {
  const data = [
    {
      key: "inTransit",
      name: COD_BUCKET_LABELS.inTransit,
      value: codPipeline?.inTransit?.amount ?? 0,
      count: codPipeline?.inTransit?.itemCount ?? 0,
    },
    {
      key: "pendingConfirm",
      name: COD_BUCKET_LABELS.pendingConfirm,
      value: codPipeline?.pendingConfirm?.amount ?? 0,
      count: codPipeline?.pendingConfirm?.itemCount ?? 0,
    },
    {
      key: "recognized",
      name: COD_BUCKET_LABELS.recognized,
      value: codPipeline?.recognized?.amount ?? 0,
      count: codPipeline?.recognized?.itemCount ?? 0,
    },
  ].filter((item) => item.value > 0);

  const total = data.reduce((sum, item) => sum + item.value, 0);

  return (
    <FinanceChartPanel
      title="COD pipeline"
      subtitle="Phân bổ theo giai đoạn fulfillment"
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
              {data.map((entry, index) => (
                <Cell key={entry.key} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip
              formatter={(value, name, props) => [
                `${formatVndPrice(value)} · ${props.payload.count} MH`,
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
        {data.map((item, index) => (
          <li key={item.key} className="flex items-center justify-between gap-2">
            <span className="inline-flex items-center gap-2 text-admin-text-secondary">
              <span
                className="h-2.5 w-2.5 rounded-sm"
                style={{ backgroundColor: COLORS[index % COLORS.length] }}
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
