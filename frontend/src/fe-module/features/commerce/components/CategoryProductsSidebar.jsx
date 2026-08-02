import { Link, useNavigate } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { buildCommerceCategoryPath } from "../utils/commerceRoutes";

function CategoryBreadcrumb({ categoryName }) {
  return (
    <nav
      className="mb-4 flex flex-wrap items-center text-xs font-semibold text-on-surface-variant"
      aria-label="Breadcrumb"
    >
      <Link to={APP_ROUTES.commerceHome} className="transition-colors hover:text-primary">
        Trang chủ
      </Link>
      <span className="material-symbols-outlined mx-1 text-sm" aria-hidden="true">
        chevron_right
      </span>
      <span className="font-bold text-on-surface">{categoryName || "Danh mục"}</span>
    </nav>
  );
}

export function CategoryProductsSidebar({
  activeCategoryId,
  categoryName,
  categoryItems = [],
  isLoadingCategories = false,
  activeCount,
  includeChildren,
  onIncludeChildrenChange,
}) {
  const navigate = useNavigate();

  return (
    <aside className="hidden lg:col-span-3 lg:block">
      <div className="sticky top-20 z-20 max-h-[calc(100vh-5rem)] space-y-4 overflow-y-auto overscroll-contain pr-1 no-scrollbar">
        <CategoryBreadcrumb categoryName={categoryName} />

        {/* Category List Card */}
        <div className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs">
          <h3 className="mb-4 text-xs font-black uppercase tracking-wider text-on-surface flex items-center gap-2 border-b border-outline-variant/60 pb-3">
            <span className="material-symbols-outlined text-primary text-lg">category</span>
            DANH MỤC SẢN PHẨM
          </h3>
          <ul className="space-y-1">
            {isLoadingCategories ? (
              <li className="py-2 text-xs font-semibold text-on-surface-variant animate-pulse">
                Đang tải danh mục...
              </li>
            ) : null}
            {!isLoadingCategories && categoryItems.length === 0 ? (
              <li className="py-2 text-xs font-semibold text-on-surface-variant">Chưa có danh mục.</li>
            ) : null}
            {categoryItems.map((item) => {
              const isActive = item.categoryId === activeCategoryId;
              const displayCount = isActive && activeCount != null ? activeCount : item.productCount;
              return (
                <li key={item.categoryId}>
                  <button
                    type="button"
                    onClick={() => navigate(buildCommerceCategoryPath(item.categoryId))}
                    className={[
                      "flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-xs font-bold transition-all cursor-pointer shadow-xs",
                      item.parentId ? "pl-5" : "",
                      isActive
                        ? "bg-primary text-on-primary shadow-xs ring-2 ring-primary/20 scale-[1.02]"
                        : "text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface",
                    ].join(" ")}
                  >
                    <span className="truncate">{item.categoryName}</span>
                    {displayCount != null ? (
                      <span
                        className={[
                          "rounded-md px-2 py-0.5 text-[10px] font-bold shrink-0 ml-2",
                          isActive
                            ? "bg-white/20 text-white"
                            : "bg-surface-container-high text-on-surface-variant",
                        ].join(" ")}
                      >
                        {displayCount}
                      </span>
                    ) : null}
                  </button>
                </li>
              );
            })}
          </ul>
        </div>

        {/* Filter Settings Card */}
        <div className="rounded-2xl border border-outline-variant/80 bg-surface-container-lowest p-5 shadow-xs">
          <h3 className="mb-3 text-xs font-black uppercase tracking-wider text-on-surface flex items-center gap-2 border-b border-outline-variant/60 pb-2.5">
            <span className="material-symbols-outlined text-primary text-lg">filter_alt</span>
            BỘ LỌC DANH MỤC
          </h3>
          <label className="flex cursor-pointer items-center gap-2.5 pt-1">
            <input
              type="checkbox"
              checked={includeChildren}
              onChange={(event) => onIncludeChildrenChange(event.target.checked)}
              className="h-4 w-4 rounded border-outline text-primary focus:ring-primary cursor-pointer"
            />
            <span className="text-xs font-bold text-on-surface">Bao gồm danh mục con</span>
          </label>
        </div>
      </div>
    </aside>
  );
}

export function CategoryProductsMobileNav({
  activeCategoryId,
  categoryItems = [],
  isLoadingCategories = false,
}) {
  const navigate = useNavigate();

  return (
    <nav className="mb-6 lg:hidden" aria-label="Danh mục nhanh">
      <div className="mb-3 flex flex-wrap items-center text-xs font-semibold text-on-surface-variant">
        <Link to={APP_ROUTES.commerceHome} className="hover:text-primary">
          Trang chủ
        </Link>
        <span className="material-symbols-outlined mx-1 text-sm" aria-hidden="true">
          chevron_right
        </span>
        <span className="font-bold text-on-surface">Danh mục</span>
      </div>
      <div className="flex gap-2 overflow-x-auto pb-1 no-scrollbar">
        {isLoadingCategories ? (
          <span className="shrink-0 text-xs font-semibold text-on-surface-variant">Đang tải...</span>
        ) : null}
        {categoryItems.map((item) => {
          const isActive = item.categoryId === activeCategoryId;
          return (
            <button
              key={item.categoryId}
              type="button"
              onClick={() => navigate(buildCommerceCategoryPath(item.categoryId))}
              className={[
                "shrink-0 rounded-xl border px-3.5 py-1.5 text-xs font-bold transition-all shadow-xs cursor-pointer",
                isActive
                  ? "border-primary bg-primary text-on-primary shadow-xs"
                  : "border-outline-variant bg-surface-container-lowest text-on-surface hover:bg-surface-container-low",
              ].join(" ")}
            >
              {item.categoryName}
            </button>
          );
        })}
      </div>
    </nav>
  );
}
