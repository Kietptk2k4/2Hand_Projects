import { useEffect, useMemo, useState } from "react";
import { useTrendingHashtags, formatTrendingPostCount } from "../hooks/useTrendingHashtags";
import { clearSearchHistory, getSearchHistory } from "../utils/searchHistoryStorage";
import { useFeed } from "../hooks/useFeed";
import { mapProductTagsFromApi } from "../utils/mapProductTagsFromApi";
import { usePostAuthorDisplay } from "../hooks/usePostAuthorDisplay";
import { formatRelativeTime } from "../utils/formatRelativeTime";
import { formatVndPrice } from "../utils/formatPrice";

function TrendingPostRowItem({ post, index, onOpenPost }) {
  const author = usePostAuthorDisplay(post.authorId);
  const productTags = mapProductTagsFromApi(post.productTags);
  const primaryProduct = productTags[0];

  return (
    <div
      onClick={() => onOpenPost?.(post.postId)}
      className="flex flex-col gap-1.5 border-b border-outline-variant/30 px-4 py-3.5 transition-colors hover:bg-surface-container-low/60 cursor-pointer group"
    >
      {/* 1. Rank Prefix */}
      <span className="text-xs font-semibold text-on-surface-variant/60">
        #{index + 1} · Thịnh hành
      </span>

      {/* 2. Main Headline (Caption) - Enlarged text-base */}
      <h3 className="line-clamp-2 text-base font-bold leading-snug text-on-surface transition-colors group-hover:text-sky-500">
        {post.caption || "Bài viết chia sẻ..."}
      </h3>

      {/* 3. Single Meta Line: Avatar (24px) + Author + Time + Stats */}
      <div className="flex flex-wrap items-center gap-2 text-xs font-medium text-on-surface-variant/80">
        <img
          src={
            author.avatarUrl ||
            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80"
          }
          alt={author.displayName}
          className="h-6 w-6 rounded-full object-cover border border-outline-variant/40 shrink-0"
        />
        <span className="font-bold text-on-surface hover:underline">{author.displayName}</span>
        <span>•</span>
        <span>{formatRelativeTime(post.createdAt)}</span>
        <span>•</span>
        <span className="flex items-center gap-1 font-semibold">
          <span className="material-symbols-outlined text-[14px] text-sky-500">thumb_up</span>
          {post.likeCount || 0}
        </span>
        <span>•</span>
        <span className="flex items-center gap-1 font-semibold">
          <span className="material-symbols-outlined text-[14px] text-on-surface-variant/60">comment</span>
          {post.replyCount || 0}
        </span>
      </div>

      {/* 4. Product Pill Tag (Refined Monochrome Tone) */}
      {primaryProduct ? (
        <div className="mt-1 inline-flex items-center gap-1.5 rounded-full bg-surface-container-low px-3 py-1 text-xs font-semibold text-on-surface border border-outline-variant/60 hover:bg-surface-container-high transition-colors max-w-full w-fit">
          <span className="truncate">{primaryProduct.name} - <span className="font-bold">{formatVndPrice(primaryProduct.price)}</span></span>
        </div>
      ) : null}
    </div>
  );
}

