import { useCallback, useState } from "react";
import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminFilterButton,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";
import { flattenVisibleCategoryTree, getCategoryBreadcrumb } from "../utils/categoryHelpers.js";
import { CatalogItemActions } from "./CatalogItemActions.jsx";
import { CatalogStatusBadge } from "./CatalogStatusBadge.jsx";

function CopySlugButton({ slug }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = useCallback(async () => {
    if (!slug) return;
    try {
      await navigator.clipboard.writeText(slug);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  }, [slug]);

  return (
    <button
      type="button"
      onClick={handleCopy}
      aria-label="Copy slug"
      className="inline-flex h-8 w-8 items-center justify-center rounded-md border border-admin-border text-admin-text-secondary transition-colors hover:bg-admin-surface-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
    >
      <span className="material-symbols-outlined text-sm" aria-hidden="true">
        {copied ? "check" : "content_copy"}
      </span>
    </button>
  );
}

function CategoryNameCell({ row, breadcrumb, expandedIds, onToggleExpand, onOpenDetail }) {
  const paddingLeft = row.depth * 20;
  const isExpanded = expandedIds.has(row.id);

  return (
    <div className="flex min-w-0 items-start gap-1" style={{ paddingLeft }}>
      {row.hasChildren ? (
        <button
          type="button"
          onClick={() => onToggleExpand?.(row.id)}
          aria-expanded={isExpanded}
          aria-label={isExpanded ? "Thu gọn" : "Mở rộng"}
          className="mt-0.5 inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-md text-admin-text-secondary transition-colors hover:bg-admin-surface-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
        >
          <span className="material-symbols-outlined text-base" aria-hidden="true">
            {isExpanded ? "expand_more" : "chevron_right"}
          </span>
        </button>
      ) : (
        <span className="inline-block w-7 shrink-0" aria-hidden="true" />
      )}
      <div className="min-w-0">
        <button
          type="button"
          onClick={() => onOpenDetail?.(row)}
          className="truncate text-left font-medium text-admin-text hover:text-admin-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
          title={row.name}
        >
          {row.name}
        </button>
        {breadcrumb ? (
          <p className="mt-0.5 truncate text-xs text-admin-text-muted" title={breadcrumb}>
            {breadcrumb}
          </p>
        ) : null}
      </div>
    </div>
  );
}

function CategoryMobileCard({
  row,
  breadcrumb,
  canWrite,
  actionId,
  onEdit,
  onDeactivate,
  onActivate,
  onOpenDetail,
}) {
  return (
    <AdminMobileCard ariaLabel={`Danh mục ${row.name}`}>
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0 flex-1">
          <button
            type="button"
            onClick={() => onOpenDetail?.(row)}
            className="truncate text-left font-medium text-admin-text"
          >
            {row.name}
          </button>
          {breadcrumb ? <p className="mt-0.5 text-xs text-admin-text-muted">{breadcrumb}</p> : null}
          <p className="mt-1 font-mono text-xs text-admin-text-muted">{row.slug}</p>
        </div>
        <CatalogStatusBadge active={row.active} />
      </div>
      <p className="mt-2 text-sm tabular-nums text-admin-text-secondary">
        {row.productCount} sản phẩm
      </p>
      <div className="mt-3 border-t border-admin-border-subtle pt-3">
        <CatalogItemActions
          canWrite={canWrite}
          active={row.active}
          actionId={actionId}
          itemId={row.id}
          onEdit={() => onEdit(row)}
          onDeactivate={() => onDeactivate(row)}
          onActivate={() => onActivate(row.id)}
        />
      </div>
    </AdminMobileCard>
  );
}

export function CategoryTreeTable({
  tree,
  categoryIndex,
  expandedIds,
  canWrite,
  actionId,
  emptyMessage,
  onToggleExpand,
  onEdit,
  onDeactivate,
  onActivate,
  onOpenDetail,
}) {
  const rows = flattenVisibleCategoryTree(tree, expandedIds);

  if (!rows.length) {
    return (
      <div className="py-12 text-center">
        <p className="text-sm text-admin-text-muted">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <>
      <AdminMobileCardList className="mb-0 p-4 md:hidden">
        {rows.map((row) => (
          <CategoryMobileCard
            key={row.id}
            row={row}
            breadcrumb={getCategoryBreadcrumb(row, categoryIndex)}
            canWrite={canWrite}
            actionId={actionId}
            onEdit={onEdit}
            onDeactivate={onDeactivate}
            onActivate={onActivate}
            onOpenDetail={onOpenDetail}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="760px" ariaLabel="Danh sách danh mục dạng cây">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Tên</AdminDataTableCell>
            <AdminDataTableCell header>Slug</AdminDataTableCell>
            <AdminDataTableCell header className="text-right">
              Sản phẩm
            </AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            {canWrite ? <AdminDataTableCell header>Thao tác</AdminDataTableCell> : null}
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {rows.map((row) => {
            const breadcrumb = getCategoryBreadcrumb(row, categoryIndex);
            return (
              <AdminDataTableRow
                key={row.id}
                className="transition-colors hover:bg-admin-surface-muted/40"
              >
                <AdminDataTableCell className="py-3">
                  <CategoryNameCell
                    row={row}
                    breadcrumb={breadcrumb}
                    expandedIds={expandedIds}
                    onToggleExpand={onToggleExpand}
                    onOpenDetail={onOpenDetail}
                  />
                </AdminDataTableCell>
                <AdminDataTableCell className="py-3">
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs text-admin-text-muted">{row.slug}</span>
                    <CopySlugButton slug={row.slug} />
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell className="py-3 text-right tabular-nums">
                  {row.productCount}
                </AdminDataTableCell>
                <AdminDataTableCell className="py-3">
                  <CatalogStatusBadge active={row.active} />
                </AdminDataTableCell>
                {canWrite ? (
                  <AdminDataTableCell className="py-3">
                    <div className="flex flex-wrap items-center gap-2">
                      <CatalogItemActions
                        canWrite={canWrite}
                        active={row.active}
                        actionId={actionId}
                        itemId={row.id}
                        onEdit={() => onEdit(row)}
                        onDeactivate={() => onDeactivate(row)}
                        onActivate={() => onActivate(row.id)}
                      />
                      <AdminFilterButton
                        type="button"
                        variant="secondary"
                        className="min-h-11 px-2.5 py-1 text-xs"
                        onClick={() => onOpenDetail?.(row)}
                      >
                        Chi tiết
                      </AdminFilterButton>
                    </div>
                  </AdminDataTableCell>
                ) : null}
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
