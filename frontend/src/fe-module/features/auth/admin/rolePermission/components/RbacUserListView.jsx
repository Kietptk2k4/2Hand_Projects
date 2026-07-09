import { AdminPagination, AdminSurfaceCard } from "../../components/ui";
import { RbacUserFilterBar } from "./RbacUserFilterBar.jsx";
import { RbacUserTable } from "./RbacUserTable.jsx";
import { RbacListSkeleton } from "./ui/RbacListSkeleton.jsx";
import { RbacRetryPanel } from "./ui/RbacRetryPanel.jsx";

export function RbacUserListView({
  status,
  errorMessage,
  draftFilters,
  statusOptions,
  sortOptions,
  items,
  pagination,
  currentPage,
  totalPages,
  activeSortLabel,
  selectedUserId,
  onDraftChange,
  onApplyFilters,
  onClearFilters,
  onPageChange,
  onUserSelect,
  onRetry,
}) {
  return (
    <div className="space-y-4">
      <AdminSurfaceCard padding="md">
        <RbacUserFilterBar
          draftFilters={draftFilters}
          statusOptions={statusOptions}
          sortOptions={sortOptions}
          onDraftChange={onDraftChange}
          onApply={onApplyFilters}
          onClear={onClearFilters}
        />
      </AdminSurfaceCard>

      {status === "loading" ? <RbacListSkeleton rows={5} /> : null}
      {status === "forbidden" ? <RbacRetryPanel message={errorMessage} /> : null}
      {status === "error" ? <RbacRetryPanel message={errorMessage} onRetry={onRetry} /> : null}

      {status === "ready" ? (
        <AdminSurfaceCard padding="md">
          <AdminPagination
            className="mb-4"
            currentPage={currentPage}
            totalPages={totalPages}
            summary={`${pagination?.total_items ?? 0} người dùng · Sắp xếp: ${activeSortLabel} · Trang ${pagination?.page ?? currentPage}/${totalPages}`}
            onPrevious={() => onPageChange(currentPage - 1)}
            onNext={() => onPageChange(currentPage + 1)}
          />
          <RbacUserTable
            items={items}
            selectedUserId={selectedUserId}
            onUserSelect={onUserSelect}
          />
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}
