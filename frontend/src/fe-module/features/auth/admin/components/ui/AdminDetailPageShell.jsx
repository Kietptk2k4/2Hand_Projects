import { AdminPageHeader } from "./AdminPageHeader.jsx";
import { AdminSurfaceCard } from "./AdminSurfaceCard.jsx";

/**
 * Admin detail / form page contract (single-column with optional toolbar).
 */
export function AdminDetailPageShell({
  eyebrow,
  title,
  subtitle,
  headerActions,
  toolbar,
  children,
  footer,
  className = "",
}) {
  return (
    <div className={["max-w-full min-w-0 space-y-6", className].filter(Boolean).join(" ")}>
      <AdminPageHeader
        eyebrow={eyebrow}
        title={title}
        subtitle={subtitle}
        actions={headerActions}
      />

      {toolbar ? <AdminSurfaceCard padding="md">{toolbar}</AdminSurfaceCard> : null}

      {children}

      {footer ? (
        <footer className="text-sm text-admin-text-secondary">{footer}</footer>
      ) : null}
    </div>
  );
}
