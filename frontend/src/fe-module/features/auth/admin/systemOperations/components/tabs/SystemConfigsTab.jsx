import { useMemo, useState } from "react";
import { CONFIG_PAGE_SIZE, CONFIG_VIEW_MODES } from "../../constants/systemConfigConstants.js";
import {
  SYSTEM_CONFIGS_EMPTY,
  SYSTEM_CONFIGS_FORBIDDEN,
  SYSTEM_CONFIGS_SUBTITLE,
  SYSTEM_CONFIGS_TITLE,
} from "../../constants/systemOperationsUiStrings.js";
import { useSystemConfigMutations } from "../../hooks/useSystemConfigMutations.js";
import { useSystemConfigPermissions } from "../../hooks/useSystemConfigPermissions.js";
import { useSystemConfigs } from "../../hooks/useSystemConfigs.js";
import { CreateSystemConfigModal } from "../CreateSystemConfigModal.jsx";
import { EditSystemConfigDrawer } from "../EditSystemConfigDrawer.jsx";
import { SystemConfigsTabView } from "./SystemConfigsTabView.jsx";

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

  const summary = useMemo(() => {
    if (!result) return "";
    return `${result.totalElements} cấu hình · trang ${result.page}/${Math.max(result.totalPages, 1)}`;
  }, [result]);

  return (
    <SystemConfigsTabView
      title={SYSTEM_CONFIGS_TITLE}
      subtitle={SYSTEM_CONFIGS_SUBTITLE}
      canViewConfigs={canViewConfigs}
      forbiddenMessage={SYSTEM_CONFIGS_FORBIDDEN}
      filterKey={[configFilters?.q, configFilters?.value_type, configFilters?.is_active].join("|")}
      configFilters={configFilters}
      canUpdateConfigs={canUpdateConfigs}
      status={status}
      errorMessage={errorMessage}
      summary={summary}
      currentPage={currentPage}
      totalPages={totalPages}
      items={result?.items}
      selectedConfigId={configId}
      emptyMessage={SYSTEM_CONFIGS_EMPTY}
      onApplyFilters={(next) => onFiltersChange?.(next)}
      onResetFilters={() =>
        onFiltersChange?.({
          q: "",
          value_type: "",
          is_active: "",
          page: "1",
          size: String(CONFIG_PAGE_SIZE),
        })
      }
      onPageChange={(nextPage) =>
        onFiltersChange?.({
          ...configFilters,
          page: String(nextPage),
          size: configFilters?.size || String(CONFIG_PAGE_SIZE),
        })
      }
      onCreateClick={() => setCreateOpen(true)}
      onSelectConfig={(id) =>
        onConfigSelectionChange?.({ configId: id, configView: CONFIG_VIEW_MODES.EDIT })
      }
      onOpenHistory={(id) =>
        onConfigSelectionChange?.({ configId: id, configView: CONFIG_VIEW_MODES.HISTORY })
      }
      onRetry={refetch}
      createModal={
        <CreateSystemConfigModal
          open={createOpen}
          pending={mutations.pending}
          onClose={() => setCreateOpen(false)}
          onSubmit={async (form) => {
            await mutations.createConfig(form);
            setCreateOpen(false);
          }}
        />
      }
      editDrawer={
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
      }
    />
  );
}
