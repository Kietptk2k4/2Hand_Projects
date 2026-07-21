import { AdminEmptyPanel } from "./AdminEmptyPanel.jsx";
import { AdminErrorPanel } from "./AdminErrorPanel.jsx";
import { AdminForbiddenPanel } from "./AdminForbiddenPanel.jsx";
import { AdminListSkeleton } from "./AdminListSkeleton.jsx";
import { AdminPageHeader } from "./AdminPageHeader.jsx";
import { AdminSurfaceCard } from "./AdminSurfaceCard.jsx";

/**
 * Admin list page contract.
 *
 * Slots: headerActions, toolbar, metrics, children (ready data), footer.
 * States: loading | forbidden | error | empty | ready | idle.
 */
export function AdminListPageShell({
  eyebrow,
  title,
  subtitle,
  headerActions,
  toolbar,
  metrics,
  status = "idle",
  forbiddenMessage,
  forbiddenAction,
  errorMessage,
  errorCode,
  emptyMessage,
  emptyHint,
  emptyIcon,
  emptyAction,
  onRetry,
  skeletonRows = 6,
  skeleton,
  footer,
  children,
  className = "",
}) {
  const showToolbar = Boolean(toolbar);
  const showMetrics = Boolean(metrics);
  const showFooter = Boolean(footer);

  return (
    <div className={["max-w-full min-w-0 space-y-6", className].filter(Boolean).join(" ")}>
      <AdminPageHeader
        eyebrow={eyebrow}
        title={title}
        subtitle={subtitle}
        actions={headerActions}
      />

      {showMetrics ? metrics : null}

      {showToolbar ? (
        <AdminSurfaceCard padding="md">{toolbar}</AdminSurfaceCard>
      ) : null}

      {status === "loading" ? (
        <AdminSurfaceCard padding="md">
          {skeleton ?? <AdminListSkeleton rows={skeletonRows} />}
        </AdminSurfaceCard>
      ) : null}

      {status === "forbidden" ? (
        <AdminForbiddenPanel
          message={forbiddenMessage || "Bạn không có quyền truy cập."}
          action={forbiddenAction}
        />
      ) : null}

      {status === "error" ? (
        <AdminErrorPanel
          message={errorMessage || "Không tải được dữ liệu."}
          errorCode={errorCode}
          onRetry={onRetry}
        />
      ) : null}

      {status === "empty" ? (
        <AdminEmptyPanel
          message={emptyMessage || "Không có dữ liệu."}
          hint={emptyHint}
          icon={emptyIcon}
          action={emptyAction}
        />
      ) : null}

      {status === "ready" ? (
        <AdminSurfaceCard padding="md">
          {children}
          {showFooter ? (
            <footer className="mt-4 border-t border-admin-border-subtle pt-4 text-sm text-admin-text-secondary">
              {footer}
            </footer>
          ) : null}
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}
