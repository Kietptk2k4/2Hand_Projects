import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { AdminFilterButton } from "../../components/ui";
import { countActiveChildren, getCategoryBreadcrumb } from "../utils/categoryHelpers.js";
import { CatalogStatusBadge } from "./CatalogStatusBadge.jsx";

export function CategoryDetailDrawer({
  open,
  item,
  categoryIndex,
  allItems,
  canWrite,
  actionId,
  onClose,
  onEdit,
  onDeactivate,
  onActivate,
}) {
  if (!open || !item) return null;

  const breadcrumb = getCategoryBreadcrumb(item, categoryIndex);
  const parent = item.parentId ? categoryIndex.get(item.parentId) : null;
  const activeChildren = countActiveChildren(item.id, allItems);

  return (
    <div className="fixed inset-0 z-[100] flex min-h-dvh justify-end bg-admin-text/40 backdrop-blur-sm">
      <button type="button" aria-label="Đóng" className="absolute inset-0" onClick={onClose} />
      <aside className="relative flex h-full min-h-dvh w-full max-w-lg flex-col border-l border-admin-border bg-admin-surface shadow-[var(--shadow-admin-surface)]">
        <div className="flex items-start justify-between gap-3 border-b border-admin-border bg-admin-surface-muted px-4 py-4 sm:px-6">
          <div className="min-w-0">
            <p className="text-xs font-medium tracking-wide text-admin-text-muted uppercase">
              Chi tiết danh mục
            </p>
            <h2 className="mt-1 text-lg font-semibold text-admin-text">{item.name}</h2>
            <div className="mt-2">
              <CatalogStatusBadge active={item.active} />
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="inline-flex h-10 w-10 items-center justify-center rounded-lg border border-admin-border text-admin-text-secondary transition-colors hover:bg-admin-surface focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
            aria-label="Đóng panel"
          >
            <span className="material-symbols-outlined text-xl" aria-hidden="true">
              close
            </span>
          </button>
        </div>

        <div className="flex-1 space-y-5 overflow-y-auto px-4 py-4 sm:px-6">
          <section>
            <h3 className="text-sm font-semibold text-admin-text">Slug</h3>
            <p className="mt-1 font-mono text-sm text-admin-text-secondary">{item.slug}</p>
          </section>

          {breadcrumb ? (
            <section>
              <h3 className="text-sm font-semibold text-admin-text">Đường dẫn</h3>
              <p className="mt-1 text-sm text-admin-text-secondary">{breadcrumb}</p>
            </section>
          ) : null}

          <section>
            <h3 className="text-sm font-semibold text-admin-text">Danh mục cha</h3>
            <p className="mt-1 text-sm text-admin-text-secondary">{parent?.name || "— (gốc)"}</p>
          </section>

          <section className="grid grid-cols-2 gap-3">
            <div>
              <h3 className="text-sm font-semibold text-admin-text">Cấp</h3>
              <p className="mt-1 tabular-nums text-admin-text-secondary">{item.level}</p>
            </div>
            <div>
              <h3 className="text-sm font-semibold text-admin-text">Sản phẩm</h3>
              <p className="mt-1 tabular-nums text-admin-text-secondary">{item.productCount}</p>
            </div>
          </section>

          {activeChildren > 0 ? (
            <div className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-900">
              Có {activeChildren} danh mục con đang hoạt động.
            </div>
          ) : null}

          <section>
            <h3 className="text-sm font-semibold text-admin-text">Path</h3>
            <p className="mt-1 break-all font-mono text-xs text-admin-text-muted">{item.path || "—"}</p>
          </section>

          <section className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div>
              <h3 className="text-sm font-semibold text-admin-text">Tạo lúc</h3>
              <p className="mt-1 text-sm text-admin-text-secondary">
                {item.createdAt ? formatDateTime(item.createdAt) : "—"}
              </p>
            </div>
            <div>
              <h3 className="text-sm font-semibold text-admin-text">Cập nhật</h3>
              <p className="mt-1 text-sm text-admin-text-secondary">
                {item.updatedAt ? formatDateTime(item.updatedAt) : "—"}
              </p>
            </div>
          </section>
        </div>

        {canWrite ? (
          <div className="flex flex-col gap-2 border-t border-admin-border bg-admin-surface-muted px-4 py-4 sm:flex-row sm:justify-end sm:px-6">
            {item.active ? (
              <AdminFilterButton
                type="button"
                variant="secondary"
                className="border-admin-danger/30 text-admin-danger hover:bg-admin-danger-soft"
                disabled={actionId === item.id}
                onClick={() => onDeactivate?.(item)}
              >
                Vô hiệu hóa
              </AdminFilterButton>
            ) : (
              <AdminFilterButton
                type="button"
                variant="primary"
                disabled={actionId === item.id}
                onClick={() => onActivate?.(item.id)}
              >
                Kích hoạt
              </AdminFilterButton>
            )}
            <AdminFilterButton type="button" variant="primary" onClick={() => onEdit?.(item)}>
              Sửa danh mục
            </AdminFilterButton>
          </div>
        ) : null}
      </aside>
    </div>
  );
}
