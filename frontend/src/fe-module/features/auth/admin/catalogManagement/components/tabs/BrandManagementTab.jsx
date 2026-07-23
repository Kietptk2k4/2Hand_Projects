import { useCallback, useState } from "react";
import {
  activateAdminBrand,
  createAdminBrand,
  deactivateAdminBrand,
  updateAdminBrand,
} from "../../api/adminCatalogApi.js";
import { mapBrand } from "../../utils/adminCatalogMapper.js";
import { useCatalogBrands } from "../../hooks/useCatalogBrands.js";
import { useCatalogPermissions } from "../../hooks/useCatalogPermissions.js";
import { CatalogFormModal } from "../modals/CatalogFormModal.jsx";
import { BrandManagementTabView } from "./BrandManagementTabView.jsx";

const TITLE = "Quản lý thương hiệu";
const SUBTITLE = "Thêm, sửa và vô hiệu hóa thương hiệu trong catalog.";

export function BrandManagementTab({ onNotify }) {
  const { canRead, canWrite } = useCatalogPermissions();
  const {
    filters,
    items,
    pagination,
    heroMetrics,
    loadStatus,
    errorMessage,
    handleQueryChange,
    handleStatusChange,
    handlePageChange,
    handleKpiStatusClick,
    refreshAll,
    setListState,
  } = useCatalogBrands({ canRead });

  const [modal, setModal] = useState(null);
  const [actionId, setActionId] = useState("");
  const [detailItem, setDetailItem] = useState(null);
  const [deactivateItem, setDeactivateItem] = useState(null);
  const [deactivatePending, setDeactivatePending] = useState(false);

  const runActivate = useCallback(
    async (brandId) => {
      setActionId(brandId);
      try {
        const updated = await activateAdminBrand(brandId);
        const mapped = mapBrand(updated);
        if (mapped) {
          setListState((prev) => ({
            ...prev,
            items: prev.items.map((item) => (item.id === mapped.id ? mapped : item)),
          }));
        }
        onNotify?.({ variant: "success", message: "Đã kích hoạt thương hiệu." });
        setDetailItem((current) => (current?.id === brandId ? mapped : current));
        refreshAll();
      } catch (error) {
        onNotify?.({ variant: "error", message: error?.message || "Không thể kích hoạt thương hiệu." });
      } finally {
        setActionId("");
      }
    },
    [onNotify, refreshAll, setListState],
  );

  const runDeactivateConfirm = useCallback(async () => {
    if (!deactivateItem?.id) return;
    setDeactivatePending(true);
    setActionId(deactivateItem.id);
    try {
      await deactivateAdminBrand(deactivateItem.id);
      onNotify?.({ variant: "success", message: "Đã vô hiệu hóa thương hiệu." });
      setDeactivateItem(null);
      setDetailItem((current) =>
        current?.id === deactivateItem.id ? { ...current, active: false } : current,
      );
      refreshAll();
    } catch (error) {
      onNotify?.({ variant: "error", message: error?.message || "Không thể vô hiệu hóa thương hiệu." });
    } finally {
      setDeactivatePending(false);
      setActionId("");
    }
  }, [deactivateItem, onNotify, refreshAll]);

  const handleSubmit = async (payload) => {
    if (modal?.mode === "edit") {
      const updated = await updateAdminBrand(modal.item.id, payload);
      onNotify?.({ variant: "success", message: "Đã cập nhật thương hiệu." });
      const mapped = mapBrand(updated);
      if (mapped) {
        setListState((prev) => ({
          ...prev,
          items: prev.items.map((item) => (item.id === mapped.id ? mapped : item)),
        }));
      }
    } else {
      await createAdminBrand(payload);
      onNotify?.({ variant: "success", message: "Đã tạo thương hiệu mới." });
    }
    await refreshAll();
  };

  return (
    <BrandManagementTabView
      title={TITLE}
      subtitle={SUBTITLE}
      canRead={canRead}
      canWrite={canWrite}
      filters={filters}
      pagination={pagination}
      loadStatus={loadStatus}
      errorMessage={errorMessage}
      heroMetrics={heroMetrics}
      items={items}
      actionId={actionId}
      detailItem={detailItem}
      deactivateItem={deactivateItem}
      deactivatePending={deactivatePending}
      onQueryChange={handleQueryChange}
      onStatusChange={handleStatusChange}
      onPageChange={handlePageChange}
      onKpiStatusClick={handleKpiStatusClick}
      onRefresh={refreshAll}
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
      onOpenDetail={setDetailItem}
      onCloseDetail={() => setDetailItem(null)}
      onRetry={refreshAll}
      modal={
        <CatalogFormModal
          open={Boolean(modal)}
          title={modal?.mode === "edit" ? "Sửa thương hiệu" : "Thêm thương hiệu"}
          submitLabel={modal?.mode === "edit" ? "Lưu thay đổi" : "Tạo thương hiệu"}
          initialValues={
            modal?.item ? { name: modal.item.name, slug: modal.item.slug } : undefined
          }
          onClose={() => setModal(null)}
          onSubmit={handleSubmit}
        />
      }
    />
  );
}
