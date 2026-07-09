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
    <div className="mb-6 space-y-4">
      <AdminSurfaceCard padding="md">
        <InvestigationUserFilterBar
          draftFilters={draftFilters}
          statusOptions={statusOptions}
          sortOptions={sortOptions}
          onDraftChange={onDraftChange}
          onApply={onApplyFilters}
          onClear={onClearFilters}
        />
      </AdminSurfaceCard>

      {status === "loading" ? <InvestigationListSkeleton rows={5} /> : null}
      {status === "forbidden" ? <InvestigationRetryPanel message={errorMessage} /> : null}

      {status === "error" ? (
        <InvestigationRetryPanel message={errorMessage} onRetry={onRetry} />
      ) : null}

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

          <InvestigationUserTable
            items={items}
            selectedUserId={selectedUserId}
            onUserSelect={onUserSelect}
          />
        </AdminSurfaceCard>
      ) : null}
    </div>
  );
}
