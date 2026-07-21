import { AdminPagination, AdminSurfaceCard } from "../../components/ui";
import { InvestigationListSkeleton } from "./ui/InvestigationListSkeleton.jsx";
import { InvestigationRetryPanel } from "./ui/InvestigationRetryPanel.jsx";
import { InvestigationUserFilterBar } from "./InvestigationUserFilterBar.jsx";
import { InvestigationUserTable } from "./InvestigationUserTable.jsx";

export function InvestigationUserListView({
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
    <AdminSurfaceCard padding="none" className="mb-4 overflow-hidden">
      <div className="border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-3 lg:px-5">
        <InvestigationUserFilterBar
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
          <InvestigationListSkeleton rows={5} bare />
        </div>
      ) : null}
      {status === "forbidden" ? (
        <div className="p-4 lg:p-5">
          <InvestigationRetryPanel message={errorMessage} />
        </div>
      ) : null}
      {status === "error" ? (
        <div className="p-4 lg:p-5">
          <InvestigationRetryPanel message={errorMessage} onRetry={onRetry} />
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

          <InvestigationUserTable
            items={items}
            selectedUserId={selectedUserId}
            onUserSelect={onUserSelect}
          />
        </div>
      ) : null}
    </AdminSurfaceCard>
  );
}
