import { useCallback, useMemo, useState } from "react";
import {
  activateAdminCategory,
  createAdminCategory,
  deactivateAdminCategory,
  updateAdminCategory,
} from "../../api/adminCatalogApi.js";
import { mapCategory } from "../../utils/adminCatalogMapper.js";
import { filterParentOptions } from "../../utils/categoryHelpers.js";
import { useCatalogCategories } from "../../hooks/useCatalogCategories.js";
import { useCatalogPermissions } from "../../hooks/useCatalogPermissions.js";
import { CatalogFormModal } from "../modals/CatalogFormModal.jsx";
import { CategoryManagementTabView } from "./CategoryManagementTabView.jsx";

const TITLE = "Quản lý danh mục";
const SUBTITLE = "Thêm, sửa và vô hiệu hóa danh mục sản phẩm thời trang.";

export function CategoryManagementTab({ onNotify }) {
  const { canRead, canWrite } = useCatalogPermissions();
  const {
    filters,
    allItems,
    tree,
    categoryIndex,
    expandedIds,
    heroMetrics,
    loadStatus,
    errorMessage,
    handleQueryChange,
    handleStatusChange,
    handleKpiStatusClick,
    toggleExpanded,
    refresh,
    setItems,
  } = useCatalogCategories({ canRead });

  const [modal, setModal] = useState(null);
  const [actionId, setActionId] = useState("");
  const [detailItem, setDetailItem] = useState(null);
  const [deactivateItem, setDeactivateItem] = useState(null);
  const [deactivatePending, setDeactivatePending] = useState(false);

  const parentOptions = useMemo(
    () => filterParentOptions(allItems, modal?.item?.id),
    [allItems, modal?.item?.id],
  );

  const runActivate = useCallback(
    async (categoryId) => {
      setActionId(categoryId);
      try {
        const updated = await activateAdminCategory(categoryId);
        const mapped = mapCategory(updated);
        if (mapped) {
          setItems((prev) => prev.map((item) => (item.id === mapped.id ? mapped : item)));
        }
        onNotify?.({ variant: "success", message: "Đã kích hoạt danh mục." });
        setDetailItem((current) => (current?.id === categoryId ? mapped : current));
        refresh();
      } catch (error) {
        onNotify?.({ variant: "error", message: error?.message || "Không thể kích hoạt danh mục." });
      } finally {
        setActionId("");
      }
    },
    [onNotify, refresh, setItems],
  );

  const runDeactivateConfirm = useCallback(async () => {
    if (!deactivateItem?.id) return;
    setDeactivatePending(true);
    setActionId(deactivateItem.id);
    try {
      await deactivateAdminCategory(deactivateItem.id);
      onNotify?.({ variant: "success", message: "Đã vô hiệu hóa danh mục." });
      setDeactivateItem(null);
      setDetailItem((current) =>
        current?.id === deactivateItem.id ? { ...current, active: false } : current,
      );
      refresh();
    } catch (error) {
      onNotify?.({ variant: "error", message: error?.message || "Không thể vô hiệu hóa danh mục." });
    } finally {
      setDeactivatePending(false);
      setActionId("");
    }
  }, [deactivateItem, onNotify, refresh]);

  const handleSubmit = async (payload) => {
    if (modal?.mode === "edit") {
      const updated = await updateAdminCategory(modal.item.id, payload);
      onNotify?.({ variant: "success", message: "Đã cập nhật danh mục." });
      const mapped = mapCategory(updated);
      if (mapped) {
        setItems((prev) => prev.map((item) => (item.id === mapped.id ? mapped : item)));
      }
    } else {
      await createAdminCategory(payload);
      onNotify?.({ variant: "success", message: "Đã tạo danh mục mới." });
    }
    await refresh();
  };

  return (
    <CategoryManagementTabView
      title={TITLE}
      subtitle={SUBTITLE}
      canRead={canRead}
      canWrite={canWrite}
      filters={filters}
      loadStatus={loadStatus}
      errorMessage={errorMessage}
      heroMetrics={heroMetrics}
      tree={tree}
      categoryIndex={categoryIndex}
      allItems={allItems}
      expandedIds={expandedIds}
      actionId={actionId}
      detailItem={detailItem}
      deactivateItem={deactivateItem}
      deactivatePending={deactivatePending}
      onQueryChange={handleQueryChange}
      onStatusChange={handleStatusChange}
      onKpiStatusClick={handleKpiStatusClick}
      onRefresh={refresh}
      onAdd={() => setModal({ mode: "create" })}
      onEdit={(item) => {
        setDetailItem(null);
        setModal({ mode: "edit", item });
      }}
      onDeactivateRequest={(item) => setDeactivateItem(item)}
      onDeactivateConfirm={runDeactivateConfirm}
      onDeactivateClose={() => {
        if (!deactivatePending) setDeactivateItem(null);
      }}
      onActivate={runActivate}
      onToggleExpand={toggleExpanded}
      onOpenDetail={setDetailItem}
      onCloseDetail={() => setDetailItem(null)}
      onRetry={refresh}
      modal={
        <CatalogFormModal
          open={Boolean(modal)}
          title={modal?.mode === "edit" ? "Sửa danh mục" : "Thêm danh mục"}
          submitLabel={modal?.mode === "edit" ? "Lưu thay đổi" : "Tạo danh mục"}
          showParentField
          parentOptions={parentOptions}
          initialValues={
            modal?.item
              ? { name: modal.item.name, slug: modal.item.slug, parentId: modal.item.parentId || "" }
              : undefined
          }
          onClose={() => setModal(null)}
          onSubmit={handleSubmit}
        />
      }
    />
  );
}
