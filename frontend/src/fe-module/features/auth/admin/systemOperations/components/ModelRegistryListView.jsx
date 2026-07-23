import { AdminFilterButton, AdminPageHeader, AdminSurfaceCard } from "../../components/ui";
import { ErrorState } from "../../../../../shared/ui/PageState.jsx";
import {
  MODEL_REGISTRY_FORBIDDEN,
  MODEL_REGISTRY_SUBTITLE,
  MODEL_REGISTRY_TITLE,
} from "../constants/systemOperationsUiStrings.js";
import { ModelRegistryActiveFilterChips } from "./ModelRegistryActiveFilterChips.jsx";
import { ModelRegistryQuickFilterChips } from "./ModelRegistryQuickFilterChips.jsx";
import { ModelRegistryRuntimeStatusCard } from "./ModelRegistryRuntimeStatusCard.jsx";
import { ModelRegistryStatsBar } from "./ModelRegistryStatsBar.jsx";
import { ModelRegistryTable } from "./ModelRegistryTable.jsx";
import { SystemOperationsForbiddenState } from "./SystemOperationsForbiddenState.jsx";

function ListSkeleton() {
  return (
    <div className="space-y-3 p-4 lg:p-5">
      {Array.from({ length: 5 }, (_, index) => (
        <div key={index} className="h-14 animate-pulse rounded-lg bg-admin-surface-muted" />
      ))}
    </div>
  );
}

function ModelRegistryEmptyState() {
  return (
    <div className="rounded-xl border border-dashed border-admin-border px-6 py-12 text-center">
      <span
        className="material-symbols-outlined mx-auto text-[40px] text-admin-text-muted"
        aria-hidden="true"
      >
        model_training
      </span>
      <p className="mt-4 text-sm font-medium text-admin-text">Chưa có phiên bản model nào</p>
      <p className="mx-auto mt-2 max-w-md text-sm text-admin-text-secondary">
        Mô hình feed_ranker được tạo qua pipeline recsys-offline (export-activate). Giao diện admin chỉ
        xem registry — không kích hoạt export từ đây.
      </p>
    </div>
  );
}

export function ModelRegistryListView({
  status,
  errorMessage,
  forbiddenMessage,
  appliedFilters,
  onQuickFilter,
  onRemoveFilterChip,
  onRetry,
  onRefresh,
  stats,
  runtimeStatus,
  runtimeStatusState,
  runtimeStatusError,
  onRuntimeStatusRetry,
  items,
  selectedVersion,
  onRowSelect,
  drawer,
}) {
  if (status === "forbidden") {
    return (
      <div className="mb-6 max-w-full min-w-0 space-y-6">
        <AdminPageHeader
          eyebrow="Vận hành hệ thống"
          title={MODEL_REGISTRY_TITLE}
          subtitle={MODEL_REGISTRY_SUBTITLE}
        />
        <SystemOperationsForbiddenState message={forbiddenMessage || MODEL_REGISTRY_FORBIDDEN} />
      </div>
    );
  }

  const summary =
    status === "success" ? `${items?.length ?? 0} phiên bản · feed_ranker` : "";

  return (
    <div className="mb-6 max-w-full min-w-0 space-y-6">
      <AdminPageHeader
        eyebrow="Vận hành hệ thống"
        title={MODEL_REGISTRY_TITLE}
        subtitle={MODEL_REGISTRY_SUBTITLE}
        actions={
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-11"
            onClick={onRefresh}
            disabled={status === "loading"}
          >
            Làm mới
          </AdminFilterButton>
        }
      />

      <ModelRegistryRuntimeStatusCard
        runtimeStatus={runtimeStatus}
        status={runtimeStatusState}
        errorMessage={runtimeStatusError}
        onRetry={onRuntimeStatusRetry}
      />

      {status === "success" ? (
        <ModelRegistryStatsBar stats={stats} onPresetClick={onQuickFilter} />
      ) : null}

      <AdminSurfaceCard padding="none" className="overflow-hidden">
        <div className="space-y-3 border-b border-admin-border-subtle bg-admin-surface-raised px-4 py-4 lg:px-5">
          <ModelRegistryQuickFilterChips filters={appliedFilters} onQuickFilter={onQuickFilter} />
          <ModelRegistryActiveFilterChips filters={appliedFilters} onRemoveChip={onRemoveFilterChip} />
        </div>

        {status === "loading" ? <ListSkeleton /> : null}

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

        {status === "success" ? (
          <div className="p-4 lg:p-5">
            {summary ? (
              <p className="mb-4 text-sm text-admin-text-secondary">{summary}</p>
            ) : null}

            {items?.length ? (
              <ModelRegistryTable
                items={items}
                selectedVersion={selectedVersion}
                onRowSelect={onRowSelect}
              />
            ) : (
              <ModelRegistryEmptyState />
            )}
          </div>
        ) : null}
      </AdminSurfaceCard>

      {drawer}
    </div>
  );
}
