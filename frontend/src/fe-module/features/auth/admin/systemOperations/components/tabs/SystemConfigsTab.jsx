import { useMemo, useState } from "react";
import { AccountCard, AccountSkeleton, TabPanelHeader } from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";
import { CONFIG_PAGE_SIZE, CONFIG_VIEW_MODES } from "../../constants/systemConfigConstants.js";
import {
  SYSTEM_CONFIGS_EMPTY,
  SYSTEM_CONFIGS_FORBIDDEN,
  SYSTEM_CONFIGS_SUBTITLE,
  SYSTEM_CONFIGS_TITLE,
  GENERIC_RETRY,
} from "../../constants/systemOperationsUiStrings.js";
import { useSystemConfigMutations } from "../../hooks/useSystemConfigMutations.js";
import { useSystemConfigPermissions } from "../../hooks/useSystemConfigPermissions.js";
import { useSystemConfigs } from "../../hooks/useSystemConfigs.js";
import { CreateSystemConfigModal } from "../CreateSystemConfigModal.jsx";
import { EditSystemConfigDrawer } from "../EditSystemConfigDrawer.jsx";
import { SystemConfigFilterBar } from "../SystemConfigFilterBar.jsx";
import { SystemConfigTable } from "../SystemConfigTable.jsx";
import { SystemOperationsEmptyState } from "../SystemOperationsEmptyState.jsx";
import { SystemOperationsForbiddenState } from "../SystemOperationsForbiddenState.jsx";

export function SystemConfigsTab({
  configId,
  configView,
  configFilters,
  onFiltersChange,
  onConfigSelectionChange,
  onNotify,
}) {
  const { canViewConfigs, canUpdateConfigs } = useSystemConfigPermissions();
  const { result, status, errorMessage, refetch } = useSystemConfigs({
    configFilters,
    enabled: canViewConfigs,
  });
  const [createOpen, setCreateOpen] = useState(false);

  const mutations = useSystemConfigMutations({
    onSuccess: () => {
      refetch();
      onNotify?.({ variant: "success", message: "Đã lưu cấu hình." });
    },
    onError: (error) => {
      onNotify?.({ variant: "error", message: error?.message || "Không thể lưu cấu hình." });
    },
  });

  const selectedConfig = useMemo(() => {
    if (!configId || !result?.items) return null;
    return result.items.find((item) => item.configId === configId) || null;
  }, [configId, result?.items]);

  const currentPage = Number(configFilters?.page) || 1;
  const totalPages = result?.totalPages || 0;

  const handleApplyFilters = (next) => onFiltersChange?.(next);
  const handleResetFilters = () =>
    onFiltersChange?.({
      q: "",
      value_type: "",
      is_active: "",
      page: "1",
      size: String(CONFIG_PAGE_SIZE),
    });

  const handlePageChange = (nextPage) =>
    onFiltersChange?.({
      ...configFilters,
      page: String(nextPage),
      size: configFilters?.size || String(CONFIG_PAGE_SIZE),
    });

  const summary = useMemo(() => {
    if (!result) return "";
    return `${result.totalElements} cấu hình · trang ${result.page}/${Math.max(result.totalPages, 1)}`;
  }, [result]);

  if (!canViewConfigs) {
    return (
      <div className="space-y-6">
        <TabPanelHeader title={SYSTEM_CONFIGS_TITLE} subtitle={SYSTEM_CONFIGS_SUBTITLE} />
        <SystemOperationsForbiddenState message={SYSTEM_CONFIGS_FORBIDDEN} />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <TabPanelHeader title={SYSTEM_CONFIGS_TITLE} subtitle={SYSTEM_CONFIGS_SUBTITLE} />

      <AccountCard>
        <SystemConfigFilterBar
          key={[configFilters?.q, configFilters?.value_type, configFilters?.is_active].join("|")}
          filters={configFilters}
          onApply={handleApplyFilters}
          onReset={handleResetFilters}
        />
      </AccountCard>

      {canUpdateConfigs ? (
        <div className="flex justify-end">
          <button
            type="button"
            onClick={() => setCreateOpen(true)}
            className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
          >
            Tạo cấu hình
          </button>
        </div>
      ) : null}

      {status === "loading" ? <AccountSkeleton /> : null}
      {status === "forbidden" ? <SystemOperationsForbiddenState message={errorMessage} /> : null}
      {status === "error" ? (
        <AccountCard className="border-error/30">
          <ErrorState message={errorMessage} />
          <button type="button" onClick={refetch} className="mt-4 rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white">
            {GENERIC_RETRY}
          </button>
        </AccountCard>
      ) : null}

      {status === "ready" ? (
        <AccountCard>
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm text-on-surface-variant">{summary}</p>
            <div className="flex gap-2">
              <button
                type="button"
                disabled={currentPage <= 1}
                onClick={() => handlePageChange(currentPage - 1)}
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Trước
              </button>
              <button
                type="button"
                disabled={currentPage >= totalPages}
                onClick={() => handlePageChange(currentPage + 1)}
                className="rounded-lg border border-outline-variant px-3 py-1.5 text-sm disabled:opacity-40"
              >
                Sau
              </button>
            </div>
          </div>
          {result?.items?.length ? (
            <SystemConfigTable
              items={result.items}
              selectedConfigId={configId}
              canUpdate={canUpdateConfigs}
              onSelectConfig={(id) => onConfigSelectionChange?.({ configId: id, configView: CONFIG_VIEW_MODES.EDIT })}
              onOpenHistory={(id) =>
                onConfigSelectionChange?.({ configId: id, configView: CONFIG_VIEW_MODES.HISTORY })
              }
            />
          ) : (
            <SystemOperationsEmptyState message={SYSTEM_CONFIGS_EMPTY} />
          )}
        </AccountCard>
      ) : null}

      <CreateSystemConfigModal
        open={createOpen}
        pending={mutations.pending}
        onClose={() => setCreateOpen(false)}
        onSubmit={async (form) => {
          await mutations.createConfig(form);
          setCreateOpen(false);
        }}
      />

      <EditSystemConfigDrawer
        config={selectedConfig}
        configView={configView || CONFIG_VIEW_MODES.EDIT}
        loading={status === "loading" && Boolean(configId)}
        canUpdate={canUpdateConfigs}
        pending={mutations.pending}
        onClose={() => onConfigSelectionChange?.({ configId: null, configView: null })}
        onViewChange={(nextView) => onConfigSelectionChange?.({ configId, configView: nextView })}
        onSave={async (form) => {
          if (!configId) return;
          await mutations.updateConfig(configId, form);
        }}
        onToggle={async (active, reason) => {
          if (!configId) return;
          await mutations.toggleConfig(configId, active, reason);
        }}
      />
    </div>
  );
}