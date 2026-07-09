import { AdminMetricCard } from "../../components/ui";
import { AnnouncementStatusBadge } from "./ui/SystemOperationsBadges.jsx";

const CARD_META = [
  { key: "DRAFT", label: "Draft" },
  { key: "SENT", label: "Đã gửi" },
  { key: "CANCELLED", label: "Đã hủy" },
];

export function SystemAnnouncementStatsCards({ stats, totalElements }) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <AdminMetricCard label="Tổng (bộ lọc)" value={totalElements ?? 0} />
      {CARD_META.map((card) => (
        <AdminMetricCard
          key={card.key}
          label={card.label}
          value={stats?.[card.key] ?? 0}
          hint="trên trang hiện tại"
          footer={<AnnouncementStatusBadge status={card.key} />}
        />
      ))}
    </div>
  );
}
