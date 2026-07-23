import { useCallback, useState } from "react";
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
import { isProtectedBrand } from "../utils/brandHelpers.js";
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

function ProtectedBadge() {
  return (
    <span className="ml-2 rounded-md bg-amber-100 px-1.5 py-0.5 text-[10px] font-medium uppercase tracking-wide text-amber-900">
      Protected
    </span>
  );
}

function BrandMobileCard({
  item,
  canWrite,
  actionId,
  onEdit,
  onDeactivate,
  onActivate,
  onOpenDetail,
}) {
  const protectedBrand = isProtectedBrand(item);

  return (
    <AdminMobileCard ariaLabel={`Thương hiệu ${item.name}`}>
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0 flex-1">
          <button
            type="button"
            onClick={() => onOpenDetail?.(item)}
            className="truncate text-left font-medium text-admin-text"
          >
            {item.name}
            {protectedBrand ? <ProtectedBadge /> : null}
          </button>
          <p className="mt-0.5 font-mono text-xs text-admin-text-muted">{item.slug}</p>
        </div>
        <CatalogStatusBadge active={item.active} />
      </div>
      <p className="mt-2 text-sm tabular-nums text-admin-text-secondary">
        {item.productCount} sản phẩm
      </p>
      <div className="mt-3 flex flex-wrap justify-end gap-2 border-t border-admin-border-subtle pt-3">
        <AdminFilterButton
          type="button"
          variant="secondary"
          className="min-h-11 text-xs"
          onClick={() => onOpenDetail?.(item)}
        >
          Chi tiết
        </AdminFilterButton>
        <CatalogItemActions
          canWrite={canWrite}
          isProtected={protectedBrand}
          active={item.active}
          actionId={actionId}
          itemId={item.id}
          onEdit={() => onEdit(item)}
          onDeactivate={() => onDeactivate(item)}
          onActivate={() => onActivate(item.id)}
        />
      </div>
    </AdminMobileCard>
  );
}

export function BrandTable({
  items,
  canWrite,
  actionId,
  emptyMessage,
  onEdit,
  onDeactivate,
  onActivate,
  onOpenDetail,
}) {
  if (!items?.length) {
    return (
      <div className="py-12 text-center">
        <p className="text-sm text-admin-text-muted">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <>
      <AdminMobileCardList className="mb-0 p-4 md:hidden">
        {items.map((item) => (
          <BrandMobileCard
            key={item.id}
            item={item}
            canWrite={canWrite}
            actionId={actionId}
            onEdit={onEdit}
            onDeactivate={onDeactivate}
            onActivate={onActivate}
            onOpenDetail={onOpenDetail}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="720px" ariaLabel="Danh sách thương hiệu">
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
          {items.map((item) => {
            const protectedBrand = isProtectedBrand(item);
            return (
              <AdminDataTableRow
                key={item.id}
                className="transition-colors hover:bg-admin-surface-muted/40"
              >
                <AdminDataTableCell className="py-3">
                  <button
                    type="button"
                    onClick={() => onOpenDetail?.(item)}
                    className="inline-flex max-w-full items-center truncate text-left font-medium text-admin-text hover:text-admin-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-admin-accent-soft"
                    title={item.name}
                  >
                    {item.name}
                    {protectedBrand ? <ProtectedBadge /> : null}
                  </button>
                </AdminDataTableCell>
                <AdminDataTableCell className="py-3">
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs text-admin-text-muted">{item.slug}</span>
                    <CopySlugButton slug={item.slug} />
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell className="py-3 text-right tabular-nums">
                  {item.productCount}
                </AdminDataTableCell>
                <AdminDataTableCell className="py-3">
                  <CatalogStatusBadge active={item.active} />
                </AdminDataTableCell>
                {canWrite ? (
                  <AdminDataTableCell className="py-3">
                    <div className="flex flex-wrap items-center justify-end gap-2">
                      <CatalogItemActions
                        canWrite={canWrite}
                        isProtected={protectedBrand}
                        active={item.active}
                        actionId={actionId}
                        itemId={item.id}
                        onEdit={() => onEdit(item)}
                        onDeactivate={() => onDeactivate(item)}
                        onActivate={() => onActivate(item.id)}
                      />
                      <AdminFilterButton
                        type="button"
                        variant="secondary"
                        className="min-h-11 px-2.5 py-1 text-xs"
                        onClick={() => onOpenDetail?.(item)}
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
