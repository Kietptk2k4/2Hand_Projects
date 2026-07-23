import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { formatVndPrice } from "../../../../social/utils/formatPrice";
import { FinanceChartPanel } from "./FinanceChartPanel.jsx";

function compactVnd(value) {
  const n = Number(value) || 0;
  if (n >= 1_000_000) return `${Math.round(n / 100_000) / 10}Tr`;
  if (n >= 1_000) return `${Math.round(n / 100) / 10}N`;
  return String(n);
}

export function FinanceTopSellersBarChart({
  topSellers,
  status,
  errorMessage,
  onRetry,
  onSellerClick,
  chartLimit = 10,
  title = "Top sellers",
  subtitle = "Theo GMV đã ghi nhận trong kỳ",
}) {
  const data = (topSellers || []).slice(0, chartLimit).map((seller) => ({
    ...seller,
    name: seller.shopName || String(seller.sellerId || "").slice(0, 8),
  }));

  const handleBarClick = (payload) => {
    const sellerId = payload?.sellerId;
    if (sellerId && onSellerClick) onSellerClick(sellerId);
  };

  return (
    <FinanceChartPanel
      title={title}
      subtitle={subtitle}
      status={status}
      errorMessage={errorMessage}
      onRetry={onRetry}
      empty={!data.length}
    >
      <div className="h-72 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart
            data={data}
            layout="vertical"
            margin={{ top: 4, right: 16, left: 8, bottom: 4 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(0,0,0,0.06)" horizontal={false} />
            <XAxis
              type="number"
              tickFormatter={compactVnd}
              tick={{ fontSize: 11, fill: "#6b7280" }}
              axisLine={false}
              tickLine={false}
            />
            <YAxis
              type="category"
              dataKey="name"
              width={96}
              tick={{ fontSize: 11, fill: "#6b7280" }}
              axisLine={false}
              tickLine={false}
            />
            <Tooltip
              formatter={(value, _name, props) => [
                formatVndPrice(value),
                props.payload.shopName || "GMV",
              ]}
              labelFormatter={() => ""}
            />
            <Bar
              dataKey="recognizedGross"
              name="GMV"
              fill="#8b7355"
              radius={[0, 6, 6, 0]}
              barSize={18}
              cursor={onSellerClick ? "pointer" : "default"}
              onClick={(entry) => handleBarClick(entry?.payload || entry)}
            />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </FinanceChartPanel>
  );
}
