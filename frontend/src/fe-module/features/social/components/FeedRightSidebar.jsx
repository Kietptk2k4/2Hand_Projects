import { useNavigate } from "react-router-dom";
import { SUGGESTED_PROVIDERS } from "../constants/suggestedProviders";
import { buildSocialHashtagPath } from "../utils/socialHashtagRoutes";

const TRENDING = [
  { tag: "RemoteWork2024", count: "12.5k posts" },
  { tag: "AIinFinance", count: "8.2k posts" },
  { tag: "FreelanceTips", count: "5.1k posts" },
  { tag: "LegalTech", count: "3.9k posts" },
];

const SUGGESTIONS = SUGGESTED_PROVIDERS;

export function FeedRightSidebar({ onComingSoon, onViewProfile, onSelectHashtag }) {
  const navigate = useNavigate();

  const goToHashtag = (tag) => {
    const normalized = tag?.replace(/^#+/, "").trim();
    if (!normalized) return;
    if (onSelectHashtag) {
      onSelectHashtag(normalized);
      return;
    }
    navigate(buildSocialHashtagPath(normalized));
  };

  return (
    <aside className="hidden flex-col gap-6 lg:col-span-3 lg:flex lg:sticky lg:top-20 lg:max-h-[calc(100vh-5rem)] lg:self-start lg:overflow-y-auto">
      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <h3 className="mb-4 text-xl font-semibold text-on-surface">Trending Now</h3>
        <ul className="flex flex-col gap-3">
          {TRENDING.map((item) => (
            <li key={item.tag} className="flex flex-col">
              <button
                type="button"
                className="text-left text-sm font-medium text-primary hover:underline"
                onClick={() => goToHashtag(item.tag)}
              >
                #{item.tag}
              </button>
              <span className="text-xs font-semibold text-on-surface-variant">{item.count}</span>
            </li>
          ))}
        </ul>
      </div>

      <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
        <h3 className="mb-4 text-xl font-semibold text-on-surface">Suggested Providers</h3>
        <ul className="flex flex-col gap-4">
          {SUGGESTIONS.map((item) => (
            <li key={item.userId} className="flex items-center justify-between gap-2">
              <button
                type="button"
                onClick={() => onViewProfile?.(item.userId)}
                className="flex min-w-0 flex-1 items-center gap-3 text-left"
              >
                <img
                  src={item.avatarUrl}
                  alt=""
                  className="h-10 w-10 shrink-0 rounded-full object-cover"
                />
                <div className="min-w-0 flex flex-col">
                  <span className="truncate text-sm font-medium text-on-surface hover:text-primary">
                    {item.name}
                  </span>
                  <span className="truncate text-xs font-semibold text-on-surface-variant">
                    {item.role}
                  </span>
                </div>
              </button>
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
