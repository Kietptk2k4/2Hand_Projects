import {
  AdminFilterButton,
  AdminFilterField,
  AdminFilterInput,
  AdminFilterSelect,
  AdminSurfaceCard,
} from "../../components/ui";

export function CatalogFilterBar({
  query,
  activeFilter,
  canWrite,
  addLabel,
  searchPlaceholder = "Tên hoặc slug",
  onQueryChange,
  onActiveFilterChange,
  onFilter,
  onAdd,
}) {
  return (
    <AdminSurfaceCard padding="md">
      <div className="flex flex-col gap-4 sm:flex-row sm:flex-wrap sm:items-end">
        <AdminFilterField label="Tìm kiếm" htmlFor="catalog-search" className="min-w-0 flex-1 sm:min-w-[200px]">
          <AdminFilterInput
            id="catalog-search"
            value={query}
            onChange={(e) => onQueryChange(e.target.value)}
            placeholder={searchPlaceholder}
          />
        </AdminFilterField>
        <AdminFilterField label="Trạng thái" htmlFor="catalog-status">
          <AdminFilterSelect
            id="catalog-status"
            value={activeFilter}
            onChange={(e) => onActiveFilterChange(e.target.value)}
          >
            <option value="">Tất cả</option>
            <option value="true">Đang hoạt động</option>
            <option value="false">Đã vô hiệu</option>
          </AdminFilterSelect>
        </AdminFilterField>
        <div className="flex w-full flex-col gap-2 sm:w-auto sm:flex-row">
          <AdminFilterButton type="button" variant="secondary" className="w-full sm:w-auto" onClick={onFilter}>
            Lọc
          </AdminFilterButton>
          {canWrite ? (
            <AdminFilterButton type="button" variant="primary" className="w-full sm:w-auto" onClick={onAdd}>
              {addLabel}
            </AdminFilterButton>
          ) : null}
        </div>
      </div>
    </AdminSurfaceCard>
  );
}
