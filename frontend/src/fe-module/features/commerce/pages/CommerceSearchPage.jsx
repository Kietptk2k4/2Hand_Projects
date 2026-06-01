import { useCallback, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FeedToast } from "../../social/components/FeedToast";
import { CommerceSearchBar } from "../components/CommerceSearchBar";
import { CommerceSearchResultsHeader } from "../components/CommerceSearchResultsHeader";
import { CommerceSearchSidebar } from "../components/CommerceSearchSidebar";
import { CommerceShell } from "../components/CommerceShell";
import { ProductCard } from "../components/ProductCard";
import { ProductListSkeleton } from "../components/ProductListSkeleton";
import { useProductSearch } from "../hooks/useProductSearch";
import { buildCommerceSearchPath } from "../utils/commerceSearchRoutes";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { MIN_KEYWORD_LENGTH } from "../constants/productSearchConstants";

const COMING_SOON_MESSAGE = "Tính năng đang được phát triển.";

export function CommerceSearchPage() {
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");
  const {
    q,
    keyword,
    items,
    sort,
    changeSort,
    isQueryTooShort,
    isInitialLoading,
    isLoadingMore,
    hasNext,
    errorMessage,
    totalItems,
    loadMore,
    retry,
  } = useProductSearch();

  const showComingSoon = useCallback(() => {
    setToastMessage(COMING_SOON_MESSAGE);
  }, []);

  const dismissToast = useCallback(() => {
    setToastMessage("");
  }, []);

  const openProduct = useCallback(
    (productId) => {
      if (!productId) return;
      navigate(APP_ROUTES.commerceProductDetail.replace(":productId", productId));
    },
    [navigate]
  );

  const handleSelectKeyword = useCallback(
    (nextKeyword) => {
      navigate(buildCommerceSearchPath(nextKeyword));
    },
    [navigate]
  );

  const handleInvalidKeyword = useCallback(() => {
    setToastMessage("Nhập ít nhất 2 ký tự.");
  }, []);

  const displayKeyword = keyword || q;
  const emptyQuery = !q;
  const emptyResults =
    !emptyQuery && !isQueryTooShort && !isInitialLoading && !errorMessage && items.length === 0;

  return (
    <CommerceShell onComingSoon={showComingSoon}>
      <div className="flex gap-6">
        <CommerceSearchSidebar
          onSelectKeyword={handleSelectKeyword}
          refreshKey={displayKeyword}
          onComingSoon={showComingSoon}
        />

        <div className="min-w-0 flex-1">
          <div className="sticky top-16 z-10 -mx-4 mb-6 bg-surface-container-lowest/95 px-4 py-3 backdrop-blur-sm md:static md:mx-0 md:bg-transparent md:px-0 md:py-0">
            <CommerceSearchBar onInvalidKeyword={handleInvalidKeyword} />
          </div>

          {emptyQuery ? (
            <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
              <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
                search
              </span>
              <p className="text-sm text-on-surface-variant">Nhập từ khóa để tìm sản phẩm</p>
              <p className="mt-2 text-xs text-on-surface-variant">
                Gợi ý: thử từ khóa trong mục &quot;Tìm kiếm gần đây&quot; bên trái.
              </p>
            </div>
          ) : null}

          {isQueryTooShort ? (
            <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
              <p className="text-sm text-on-surface-variant">
                Nhập ít nhất {MIN_KEYWORD_LENGTH} ký tự để tìm kiếm.
              </p>
            </div>
          ) : null}

          {!emptyQuery && !isQueryTooShort ? (
            <>
              <CommerceSearchResultsHeader
                keyword={displayKeyword}
                displayedCount={items.length}
                totalItems={totalItems}
                sort={sort}
                onSortChange={changeSort}
                sortDisabled={isInitialLoading}
              />

              {isInitialLoading ? <ProductListSkeleton /> : null}

              {!isInitialLoading && errorMessage ? (
                <div className="rounded-xl border border-error/30 bg-error-container/40 p-6 text-center">
                  <p className="text-sm text-on-error-container">{errorMessage}</p>
                  <button
                    type="button"
                    onClick={retry}
                    className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
                  >
                    Thử lại
                  </button>
                </div>
              ) : null}

              {emptyResults ? (
                <div className="rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
                  <span className="material-symbols-outlined mb-2 text-4xl text-outline" aria-hidden="true">
                    search_off
                  </span>
                  <p className="text-sm text-on-surface-variant">
                    Không tìm thấy sản phẩm phù hợp với &quot;{displayKeyword}&quot;.
                  </p>
                </div>
              ) : null}

              {!isInitialLoading && !errorMessage && items.length > 0 ? (
                <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 xl:grid-cols-4">
                  {items.map((product) => (
                    <ProductCard
                      key={product.productId}
                      product={product}
                      onOpenProduct={openProduct}
                      onComingSoon={showComingSoon}
                    />
                  ))}
                </div>
              ) : null}

              {!isInitialLoading && !errorMessage && hasNext ? (
                <div className="mt-10 flex justify-center">
                  {isLoadingMore ? (
                    <div
                      className="h-8 w-8 animate-spin rounded-full border-4 border-[#d8e3fb] border-t-primary"
                      aria-label="Đang tải thêm"
                    />
                  ) : (
                    <button
                      type="button"
                      onClick={loadMore}
                      className="rounded-md border-2 border-primary px-8 py-3 text-label-md font-bold text-primary transition-colors hover:bg-primary hover:text-on-primary"
                    >
                      Tải thêm sản phẩm
                    </button>
                  )}
                </div>
              ) : null}
            </>
          ) : null}
        </div>
      </div>

      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </CommerceShell>
  );
}
