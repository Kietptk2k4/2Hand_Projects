const TRENDING = [
  { tag: "#RemoteWork2024", count: "12.5k posts" },
  { tag: "#AIinFinance", count: "8.2k posts" },
  { tag: "#FreelanceTips", count: "5.1k posts" },
  { tag: "#LegalTech", count: "3.9k posts" },
];

const SUGGESTIONS = [
  { name: "Elena Rodriguez", role: "Graphic Designer", img: 12 },
  { name: "Marcus Johnson", role: "Business Analyst", img: 33 },
  { name: "Chloe Smith", role: "Copywriter", img: 45 },
];

export function FeedRightSidebar({ onComingSoon }) {
  return (
    <aside className="hidden flex-col gap-6 lg:flex lg:col-span-3">
      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <h3 className="mb-4 text-xl font-semibold text-on-surface">Trending Now</h3>
        <ul className="flex flex-col gap-3">
          {TRENDING.map((item) => (
            <li key={item.tag} className="flex flex-col">
              <a
                href="#"
                className="text-sm font-medium text-primary hover:underline"
                onClick={(event) => {
                  event.preventDefault();
                  onComingSoon?.();
                }}
              >
                {item.tag}
              </a>
              <span className="text-xs font-semibold text-on-surface-variant">{item.count}</span>
            </li>
          ))}
        </ul>
      </div>

      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <h3 className="mb-4 text-xl font-semibold text-on-surface">Suggested Providers</h3>
        <ul className="flex flex-col gap-4">
          {SUGGESTIONS.map((item) => (
            <li key={item.name} className="flex items-center justify-between gap-2">
              <div className="flex min-w-0 items-center gap-3">
                <img
                  src={`https://i.pravatar.cc/80?img=${item.img}`}
                  alt=""
                  className="h-10 w-10 shrink-0 rounded-full object-cover"
                />
                <div className="min-w-0 flex flex-col">
                  <span className="truncate text-sm font-medium text-on-surface">{item.name}</span>
                  <span className="truncate text-xs font-semibold text-on-surface-variant">{item.role}</span>
                </div>
              </div>
              <button
                type="button"
                onClick={onComingSoon}
                className="shrink-0 rounded-full border border-primary px-3 py-1 text-xs font-semibold text-primary transition-colors hover:bg-[#e7eeff]"
              >
                Follow
              </button>
            </li>
          ))}
        </ul>
        <button
          type="button"
          onClick={onComingSoon}
          className="mt-4 block w-full text-center text-sm font-medium text-primary hover:underline"
        >
          View all recommendations
        </button>
      </div>
    </aside>
  );
}