export function ExploreDefaultOverview({ onSelectKeyword, onSelectHashtag, onOpenPost }) {
  const [history, setHistory] = useState([]);
  const { items: trendingItems, isLoading: isTrendingLoading } = useTrendingHashtags({ limit: 10 });
  const { items: feedPosts, isInitialLoading: isFeedLoading } = useFeed("global");

  const topTrendingPosts = useMemo(() => {
    const TWENTY_FOUR_HOURS_MS = 24 * 60 * 60 * 1000;
    const now = Date.now();

    const postsIn24h = (feedPosts || []).filter((post) => {
      if (!post.createdAt) return false;
      const postTime = new Date(post.createdAt).getTime();
      return now - postTime <= TWENTY_FOUR_HOURS_MS;
    });

    const candidatePosts = postsIn24h.length > 0 ? postsIn24h : (feedPosts || []);

    return candidatePosts
      .slice()
      .sort((a, b) => {
        const scoreB = ((b.likeCount || 0) * 10) + ((b.replyCount || 0) * 5);
        const scoreA = ((a.likeCount || 0) * 10) + ((a.replyCount || 0) * 5);
        if (scoreB !== scoreA) {
          return scoreB - scoreA;
        }
        const timeB = new Date(b.createdAt || 0).getTime();
        const timeA = new Date(a.createdAt || 0).getTime();
        return timeB - timeA;
      })
      .slice(0, 5);
  }, [feedPosts]);

  const refreshHistory = () => {
    setHistory(getSearchHistory());
  };

  useEffect(() => {
    refreshHistory();
  }, []);

  const handleClearHistory = () => {
    clearSearchHistory();
    refreshHistory();
  };

  return (
    <div className="flex flex-col gap-6 p-4">
      {/* 1. Lịch sử tìm kiếm gần đây */}
      {history.length > 0 ? (
        <section className="rounded-2xl border border-outline-variant/40 bg-surface-container-lowest p-4">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="text-xs font-extrabold uppercase tracking-wider text-on-surface-variant/70 flex items-center gap-1.5">
              <span className="material-symbols-outlined text-[16px]">history</span>
              Lịch sử tìm kiếm gần đây
            </h2>
            <button
              type="button"
              onClick={handleClearHistory}
              className="text-xs font-bold text-sky-500 hover:underline"
            >
              Xóa lịch sử
            </button>
          </div>
          <div className="flex flex-wrap gap-2">
            {history.map((item) => (
              <button
                key={item}
                type="button"
                onClick={() => onSelectKeyword?.(item)}
                className="flex items-center gap-1.5 rounded-full border border-outline-variant/50 bg-surface-container-low px-3.5 py-1.5 text-xs font-semibold text-on-surface transition-all hover:border-sky-500/50 hover:bg-surface-container-high"
              >
                <span className="material-symbols-outlined text-[14px] text-on-surface-variant/60">
                  search
                </span>
                <span>{item}</span>
              </button>
            ))}
          </div>
        </section>
      ) : null}

      {/* 2. Top 5 Bài viết thịnh hành nhất 24h qua */}
      <section className="rounded-2xl border border-outline-variant/40 bg-surface-container-lowest overflow-hidden">
        <div className="px-4 pt-4 pb-3 border-b border-outline-variant/30">
          <h2 className="text-base font-extrabold text-on-surface flex items-center gap-2">
            <span className="material-symbols-outlined text-[20px] text-sky-500">trending_up</span>
            Bài viết sôi nổi hôm nay
          </h2>
        </div>

        {isFeedLoading ? (
          <div className="p-4 space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-16 animate-pulse rounded-xl bg-surface-container-low" />
            ))}
          </div>
        ) : topTrendingPosts.length === 0 ? (
          <p className="p-4 text-xs text-on-surface-variant/70">Chưa có dữ liệu bài viết thịnh hành 24h.</p>
        ) : (
          <div className="flex flex-col divide-y divide-outline-variant/20">
            {topTrendingPosts.map((post, index) => (
              <TrendingPostRowItem
                key={post.postId}
                post={post}
                index={index}
                onOpenPost={onOpenPost}
              />
            ))}
          </div>
        )}
      </section>

      {/* 3. Bảng xếp hạng Hashtag Thịnh Hành 24h */}
      <section className="rounded-2xl border border-outline-variant/40 bg-surface-container-lowest overflow-hidden">
        <div className="px-4 pt-4 pb-3 border-b border-outline-variant/30">
          <h2 className="text-base font-extrabold text-on-surface flex items-center gap-2">
            <span className="material-symbols-outlined text-[20px] text-sky-500">local_fire_department</span>
            Hashtag thịnh hành 24 giờ qua
          </h2>
        </div>

        {isTrendingLoading ? (
          <div className="p-4 space-y-3">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-10 animate-pulse rounded-lg bg-surface-container-low" />
            ))}
          </div>
        ) : trendingItems.length === 0 ? (
          <p className="p-4 text-xs text-on-surface-variant/70">Chưa có dữ liệu hashtag thịnh hành.</p>
        ) : (
          <ul className="divide-y divide-outline-variant/20">
            {trendingItems.map((item, index) => (
              <li key={item.tag}>
                <button
                  type="button"
                  onClick={() => onSelectHashtag?.(item.tag)}
                  className="flex items-center justify-between gap-3 px-4 py-3.5 hover:bg-surface-container-low/50 transition-colors w-full text-left group"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <span className="text-xs font-extrabold text-on-surface-variant/50 w-5 text-center">
                      #{index + 1}
                    </span>
                    <div className="min-w-0 flex flex-col">
                      <span className="truncate text-[15px] font-extrabold text-sky-500 group-hover:underline">
                        #{item.tag}
                      </span>
                      <span className="text-xs text-on-surface-variant/60">
                        {formatTrendingPostCount(item.postCount)} bài viết
                      </span>
                    </div>
                  </div>
                  <span className="material-symbols-outlined text-[20px] text-on-surface-variant/40 group-hover:text-sky-500 transition-colors">
                    chevron_right
                  </span>
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
