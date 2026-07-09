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

function CategoryMobileCard({ item, canWrite, actionId, onEdit, onDeactivate, onActivate }) {
  return (
    <AdminMobileCard ariaLabel={`Danh mục ${item.name}`}>
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0 flex-1" style={{ paddingLeft: `${item.level * 12}px` }}>
          <p className="truncate font-medium text-admin-text" title={item.name}>
            {item.name}
          </p>
          <p className="mt-0.5 font-mono text-xs text-admin-text-muted">{item.slug}</p>
        </div>
        <CatalogStatusBadge active={item.active} />
      </div>
      <dl className="mt-3 grid grid-cols-2 gap-2 text-sm">
        <div>
          <dt className="text-admin-text-muted">Cấp</dt>
          <dd className="text-admin-text">{item.level}</dd>
        </div>
        <div>
          <dt className="text-admin-text-muted">Sản phẩm</dt>
          <dd className="text-admin-text">{item.productCount}</dd>
        </div>
      </dl>
      <div className="mt-3 border-t border-admin-border-subtle pt-3">
        <CatalogItemActions
          canWrite={canWrite}
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

export function CategoryTable({
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
          <CategoryMobileCard
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

      <AdminDataTable minWidth="720px" ariaLabel="Danh sách danh mục">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Tên</AdminDataTableCell>
            <AdminDataTableCell header>Slug</AdminDataTableCell>
            <AdminDataTableCell header>Cấp</AdminDataTableCell>
            <AdminDataTableCell header>Sản phẩm</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            {canWrite ? <AdminDataTableCell header>Thao tác</AdminDataTableCell> : null}
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => (
            <AdminDataTableRow key={item.id}>
              <AdminDataTableCell>
                <span
                  className="block truncate"
                  style={{ paddingLeft: `${item.level * 16}px` }}
                  title={item.name}
                >
                  {item.name}
                </span>
              </AdminDataTableCell>
              <AdminDataTableCell className="font-mono text-xs text-admin-text-muted">
                {item.slug}
              </AdminDataTableCell>
              <AdminDataTableCell>{item.level}</AdminDataTableCell>
              <AdminDataTableCell>{item.productCount}</AdminDataTableCell>
              <AdminDataTableCell>
                <CatalogStatusBadge active={item.active} />
              </AdminDataTableCell>
              {canWrite ? (
                <AdminDataTableCell>
                  <CatalogItemActions
                    canWrite={canWrite}
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
          ))}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
