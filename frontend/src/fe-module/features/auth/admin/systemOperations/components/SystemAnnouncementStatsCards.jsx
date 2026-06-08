const CARD_META = [
  { key: "DRAFT", label: "Draft", tone: "bg-slate-100 text-slate-800" },
  { key: "SENT", label: "Đã gửi", tone: "bg-green-100 text-green-800" },
  { key: "CANCELLED", label: "Đã hủy", tone: "bg-red-100 text-red-800" },
];

export function SystemAnnouncementStatsCards({ stats, totalElements }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4">
        <p className="text-xs font-semibold uppercase text-on-surface-variant">Tổng (bộ lọc)</p>
        <p className="mt-2 text-2xl font-semibold text-on-surface">{totalElements ?? 0}</p>
      </div>
      {CARD_META.map((card) => (
        <div key={card.key} className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4">
          <p className="text-xs font-semibold uppercase text-on-surface-variant">{card.label}</p>
          <p className={`mt-2 inline-flex rounded-full px-3 py-1 text-lg font-semibold ${card.tone}`}>
            {stats?.[card.key] ?? 0}
          </p>
          <p className="mt-1 text-xs text-on-surface-variant">trên trang hiện tại</p>
        </div>
      ))}
    </div>
  );
}