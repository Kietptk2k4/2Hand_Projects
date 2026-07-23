import { useCallback, useEffect, useMemo, useState } from "react";
import { FeedToast } from "../../../../social/components/FeedToast";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { fetchSystemConfigs } from "../api/systemConfigApi.js";
import { CONFIG_PAGE_SIZE, CONFIG_VIEW_MODES } from "../constants/systemConfigConstants.js";
import { SYSTEM_CONFIG_LIST_PAGE_SIZE } from "../constants/systemConfigListConstants.js";
import { useSystemConfigDetail } from "../hooks/useSystemConfigDetail.js";
import { useSystemConfigMutations } from "../hooks/useSystemConfigMutations.js";
import { useSystemConfigPermissions } from "../hooks/useSystemConfigPermissions.js";
import { useSystemConfigStats } from "../hooks/useSystemConfigStats.js";
import { mapSystemConfigsResponse } from "../utils/systemConfigMapper.js";
import { handleSystemOperationsLoadError } from "../utils/systemOperationsTabErrors.js";
import {
  buildSystemConfigQuickFilter,
  removeSystemConfigFilterChip,
} from "../utils/systemConfigFilterHelpers.js";
import { CreateSystemConfigModal } from "./CreateSystemConfigModal.jsx";
import { EditSystemConfigDrawer } from "./EditSystemConfigDrawer.jsx";
import { SystemConfigListView } from "./SystemConfigListView.jsx";

function buildQueryParams(filters) {
  const params = {
    page: Number(filters?.page) || 1,
    size: Number(filters?.size) || CONFIG_PAGE_SIZE,
  };
  if (filters?.q) params.q = filters.q;
  if (filters?.value_type) params.value_type = filters.value_type;
  if (filters?.is_active === "true" || filters?.is_active === "false") {
    params.is_active = filters.is_active === "true";
  }
  return params;
}

