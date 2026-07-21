import { AdminErrorPanel } from "./AdminErrorPanel.jsx";
import { AdminListSkeleton } from "./AdminListSkeleton.jsx";
import { AdminPageHeader } from "./AdminPageHeader.jsx";

/**
 * Admin workspace page contract (list + detail panel).
 *
 * Slots: alert, sidebar (primary list), children (detail panel), footer, modals.
 *
 * layout:
 * - "list-aside" (default): wide list left, sticky narrow panel right (assign / revoke / user perms)
 * - "master-detail": sticky narrow master left, wide detail right (role permissions editor)
 *
 * asideSize (list-aside only):
 * - "default": ~260–300px aside
 * - "comfortable": ~300–380px aside (permission inspector)
 */
export function AdminWorkspacePageShell({
  eyebrow,
  title,
  subtitle,
  headerActions,
  alert,
  status = "ready",
  errorMessage,
  onRetry,
  skeletonRows = 5,
  sidebar,
  children,
  footer,
  modals,
  stickyAside = true,
  layout = "list-aside",
  asideSize = "default",
  className = "",
}) {
  const isMasterDetail = layout === "master-detail";
  const isComfortableAside = !isMasterDetail && asideSize === "comfortable";
  const gridClass = isMasterDetail
    ? "grid gap-4 lg:grid-cols-[minmax(240px,280px)_minmax(0,1fr)] xl:grid-cols-[minmax(260px,300px)_minmax(0,1fr)]"
    : isComfortableAside
      ? "grid gap-4 lg:grid-cols-[minmax(0,1fr)_minmax(300px,340px)] xl:grid-cols-[minmax(0,1fr)_minmax(320px,380px)]"
      : "grid gap-4 lg:grid-cols-[minmax(0,1fr)_minmax(260px,280px)] xl:grid-cols-[minmax(0,1fr)_minmax(280px,300px)]";
  const stickyClass = stickyAside ? "lg:sticky lg:top-4 lg:self-start" : "";

  if (status === "loading") {
    return (
      <div className={["max-w-full min-w-0 space-y-6", className].filter(Boolean).join(" ")}>
        <AdminPageHeader
          eyebrow={eyebrow}
          title={title}
          subtitle={subtitle}
          actions={headerActions}
        />
        <AdminListSkeleton rows={skeletonRows} />
      </div>
    );
  }

  if (status === "error") {
    return (
      <div className={["max-w-full min-w-0 space-y-6", className].filter(Boolean).join(" ")}>
        <AdminPageHeader
          eyebrow={eyebrow}
          title={title}
          subtitle={subtitle}
          actions={headerActions}
        />
        <AdminErrorPanel message={errorMessage || "Không tải được dữ liệu."} onRetry={onRetry} />
      </div>
    );
  }

  return (
    <div className={["max-w-full min-w-0 space-y-6", className].filter(Boolean).join(" ")}>
      <AdminPageHeader
        eyebrow={eyebrow}
        title={title}
        subtitle={subtitle}
        actions={headerActions}
      />

      {alert}

      <div className={gridClass}>
        <div className={["min-w-0", isMasterDetail ? stickyClass : ""].filter(Boolean).join(" ")}>
          {sidebar}
        </div>
        <div className={["min-w-0", !isMasterDetail ? stickyClass : ""].filter(Boolean).join(" ")}>
          {children}
        </div>
      </div>

      {footer ? (
        <footer className="text-sm text-admin-text-secondary">{footer}</footer>
      ) : null}

      {modals}
    </div>
  );
}
