import { Link, useNavigate } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";
import { SIDEBAR_CATEGORY_ITEMS } from "../constants/categoryProductsConstants";
import { buildCommerceCategoryPath } from "../utils/commerceRoutes";

function CategoryBreadcrumb({ categoryName }) {
  return (
    <nav
      className="mb-4 flex flex-wrap items-center text-body-sm text-on-surface-variant"
      aria-label="Breadcrumb"
    >
      <Link to={APP_ROUTES.commerceHome} className="transition-colors hover:text-primary">
        Trang chủ
      </Link>
      <span className="material-symbols-outlined mx-1 text-[16px]" aria-hidden="true">
        chevron_right
      </span>
      <span className="font-medium text-on-surface">{categoryName || "Danh mục"}</span>
    </nav>
  );
}

export function CategoryProductsSidebar({
  activeCategoryId,
  categoryName,
  includeChildren,
  onIncludeChildrenChange,
}) {
  const navigate = useNavigate();

  return (
    <aside className="hidden lg:col-span-3 lg:block">
      <div className="sticky top-16 z-20 max-h-[calc(100vh-4rem)] space-y-6 overflow-y-auto overscroll-contain pr-1">
      <CategoryBreadcrumb categoryName={categoryName} />

      <div className="rounded-lg border border-outline-variant bg-surface p-4">
        <h3 className="mb-3 text-headline-sm font-semibold text-on-surface">Danh mục</h3>
        <ul className="space-y-1">
          {SIDEBAR_CATEGORY_ITEMS.map((item) => {
            const isActive = item.categoryId === activeCategoryId;
            return (
              <li key={item.categoryId}>
                <button
                  type="button"
                  onClick={() => navigate(buildCommerceCategoryPath(item.categoryId))}
                  className={[
                    "flex w-full items-center justify-between rounded-md py-2 text-left text-body-md transition-colors",
                    item.parentId ? "pl-4" : "",
                    isActive
                      ? "font-medium text-primary"
                      : "text-on-surface-variant hover:text-primary",
                  ].join(" ")}
                >
                  <span>{item.categoryName}</span>
                  <span
                    className={[
                      "rounded-full px-2 py-0.5 text-label-sm",
                      isActive
                        ? "bg-surface-container-low text-on-surface-variant"
                        : "bg-surface-container-low text-outline",
                    ].join(" ")}
                  >
                    {item.productCount}
                  </span>
                </button>
              </li>
            );
          })}
        </ul>
      </div>

      <div className="rounded-lg border border-outline-variant bg-surface p-4">
        <h3 className="mb-3 text-headline-sm font-semibold text-on-surface">Lọc danh mục</h3>
        <label className="flex cursor-pointer items-start gap-3">
          <input
            type="checkbox"
            checked={includeChildren}
            onChange={(event) => onIncludeChildrenChange(event.target.checked)}
            className="mt-0.5 h-4 w-4 rounded border-outline text-primary focus:ring-primary"
          />
          <span className="text-body-sm text-on-surface-variant">Bao gồm danh mục con</span>
        </label>
      </div>
      </div>
    </aside>
  );
}

export function CategoryProductsMobileNav({ activeCategoryId }) {
  const navigate = useNavigate();

  return (
    <nav className="mb-6 lg:hidden" aria-label="Danh mục nhanh">
      <div className="mb-3 flex flex-wrap items-center text-body-sm text-on-surface-variant">
        <Link to={APP_ROUTES.commerceHome} className="hover:text-primary">
          Trang chủ
        </Link>
        <span className="material-symbols-outlined mx-1 text-[16px]" aria-hidden="true">
          chevron_right
        </span>
        <span className="font-medium text-on-surface">Danh mục</span>
      </div>
      <div className="flex gap-2 overflow-x-auto pb-1">
        {SIDEBAR_CATEGORY_ITEMS.map((item) => {
          const isActive = item.categoryId === activeCategoryId;
          return (
            <button
              key={item.categoryId}
              type="button"
              onClick={() => navigate(buildCommerceCategoryPath(item.categoryId))}
              className={[
                "shrink-0 rounded-full border px-3 py-1.5 text-label-md transition-colors",
                isActive
                  ? "border-primary bg-primary-container text-on-primary-container"
                  : "border-outline-variant bg-surface-container-lowest text-on-surface",
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
