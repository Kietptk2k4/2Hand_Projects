export function RefundSupportStatsBar({ stats, status, onPresetClick }) {
  if (status === "loading") {
    return (
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {[1, 2, 3, 4].map((key) => (
          <div
            key={key}
            className="h-20 animate-pulse rounded-xl border border-admin-border bg-admin-surface-muted"
          />
        ))}
      </div>
    );
  }

  if (!stats) return null;

  const presetMap = {
    REQUESTED: "requested",
    CONFIRMED: "confirmed",
    REJECTED: "rejected",
  };

  const cards = [
    { id: "total", label: "Tổng yêu cầu", count: stats.total, clickable: false },
    ...stats.presets.map((preset) => ({ ...preset, clickable: true })),
  ];

  return (
    <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
      {cards.map((card) => {
        const content = (
          <>
            <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
              {card.label}
            </p>
            <p className="mt-2 text-2xl font-semibold tabular-nums text-admin-text">{card.count}</p>
          </>
        );

        if (!card.clickable) {
          return (
            <div
              key={card.id}
              className="rounded-xl border border-admin-border bg-admin-surface-raised px-4 py-3"
            >
              {content}
            </div>
          );
        }

        return (
          <button
            key={card.id}
            type="button"
            onClick={() => onPresetClick?.(presetMap[card.id] || card.id)}
            className="rounded-xl border border-admin-border bg-admin-surface-raised px-4 py-3 text-left transition-colors hover:border-admin-accent-border hover:bg-admin-accent-soft/30 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
          >
            {content}
          </button>
        );
      })}
    </div>
  );
}
