import { useNavigate } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { useSuggestedUsers } from "../hooks/useSuggestedUsers";
import { useTrendingHashtags, formatTrendingPostCount } from "../hooks/useTrendingHashtags";
import { buildSocialHashtagPath } from "../utils/socialHashtagRoutes";
import { SuggestedUserListItem } from "./SuggestedUserListItem";
import { useProductList } from "../../commerce/hooks/useProductList";
import { formatVndPrice } from "../utils/formatPrice";
import { buildCommerceProductDetailPath } from "../../commerce/utils/commerceRoutes";

export function FeedRightSidebar({ onComingSoon, onViewProfile, onSelectHashtag, onToast }) {
  const navigate = useNavigate();
  const {
    items: trendingItems,
    isLoading: isTrendingLoading,
    isError: isTrendingError,
    errorMessage: trendingErrorMessage,
  } = useTrendingHashtags({ limit: 3 });
  const {
    items: suggestedItems,
    isLoading: isSuggestionsLoading,
    isError: isSuggestionsError,
    errorMessage: suggestionsErrorMessage,
    hasMore: hasMoreSuggestions,
    handleFollowToggle,
    followButtonLabel,
    suggestionSubtitle,
    loadingUserId,
    followDisabled,
  } = useSuggestedUsers({ onToast });

  // TODO: swap to /recommend/products API when ready
  const { items: productItems, isInitialLoading: isProductsLoading } = useProductList();
  const interestedProducts = (productItems || []).slice(0, 4);

  const goToHashtag = (tag) => {
    const normalized = tag?.replace(/^#+/, "").trim();
    if (!normalized) return;
    if (onSelectHashtag) {
      onSelectHashtag(normalized);
      return;
    }
    navigate(buildSocialHashtagPath(normalized));
  };

  const goToAllSuggestions = () => {
    navigate(APP_ROUTES.socialSuggestedUsers);
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    const query = e.target.search?.value?.trim();
    if (query) {
      navigate(`${APP_ROUTES.socialSearchPosts}?q=${encodeURIComponent(query)}`);
    }
  };

  return (
    <aside className="sticky top-16 z-30 hidden flex-col gap-4 py-2 px-2 lg:col-span-3 lg:flex lg:self-start">
      {/* Sticky Search Bar Header */}
      <div className="sticky top-16 z-30 bg-surface-container-lowest pt-2 pb-1">
        <form onSubmit={handleSearchSubmit}>
          <div className="flex items-center gap-2.5 rounded-full border border-outline-variant/30 bg-surface-container-low px-4 py-2.5 text-on-surface-variant transition-all focus-within:border-on-surface focus-within:bg-surface-container-lowest focus-within:ring-2 focus-within:ring-on-surface/20">
            <span className="material-symbols-outlined text-[20px] text-on-surface-variant/70" aria-hidden="true">
              search
            </span>
            <input
              type="text"
              name="search"
              placeholder="Tìm kiếm bài viết..."
              className="w-full bg-transparent text-sm text-on-surface outline-none placeholder:text-on-surface-variant/60"
            />
          </div>
        </form>
      </div>

      {/* Trending Box */}
      <div className="rounded-2xl border border-outline-variant/40 bg-surface-container-lowest overflow-hidden">
        <h3 className="text-xl font-extrabold px-4 pt-4 pb-0.5 text-on-surface">Xu hướng 24h</h3>
        <p className="px-4 pb-2 text-xs text-on-surface-variant/70">Chủ đề thảo luận hot 24h qua</p>
        {isTrendingLoading ? (
          <p className="px-4 py-3 text-[15px] text-on-surface-variant/70">Đang tải...</p>
        ) : isTrendingError ? (
          <p className="px-4 py-3 text-[15px] text-error">{trendingErrorMessage}</p>
        ) : trendingItems.length === 0 ? (
          <p className="px-4 py-3 text-[15px] text-on-surface-variant/70">Chưa có hashtag thịnh hành 24h.</p>
        ) : (
          <ul className="divide-y divide-outline-variant/20">
            {trendingItems.map((item) => (
              <li key={item.tag}>
                <button
                  type="button"
                  className="flex flex-col gap-0.5 px-4 py-3 hover:bg-surface-container-low/50 transition-colors w-full text-left group"
                  onClick={() => goToHashtag(item.tag)}
                >
                  <span className="text-[15px] font-bold leading-tight text-sky-500 group-hover:underline">
                    #{item.tag}
                  </span>
                  <span className="text-[15px] text-on-surface-variant/70">
                    {formatTrendingPostCount(item.postCount)}
                  </span>
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Suggested Users Box */}
      <div className="rounded-2xl border border-outline-variant/40 bg-surface-container-lowest overflow-hidden">
        <h3 className="text-xl font-extrabold px-4 pt-4 pb-2 text-on-surface">Gợi ý theo dõi</h3>
        {isSuggestionsLoading ? (
          <p className="px-4 py-3 text-[15px] text-on-surface-variant/70">Đang tải...</p>
        ) : isSuggestionsError ? (
          <p className="px-4 py-3 text-[15px] text-error">{suggestionsErrorMessage}</p>
        ) : suggestedItems.length === 0 ? (
          <p className="px-4 py-3 text-[15px] text-on-surface-variant/70">Chưa có gợi ý người dùng.</p>
        ) : (
          <ul className="divide-y divide-outline-variant/20">
            {suggestedItems.map((item) => (
              <SuggestedUserListItem
                key={item.userId}
                item={item}
                onViewProfile={onViewProfile}
                onFollowToggle={handleFollowToggle}
                followButtonLabel={followButtonLabel}
                suggestionSubtitle={suggestionSubtitle}
                loadingUserId={loadingUserId}
                followDisabled={followDisabled}
              />
            ))}
          </ul>
        )}
        {hasMoreSuggestions && suggestedItems.length > 0 ? (
          <button
            type="button"
            onClick={goToAllSuggestions}
            className="px-4 py-3 text-[15px] font-normal text-sky-500 hover:bg-surface-container-low/50 transition-colors w-full text-left block"
          >
            Xem tất cả gợi ý →
          </button>
        ) : null}
      </div>

      {/* Recommended Products Widget ("Có thể bạn quan tâm") */}
      <div className="rounded-2xl border border-outline-variant/40 bg-surface-container-lowest overflow-hidden">
        <h3 className="text-xl font-extrabold px-4 pt-4 pb-2 text-on-surface">Có thể bạn quan tâm</h3>
        {isProductsLoading ? (
          <p className="px-4 py-3 text-[15px] text-on-surface-variant/70">Đang tải...</p>
        ) : interestedProducts.length === 0 ? (
          <p className="px-4 py-3 text-[15px] text-on-surface-variant/70">Chưa có sản phẩm gợi ý.</p>
        ) : (
          <ul className="divide-y divide-outline-variant/20">
            {interestedProducts.map((product, index) => {
              const badgeLabel = index === 0 ? "🔥 Hot" : index === 1 ? "Mới" : "👁 Xem nhiều";
              return (
                <li key={product.productId || index}>
                  <button
                    type="button"
                    onClick={() => navigate(buildCommerceProductDetailPath(product.productId))}
                    className="flex items-center gap-3 px-4 py-3 hover:bg-surface-container-low/50 transition-colors group w-full text-left"
                  >
                    {product.thumbnailUrl ? (
                      <img
                        src={product.thumbnailUrl}
                        alt=""
                        className="w-14 h-14 shrink-0 rounded-lg object-cover border border-outline-variant/30"
                      />
                    ) : (
                      <div className="flex w-14 h-14 shrink-0 items-center justify-center rounded-lg bg-surface-container-high text-outline">
                        <span className="material-symbols-outlined text-xl">inventory_2</span>
                      </div>
                    )}
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center justify-between gap-1">
                        <span className="truncate text-[15px] font-bold leading-tight text-on-surface group-hover:underline">
                          {product.title}
                        </span>
                        <span className="shrink-0 rounded-full bg-on-surface/10 px-2 py-0.5 text-xs font-semibold text-on-surface">
                          {badgeLabel}
                        </span>
                      </div>
                      <p className="mt-1 text-[15px] font-bold text-on-surface">
                        {formatVndPrice(product.effectivePrice ?? product.price)}
                      </p>
                    </div>
                  </button>
                </li>
              );
            })}
          </ul>
        )}
        <button
          type="button"
          onClick={() => navigate(APP_ROUTES.commerceHome)}
          className="px-4 py-3 text-[15px] font-normal text-sky-500 hover:bg-surface-container-low/50 transition-colors w-full text-left block"
        >
          Xem thêm sản phẩm →
        </button>
      </div>
    </aside>
  );
}
