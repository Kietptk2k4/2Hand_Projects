import { useCallback, useEffect, useState } from "react";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import {
  activateAdminBrand,
  createAdminBrand,
  deactivateAdminBrand,
  fetchAdminBrands,
  updateAdminBrand,
} from "../../api/adminCatalogApi.js";
import { isCatalogForbiddenError } from "../../constants/catalogPermissions.js";
import { CatalogFormModal } from "../modals/CatalogFormModal.jsx";
import { useCatalogPermissions } from "../../hooks/useCatalogPermissions.js";
import { BrandManagementTabView } from "./BrandManagementTabView.jsx";

function mapBrand(item) {
  return {
    id: item.id,
    name: item.name,
    slug: item.slug,
    active: item.is_active ?? item.isActive ?? true,
    productCount: item.product_count ?? item.productCount ?? 0,
  };
}

const TITLE = "Quản lý thương hiệu";
const SUBTITLE = "Thêm, sửa và vô hiệu hóa thương hiệu trong catalog.";
const EMPTY_MESSAGE = "Không có thương hiệu phù hợp.";

export function BrandManagementTab({ onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const { canRead, canWrite } = useCatalogPermissions();
  const [items, setItems] = useState([]);
  const [query, setQuery] = useState("");
  const [activeFilter, setActiveFilter] = useState("");
  const [loadStatus, setLoadStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [modal, setModal] = useState(null);
  const [actionId, setActionId] = useState("");

  const load = useCallback(async () => {
    setLoadStatus("loading");
    setErrorMessage("");
    try {
      const data = await fetchAdminBrands({
        q: query || undefined,
        isActive: activeFilter === "" ? undefined : activeFilter === "true",
        page: 1,
        limit: 50,
      });
      setItems((data?.items || []).map(mapBrand));
      setLoadStatus("ready");
    } catch (error) {
      if (String(error?.code ?? "").includes("401")) {
        showSessionExpired(error?.message);
        return;
      }
      if (isCatalogForbiddenError(error)) {
        setErrorMessage(error?.message || "Tài khoản thiếu quyền CATALOG_READ.");
        setLoadStatus("forbidden");
        return;
      }
      setErrorMessage(error?.message || "Không tải được thương hiệu.");
      setLoadStatus("error");
    }
  }, [activeFilter, query, showSessionExpired]);

  useEffect(() => {
    if (canRead) load();
  }, [canRead, load]);

  const runToggle = async (brandId, active) => {
    setActionId(brandId);
    try {
      if (active) {
        await activateAdminBrand(brandId);
        onNotify?.({ variant: "success", message: "Đã kích hoạt thương hiệu." });
      } else {
        await deactivateAdminBrand(brandId);
        onNotify?.({ variant: "success", message: "Đã vô hiệu hóa thương hiệu." });
      }
      await load();
    } catch (error) {
      onNotify?.({ variant: "error", message: error?.message || "Không thể cập nhật trạng thái." });
    } finally {
      setActionId("");
    }
  };

  const handleSubmit = async (payload) => {
    if (modal?.mode === "edit") {
      await updateAdminBrand(modal.item.id, payload);
      onNotify?.({ variant: "success", message: "Đã cập nhật thương hiệu." });
    } else {
      await createAdminBrand(payload);
      onNotify?.({ variant: "success", message: "Đã tạo thương hiệu mới." });
    }
    await load();
  };

  return (
    <BrandManagementTabView
      title={TITLE}
      subtitle={SUBTITLE}
      canRead={canRead}
      canWrite={canWrite}
      query={query}
      activeFilter={activeFilter}
      loadStatus={loadStatus}
      errorMessage={errorMessage}
      items={items}
      actionId={actionId}
      emptyMessage={EMPTY_MESSAGE}
      onQueryChange={setQuery}
      onActiveFilterChange={setActiveFilter}
      onFilter={load}
      onAdd={() => setModal({ mode: "create" })}
      onEdit={(item) => setModal({ mode: "edit", item })}
      onDeactivate={(id) => runToggle(id, false)}
      onActivate={(id) => runToggle(id, true)}
      onRetry={load}
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
