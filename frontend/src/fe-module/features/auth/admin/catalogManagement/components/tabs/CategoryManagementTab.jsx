import { useCallback, useEffect, useMemo, useState } from "react";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import {
  activateAdminCategory,
  createAdminCategory,
  deactivateAdminCategory,
  fetchAdminCategories,
  updateAdminCategory,
} from "../../api/adminCatalogApi.js";
import { isCatalogForbiddenError } from "../../constants/catalogPermissions.js";
import { CatalogFormModal } from "../modals/CatalogFormModal.jsx";
import { useCatalogPermissions } from "../../hooks/useCatalogPermissions.js";
import { CategoryManagementTabView } from "./CategoryManagementTabView.jsx";

function mapCategory(item) {
  return {
    id: item.id,
    name: item.name,
    slug: item.slug,
    parentId: item.parent_id ?? item.parentId ?? null,
    level: item.level ?? 0,
    active: item.is_active ?? item.isActive ?? true,
    productCount: item.product_count ?? item.productCount ?? 0,
  };
}

const TITLE = "Quản lý danh mục";
const SUBTITLE = "Thêm, sửa và vô hiệu hóa danh mục sản phẩm thời trang.";
const EMPTY_MESSAGE = "Không có danh mục phù hợp.";

export function CategoryManagementTab({ onNotify }) {
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
      const data = await fetchAdminCategories({
        q: query || undefined,
        isActive: activeFilter === "" ? undefined : activeFilter === "true",
      });
      setItems((data?.items || []).map(mapCategory));
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
      setErrorMessage(error?.message || "Không tải được danh mục.");
      setLoadStatus("error");
    }
  }, [activeFilter, query, showSessionExpired]);

  useEffect(() => {
    if (canRead) load();
  }, [canRead, load]);

  const parentOptions = useMemo(
    () => items.filter((item) => item.active).sort((a, b) => a.level - b.level || a.name.localeCompare(b.name)),
    [items],
  );

  const runToggle = async (categoryId, active) => {
    setActionId(categoryId);
    try {
      if (active) {
        await activateAdminCategory(categoryId);
        onNotify?.({ variant: "success", message: "Đã kích hoạt danh mục." });
      } else {
        await deactivateAdminCategory(categoryId);
        onNotify?.({ variant: "success", message: "Đã vô hiệu hóa danh mục." });
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
      await updateAdminCategory(modal.item.id, payload);
      onNotify?.({ variant: "success", message: "Đã cập nhật danh mục." });
    } else {
      await createAdminCategory(payload);
      onNotify?.({ variant: "success", message: "Đã tạo danh mục mới." });
    }
    await load();
  };

  return (
    <CategoryManagementTabView
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
          title={modal?.mode === "edit" ? "Sửa danh mục" : "Thêm danh mục"}
          submitLabel={modal?.mode === "edit" ? "Lưu thay đổi" : "Tạo danh mục"}
          showParentField
          parentOptions={parentOptions.filter((option) => option.id !== modal?.item?.id)}
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
