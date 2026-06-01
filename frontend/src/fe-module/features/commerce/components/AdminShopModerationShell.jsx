import { Link, useLocation } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

const NAV_ITEMS = [
  { id: "dashboard", label: "Dashboard", icon: "dashboard", href: APP_ROUTES.admin, disabled: true },
  {
    id: "shops",
    label: "Shop Moderation",
    icon: "storefront",
    href: APP_ROUTES.commerceAdminShopModeration,
    active: true,
  },
  { id: "disputes", label: "Disputes", icon: "gavel", disabled: true },
  { id: "users", label: "User Management", icon: "group", disabled: true },
  { id: "settings", label: "Settings", icon: "settings", disabled: true },
];

export function AdminShopModerationShell({
  children,
  headerSearch,
  onHeaderSearchChange,
  onHeaderSearchSubmit,
}) {
  const location = useLocation();

  return (
    <div className="flex min-h-screen bg-surface text-on-surface">
      <nav
        aria-label="Admin sidebar"
        className="fixed left-0 top-0 z-40 hidden h-screen w-64 flex-col border-r border-outline-variant bg-surface-container-low py-6 md:flex"
      >
        <div className="mb-8 px-6">
          <h1 className="text-headline-md font-bold text-primary">2Hands Admin</h1>
          <p className="mt-1 text-body-sm text-on-surface-variant">Marketplace Moderator</p>
        </div>

        <ul className="flex flex-1 flex-col gap-1 px-3">
          {NAV_ITEMS.map((item) => {
            const isActive =
              item.href &&
              (location.pathname === item.href || location.pathname.startsWith(`${item.href}/`));

            if (item.disabled) {
              return (
                <li key={item.id}>
                  <span
                    className="flex cursor-not-allowed items-center gap-3 rounded-lg px-4 py-3 text-label-md text-on-surface-variant/60"
                    title="Sắp có"
                  >
                    <span className="material-symbols-outlined" aria-hidden="true">
                      {item.icon}
                    </span>
                    {item.label}
                  </span>
                </li>
              );
            }

            return (
              <li key={item.id}>
                <Link
                  to={item.href}
                  className={[
                    "flex items-center gap-3 rounded-lg px-4 py-3 text-label-md transition-colors",
                    isActive
                      ? "border-r-4 border-primary bg-primary/10 font-semibold text-primary"
                      : "text-on-surface-variant hover:bg-surface-container-highest",
                  ].join(" ")}
                >
                  <span
                    className="material-symbols-outlined"
                    style={isActive ? { fontVariationSettings: "'FILL' 1" } : undefined}
                    aria-hidden="true"
                  >
                    {item.icon}
                  </span>
                  {item.label}
                </Link>
              </li>
            );
          })}
        </ul>

        <div className="px-6 pb-4">
          <div className="inline-flex items-center gap-2 rounded-full bg-surface-container-highest px-3 py-1">
            <span className="h-2 w-2 rounded-full bg-emerald-500" aria-hidden="true" />
            <span className="text-label-sm text-on-surface-variant">Hệ thống: ổn định</span>
          </div>
        </div>
      </nav>

      <div className="flex min-h-screen flex-1 flex-col md:ml-64">
        <header className="sticky top-0 z-30 hidden h-16 items-center justify-between border-b border-outline-variant bg-surface px-8 md:flex">
          <div className="flex max-w-2xl flex-1 items-center gap-6">
            <div className="relative w-full max-w-md">
              <span
                className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant"
                aria-hidden="true"
              >
                search
              </span>
              <input
                type="search"
                value={headerSearch ?? ""}
                onChange={(e) => onHeaderSearchChange?.(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") onHeaderSearchSubmit?.();
                }}
                placeholder="Tìm shop, seller..."
                className="w-full rounded-lg border border-outline-variant bg-surface-container-lowest py-2 pl-10 pr-4 text-body-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
              />
            </div>
          </div>
          <Link
            to={APP_ROUTES.admin}
            className="text-label-md text-primary hover:underline"
          >
            Quay lại Admin
          </Link>
        </header>

        <main className="flex-1 bg-surface-container-low p-4 md:p-8">{children}</main>
      </div>
    </div>
  );
}