export function SystemConfigListPanel({
  configFilters,
  onFiltersChange,
  configId,
  configView,
  onConfigSelectionChange,
}) {
  const { showSessionExpired } = useAuthSession();
  const { canViewConfigs, canUpdateConfigs } = useSystemConfigPermissions();
  const [result, setResult] = useState(null);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState("");

  const filterQ = configFilters?.q || "";
  const filterValueType = configFilters?.value_type || "";
  const filterIsActive = configFilters?.is_active || "";
  const filterPage = Number(configFilters?.page) || 1;
  const filterSize = Number(configFilters?.size) || SYSTEM_CONFIG_LIST_PAGE_SIZE;

  const [draftFilters, setDraftFilters] = useState({
    q: filterQ,
    value_type: filterValueType,
    is_active: filterIsActive,
  });

  const { stats, status: statsStatus, refetch: refetchStats } = useSystemConfigStats({
    enabled: canViewConfigs,
  });

  const {
    detail: fetchedConfig,
    status: detailStatus,
    refetch: refetchDetail,
  } = useSystemConfigDetail(configId);

  const fetchList = useCallback(async () => {
    if (!canViewConfigs) return;

    setStatus("loading");
    setErrorMessage("");

    try {
      const data = await fetchSystemConfigs(buildQueryParams(configFilters));
      setResult(mapSystemConfigsResponse(data));
      setStatus("ready");
    } catch (error) {
      handleSystemOperationsLoadError(error, {
        showSessionExpired,
        setStatus,
        setErrorMessage,
        permissionHint: "SYSTEM_CONFIG_VIEW",
      });
      setResult(null);
    }
  }, [canViewConfigs, configFilters, showSessionExpired]);

  const refreshAll = useCallback(() => {
    fetchList();
    refetchStats();
    refetchDetail();
  }, [fetchList, refetchDetail, refetchStats]);

  const mutations = useSystemConfigMutations({
    onSuccess: () => {
      refreshAll();
      setToastMessage("Đã lưu cấu hình.");
    },
    onError: (error) => {
      setToastMessage(error?.message || "Không thể lưu cấu hình.");
    },
  });

  useEffect(() => {
    setDraftFilters({
      q: filterQ,
      value_type: filterValueType,
      is_active: filterIsActive,
    });
  }, [filterIsActive, filterQ, filterValueType]);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const applyFiltersPatch = useCallback(
    (patch) => {
      onFiltersChange?.({
        ...configFilters,
        ...patch,
      });
    },
    [configFilters, onFiltersChange],
  );

  const handleApplyFilters = (event) => {
    event.preventDefault();
    applyFiltersPatch({
      ...draftFilters,
      page: "1",
      size: String(filterSize),
    });
  };

  const handleClearFilters = () => {
    const cleared = {
      q: "",
      value_type: "",
      is_active: "",
      page: "1",
      size: String(SYSTEM_CONFIG_LIST_PAGE_SIZE),
    };
    setDraftFilters(cleared);
    onFiltersChange?.(cleared);
  };

  const handleQuickFilter = (preset) => {
    const next = buildSystemConfigQuickFilter(preset);
    setDraftFilters({
      q: next.q,
      value_type: next.value_type,
      is_active: next.is_active,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const handleRemoveFilterChip = (chipKey) => {
    const next = removeSystemConfigFilterChip(configFilters, chipKey);
    setDraftFilters({
      q: next.q,
      value_type: next.value_type,
      is_active: next.is_active,
    });
    applyFiltersPatch({
      ...next,
      size: String(filterSize),
    });
  };

  const listConfig = useMemo(() => {
    if (!configId || !result?.items) return null;
    return result.items.find((item) => item.configId === configId) || null;
  }, [configId, result?.items]);

  const selectedConfig = useMemo(() => {
    if (fetchedConfig) return fetchedConfig;
    return listConfig;
  }, [fetchedConfig, listConfig]);

  const currentPage = filterPage || result?.page || 1;
  const totalPages = result?.totalPages || 1;
  const items = result?.items || [];

  const handleRowSelect = (item) => {
    if (!item?.configId) return;
    if (item.configId === configId) {
      onConfigSelectionChange?.({ configId: null, configView: null });
      return;
    }
    onConfigSelectionChange?.({
      configId: item.configId,
      configView: CONFIG_VIEW_MODES.EDIT,
    });
  };

  return (
    <>
      <SystemConfigListView
        canViewConfigs={canViewConfigs}
        canUpdateConfigs={canUpdateConfigs}
        status={status}
        errorMessage={errorMessage}
        appliedFilters={configFilters}
        draftFilters={draftFilters}
        onDraftFiltersChange={setDraftFilters}
        onApplyFilters={handleApplyFilters}
        onClearFilters={handleClearFilters}
        onQuickFilter={handleQuickFilter}
        onRemoveFilterChip={handleRemoveFilterChip}
        onRetry={fetchList}
        stats={stats}
        statsStatus={statsStatus}
        onStatPresetClick={handleQuickFilter}
        items={items}
        result={result}
        currentPage={currentPage}
        totalPages={totalPages}
        pageSize={String(filterSize)}
        selectedConfigId={configId}
        onRowSelect={handleRowSelect}
        onPageChange={(nextPage) =>
          applyFiltersPatch({
            page: String(nextPage),
            size: String(filterSize),
          })
        }
        onPageSizeChange={(nextSize) =>
          applyFiltersPatch({
            page: "1",
            size: String(nextSize),
          })
        }
        onCreateClick={() => setCreateOpen(true)}
        drawer={
          configId ? (
            <EditSystemConfigDrawer
              config={selectedConfig}
              configView={configView || CONFIG_VIEW_MODES.EDIT}
              loading={detailStatus === "loading" && !selectedConfig}
              canUpdate={canUpdateConfigs}
              pending={mutations.pending}
              onClose={() => onConfigSelectionChange?.({ configId: null, configView: null })}
              onViewChange={(nextView) =>
                onConfigSelectionChange?.({ configId, configView: nextView })
              }
              onSave={async (form) => {
                if (!configId) return;
                await mutations.updateConfig(configId, form);
              }}
              onToggle={async (active, reason) => {
                if (!configId) return;
                await mutations.toggleConfig(configId, active, reason);
              }}
              onRefresh={refreshAll}
            />
          ) : null
        }
      />

      <CreateSystemConfigModal
        open={createOpen}
        pending={mutations.pending}
        onClose={() => setCreateOpen(false)}
        onSubmit={async (form) => {
          await mutations.createConfig(form);
          setCreateOpen(false);
          refreshAll();
        }}
      />

      <FeedToast message={toastMessage} onClose={() => setToastMessage("")} />
    </>
  );
}
