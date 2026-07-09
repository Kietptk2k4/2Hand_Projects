import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
  AdminSurfaceCard,
} from "../../components/ui";
import { CatalogItemActions } from "./CatalogItemActions.jsx";
import { CatalogStatusBadge } from "./CatalogStatusBadge.jsx";

const PROTECTED_SLUG = "khac";

function BrandMobileCard({ item, canWrite, actionId, onEdit, onDeactivate, onActivate }) {
  const isProtected = item.slug === PROTECTED_SLUG;

  return (
    <AdminMobileCard ariaLabel={`Thương hiệu ${item.name}`}>
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0 flex-1">
          <p className="truncate font-medium text-admin-text" title={item.name}>
            {item.name}
          </p>
          <p className="mt-0.5 font-mono text-xs text-admin-text-muted">{item.slug}</p>
        </div>
        <CatalogStatusBadge active={item.active} />
      </div>
      <p className="mt-2 text-sm text-admin-text-secondary">
        {item.productCount} sản phẩm
      </p>
      <div className="mt-3 border-t border-admin-border-subtle pt-3">
        <CatalogItemActions
          canWrite={canWrite}
          isProtected={isProtected}
          active={item.active}
          actionId={actionId}
          itemId={item.id}
          onEdit={() => onEdit(item)}
          onDeactivate={() => onDeactivate(item.id)}
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
}) {
  if (!items?.length) {
    return (
      <AdminSurfaceCard padding="lg" className="text-center">
        <p className="text-sm text-admin-text-muted">{emptyMessage}</p>
      </AdminSurfaceCard>
    );
  }

  return (
    <>
      <AdminMobileCardList className="mb-0 md:hidden">
        {items.map((item) => (
          <BrandMobileCard
            key={item.id}
            item={item}
            canWrite={canWrite}
            actionId={actionId}
            onEdit={onEdit}
            onDeactivate={onDeactivate}
            onActivate={onActivate}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="640px" ariaLabel="Danh sách thương hiệu">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Tên</AdminDataTableCell>
            <AdminDataTableCell header>Slug</AdminDataTableCell>
            <AdminDataTableCell header>Sản phẩm</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            {canWrite ? <AdminDataTableCell header>Thao tác</AdminDataTableCell> : null}
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => {
            const isProtected = item.slug === PROTECTED_SLUG;
            return (
              <AdminDataTableRow key={item.id}>
                <AdminDataTableCell>
                  <span className="block truncate" title={item.name}>
                    {item.name}
                  </span>
                </AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs text-admin-text-muted">
                  {item.slug}
                </AdminDataTableCell>
                <AdminDataTableCell>{item.productCount}</AdminDataTableCell>
                <AdminDataTableCell>
                  <CatalogStatusBadge active={item.active} />
                </AdminDataTableCell>
                {canWrite ? (
                  <AdminDataTableCell>
                    <CatalogItemActions
                      canWrite={canWrite}
                      isProtected={isProtected}
                      active={item.active}
                      actionId={actionId}
                      itemId={item.id}
                      onEdit={() => onEdit(item)}
                      onDeactivate={() => onDeactivate(item.id)}
                      onActivate={() => onActivate(item.id)}
                    />
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
