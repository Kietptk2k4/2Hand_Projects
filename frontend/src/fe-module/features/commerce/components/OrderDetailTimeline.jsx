import { formatOrderDate } from "../utils/formatOrderDate";

export function OrderDetailTimeline({ events, isLoading }) {
  return (
    <section className="rounded-xl border border-outline-variant bg-surface-container-lowest p-4 shadow-sm md:p-6">
      <h2 className="mb-4 text-headline-sm font-semibold text-on-surface">Tiến trình đơn hàng</h2>

      {isLoading && !events?.length ? (
        <p className="text-body-sm text-on-surface-variant">Đang tải tiến trình...</p>
      ) : null}

      {!isLoading && !events?.length ? (
        <p className="text-body-sm text-on-surface-variant">Chưa có sự kiện theo dõi.</p>
      ) : null}

      {events?.length ? (
        <div className="relative pl-2">
          <div
            className="absolute bottom-6 left-[15px] top-2 w-0.5 bg-outline-variant"
            aria-hidden="true"
          />
          <ul className="space-y-6">
            {events.map((event) => (
              <li key={event.id} className="relative z-10 flex gap-4">
                <div
                  className={[
                    "mt-1 flex h-6 w-6 shrink-0 items-center justify-center rounded-full border-2 border-surface-container-lowest",
                    event.isLatest ? "bg-primary" : "bg-primary/80",
                  ].join(" ")}
                >
                  <span
                    className="material-symbols-outlined text-[14px] text-on-primary"
                    style={{ fontVariationSettings: "'FILL' 1" }}
                    aria-hidden="true"
                  >
                    check
                  </span>
                </div>
                <div>
                  <p
                    className={[
                      "text-label-md",
                      event.isLatest ? "font-semibold text-on-surface" : "text-on-surface",
                    ].join(" ")}
                  >
                    {event.label}
                  </p>
                  <p className="text-body-sm text-on-surface-variant">
                    {formatOrderDate(event.occurredAt)}
                  </p>
                </div>
              </li>
            ))}
          </ul>
        </div>
      ) : null}
    </section>
  );
}
