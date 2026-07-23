import { ANNOUNCEMENT_PAGE_SIZE_OPTIONS } from "../constants/announcementListConstants.js";
import {
  AdminFilterButton,
  AdminFilterSelect,
  AdminPageHeader,
  AdminPagination,
  AdminSurfaceCard,
} from "../../components/ui";
import { ErrorState } from "../../../../../shared/ui/PageState.jsx";
import {
  SYSTEM_ANNOUNCEMENTS_FORBIDDEN,
  SYSTEM_ANNOUNCEMENTS_SUBTITLE,
  SYSTEM_ANNOUNCEMENTS_TITLE,
} from "../constants/systemOperationsUiStrings.js";
import { SystemAnnouncementFilterBar } from "./SystemAnnouncementFilterBar.jsx";
import { SystemAnnouncementStatsBar } from "./SystemAnnouncementStatsBar.jsx";
import { SystemAnnouncementTable } from "./SystemAnnouncementTable.jsx";
import { SystemOperationsForbiddenState } from "./SystemOperationsForbiddenState.jsx";

function ListSkeleton() {
  return (
    <div className="space-y-3 p-4 lg:p-5">
      {Array.from({ length: 6 }, (_, index) => (
        <div key={index} className="h-14 animate-pulse rounded-lg bg-admin-surface-muted" />
      ))}
    </div>
  );
}

function PageSizeSelect({ value, onChange, disabled }) {
  return (
    <label className="inline-flex min-h-11 items-center gap-2 text-sm text-admin-text-secondary">
      <span className="hidden sm:inline">Hiển thị</span>
      <AdminFilterSelect
        value={value}
        disabled={disabled}
        onChange={(event) => onChange?.(event.target.value)}
        className="min-h-11 w-auto min-w-[7.5rem] py-1.5"
        aria-label="Số bản ghi mỗi trang"
      >
        {ANNOUNCEMENT_PAGE_SIZE_OPTIONS.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </AdminFilterSelect>
    </label>
  );
}

function AnnouncementEmptyState({ canCreate, onCreateClick }) {
  return (
    <div className="rounded-xl border border-dashed border-admin-border px-6 py-12 text-center">
      <span
        className="material-symbols-outlined mx-auto text-[40px] text-admin-text-muted"
        aria-hidden="true"
      >
        campaign
      </span>
      <p className="mt-4 text-sm font-medium text-admin-text">Chưa có thông báo nào</p>
      <p className="mt-2 text-sm text-admin-text-secondary">
        Tạo thông báo bảo trì hoặc phát hành phiên bản mới cho toàn hệ thống.
      </p>
      {canCreate ? (
        <AdminFilterButton
          type="button"
          variant="primary"
          className="mt-6 min-h-11"
          onClick={onCreateClick}
        >
          Tạo thông báo đầu tiên
        </AdminFilterButton>
      ) : null}
    </div>
  );
}

export function SystemAnnouncementListView({
  canViewAnnouncements,
  canCreateAnnouncements,
  status,
  errorMessage,
  appliedFilters,
  draftFilters,
  onDraftFiltersChange,
  onApplyFilters,
  onClearFilters,
  onQuickFilter,
  onRemoveFilterChip,
  onRetry,
  stats,
  statsStatus,
  onStatPresetClick,
  items,
  result,
  currentPage,
  totalPages,
  pageSize,
  selectedAnnouncementId,
  onRowSelect,
  onPageChange,
  onPageSizeChange,
  onCreateClick,
  drawer,
  createDrawer,
}) {
  if (!canViewAnnouncements) {
    return (
      <div className="mb-6 max-w-full min-w-0 space-y-6">
        <AdminPageHeader
          eyebrow="Vận hành hệ thống"
          title={SYSTEM_ANNOUNCEMENTS_TITLE}
          subtitle={SYSTEM_ANNOUNCEMENTS_SUBTITLE}
        />
        <SystemOperationsForbiddenState message={SYSTEM_ANNOUNCEMENTS_FORBIDDEN} />
      </div>
    );
  }

  const summary =
    status === "ready"
      ? `${result?.totalElements ?? 0} thông báo · Trang ${currentPage}/${Math.max(totalPages, 1)}`
      : "";

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Vận hành hệ thống"
        title={SYSTEM_ANNOUNCEMENTS_TITLE}
        subtitle={SYSTEM_ANNOUNCEMENTS_SUBTITLE}
        actions={
          canCreateAnnouncements ? (
            <AdminFilterButton type="button" variant="primary" className="min-h-11" onClick={onCreateClick}>
              Tạo thông báo
            </AdminFilterButton>
          ) : null
        }
      />

      <SystemAnnouncementStatsBar stats={stats} status={statsStatus} onPresetClick={onStatPresetClick} />

      <AdminSurfaceCard padding="none" className="overflow-hidden">
        <div className="border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 lg:px-5">
          <SystemAnnouncementFilterBar
            appliedFilters={appliedFilters}
            draftFilters={draftFilters}
            onDraftFiltersChange={onDraftFiltersChange}
            onApply={onApplyFilters}
            onClear={onClearFilters}
            onQuickFilter={onQuickFilter}
            onRemoveFilterChip={onRemoveFilterChip}
          />
        </div>

        {status === "loading" ? <ListSkeleton /> : null}

        {status === "forbidden" ? (
          <div className="p-4 lg:p-5">
            <SystemOperationsForbiddenState message={errorMessage} />
          </div>
        ) : null}

        {status === "error" ? (
          <div className="p-4 lg:p-5">
            <AdminSurfaceCard padding="lg" className="border-admin-danger/30">
              <ErrorState message={errorMessage} />
              <button
                type="button"
                onClick={onRetry}
                className="mt-4 inline-flex min-h-11 items-center justify-center rounded-lg bg-admin-accent px-4 py-2 text-sm font-medium text-white hover:bg-admin-accent-strong"
              >
                Thử lại
              </button>
            </AdminSurfaceCard>
          </div>
        ) : null}

        {status === "ready" ? (
          <div className="p-4 lg:p-5">
            <div className="mb-4 flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between">
              <AdminPagination
                className="min-w-0 flex-1"
                currentPage={currentPage}
                totalPages={totalPages}
                summary={summary}
                showPageNumbers
                onPrevious={() => onPageChange?.(currentPage - 1)}
                onNext={() => onPageChange?.(currentPage + 1)}
                onGoToPage={onPageChange}
                previousLabel="Trước"
                nextLabel="Sau"
              />
              <PageSizeSelect
                value={pageSize}
                onChange={onPageSizeChange}
                disabled={totalPages === 0 && !items?.length}
              />
            </div>

            {items?.length ? (
              <SystemAnnouncementTable
                items={items}
                selectedAnnouncementId={selectedAnnouncementId}
                onRowSelect={onRowSelect}
              />
            ) : (
              <AnnouncementEmptyState canCreate={canCreateAnnouncements} onCreateClick={onCreateClick} />
            )}
          </div>
        ) : null}
      </AdminSurfaceCard>

      {drawer}
      {createDrawer}
    </div>
  );
}
