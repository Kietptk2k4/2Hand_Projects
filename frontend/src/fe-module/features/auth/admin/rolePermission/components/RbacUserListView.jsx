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
    <AdminSurfaceCard padding="none" className="overflow-hidden">
      <div className="border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-3 lg:px-5">
        <RbacUserFilterBar
          draftFilters={draftFilters}
          statusOptions={statusOptions}
          sortOptions={sortOptions}
          onDraftChange={onDraftChange}
          onApply={onApplyFilters}
          onClear={onClearFilters}
        />
      </div>

      {status === "loading" ? (
        <div className="p-4 lg:p-5">
          <RbacListSkeleton rows={5} bare />
        </div>
      ) : null}
      {status === "forbidden" ? (
        <div className="p-4 lg:p-5">
          <RbacRetryPanel message={errorMessage} />
        </div>
      ) : null}
      {status === "error" ? (
        <div className="p-4 lg:p-5">
          <RbacRetryPanel message={errorMessage} onRetry={onRetry} />
        </div>
      ) : null}

      {status === "ready" ? (
        <div className="p-4 lg:p-5">
          <AdminPagination
            className="mb-3"
            currentPage={currentPage}
            totalPages={totalPages}
            summary={`${pagination?.total_items ?? 0} người dùng · ${activeSortLabel}`}
            onPrevious={() => onPageChange(currentPage - 1)}
            onNext={() => onPageChange(currentPage + 1)}
          />
          <RbacUserTable
            items={items}
            selectedUserId={selectedUserId}
            onUserSelect={onUserSelect}
          />
        </div>
      ) : null}
    </AdminSurfaceCard>
  );
}
