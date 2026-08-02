import { useCallback, useMemo, useState } from "react";
import { FeedToast } from "../../../../social/components/FeedToast";
import {
  DEFAULT_MODEL_NAME,
  MODEL_REGISTRY_TARGETS,
  MODEL_REGISTRY_VIEW_MODES,
} from "../constants/modelRegistryConstants.js";
import { useRecommendationModelArtifacts } from "../hooks/useRecommendationModelArtifacts.js";
import { useRecommendationModelStatus } from "../hooks/useRecommendationModelStatus.js";
import {
  buildModelRegistryQuickFilter,
  removeModelRegistryFilterChip,
} from "../utils/modelRegistryFilterHelpers.js";
import {
  computeModelRegistryStats,
  filterModelRegistryItems,
} from "../utils/modelRegistryDisplayUtils.js";
import { ModelRegistryDrawer } from "./ModelRegistryDrawer.jsx";
import { ModelRegistryListView } from "./ModelRegistryListView.jsx";

export function ModelRegistryListPanel({
  modelRegistryFilters,
  onFiltersChange,
  mrVersion,
  mrView,
  onModelRegistrySelectionChange,
}) {
  const [toastMessage, setToastMessage] = useState("");
  const [selectedModelName, setSelectedModelName] = useState(DEFAULT_MODEL_NAME);

  const { status, items, errorMessage, refetch } = useRecommendationModelArtifacts({
    enabled: true,
    modelName: selectedModelName,
  });
  const {
    status: runtimeStatusState,
    runtimeStatus,
    errorMessage: runtimeStatusError,
    refetch: refetchRuntimeStatus,
  } = useRecommendationModelStatus({ enabled: true, modelName: selectedModelName });

  const statusFilter = modelRegistryFilters?.status || "";
  const selectedTarget =
    MODEL_REGISTRY_TARGETS.find((t) => t.modelName === selectedModelName) ||
    MODEL_REGISTRY_TARGETS[0];

  const filteredItems = useMemo(
    () => filterModelRegistryItems(items, statusFilter),
    [items, statusFilter],
  );

  const stats = useMemo(() => computeModelRegistryStats(items), [items]);

  const selectedArtifact = useMemo(() => {
    if (!mrVersion) return null;
    return items.find((item) => String(item.version) === String(mrVersion)) || null;
  }, [items, mrVersion]);

  const refreshAll = useCallback(async () => {
    try {
      await Promise.all([refetch(), refetchRuntimeStatus()]);
      setToastMessage("Đã làm mới registry.");
    } catch {
      setToastMessage("Không thể làm mới registry.");
    }
  }, [refetch, refetchRuntimeStatus]);

  const handleQuickFilter = (preset) => {
    const next = buildModelRegistryQuickFilter(preset);
    onFiltersChange?.(next);
  };

  const handleRemoveFilterChip = (chipKey) => {
    const next = removeModelRegistryFilterChip(modelRegistryFilters, chipKey);
    onFiltersChange?.(next);
  };

  const handleRowSelect = (item) => {
    if (!item?.version) return;
    if (String(item.version) === String(mrVersion)) {
      onModelRegistrySelectionChange?.({ mrVersion: null, mrView: null });
      return;
    }
    onModelRegistrySelectionChange?.({
      mrVersion: String(item.version),
      mrView: MODEL_REGISTRY_VIEW_MODES.DETAIL,
    });
  };

  const handleModelChange = (nextModelName) => {
    setSelectedModelName(nextModelName);
    onModelRegistrySelectionChange?.({ mrVersion: null, mrView: null });
  };

  return (
    <>
      <ModelRegistryListView
        status={status}
        errorMessage={errorMessage}
        forbiddenMessage="Bạn cần role ADMIN hoặc MODERATOR để xem model registry."
        appliedFilters={modelRegistryFilters}
        onQuickFilter={handleQuickFilter}
        onRemoveFilterChip={handleRemoveFilterChip}
        onRetry={refetch}
        onRefresh={refreshAll}
        stats={stats}
        runtimeStatus={runtimeStatus}
        runtimeStatusState={runtimeStatusState}
        runtimeStatusError={runtimeStatusError}
        onRuntimeStatusRetry={refetchRuntimeStatus}
        items={filteredItems}
        selectedVersion={mrVersion}
        onRowSelect={handleRowSelect}
        modelTargets={MODEL_REGISTRY_TARGETS}
        selectedModelName={selectedModelName}
        onModelChange={handleModelChange}
        modelLabel={selectedTarget.label}
        drawer={
          mrVersion ? (
            <ModelRegistryDrawer
              artifact={selectedArtifact}
              viewMode={mrView || MODEL_REGISTRY_VIEW_MODES.DETAIL}
              onClose={() => onModelRegistrySelectionChange?.({ mrVersion: null, mrView: null })}
              onViewChange={(nextView) =>
                onModelRegistrySelectionChange?.({ mrVersion, mrView: nextView })
              }
            />
          ) : null
        }
      />

      <FeedToast message={toastMessage} onClose={() => setToastMessage("")} />
    </>
  );
}
