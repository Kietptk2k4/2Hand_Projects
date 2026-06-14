import { useCallback, useEffect, useMemo, useState } from "react";
import { AccountCard, AccountSkeleton, TabPanelHeader } from "../../../../../../shared/ui/auth/authUi.jsx";
import { ErrorState } from "../../../../../../shared/ui/PageState.jsx";
import { useAuthSession } from "../../../../hooks/useAuthSession.jsx";
import {
  activateAdminCategory,
  createAdminCategory,
  deactivateAdminCategory,
  fetchAdminCategories,
  updateAdminCategory,
} from "../../api/adminCatalogApi.js";
import { CatalogForbiddenState } from "../CatalogForbiddenState.jsx";
import { CatalogFormModal } from "../modals/CatalogFormModal.jsx";
import { useCatalogPermissions } from "../../hooks/useCatalogPermissions.js";

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
    <div className="space-y-4">
      <TabPanelHeader
        title="Quản lý danh mục"
        description="Thêm, sửa và vô hiệu hóa danh mục sản phẩm thời trang."
      />

      <AccountCard className="p-4">
        <div className="flex flex-wrap items-end gap-3">
          <div className="min-w-[200px] flex-1">
            <label className="mb-1 block text-sm font-medium">Tìm kiếm</label>
            <input
              className="w-full rounded-lg border border-outline-variant px-3 py-2 text-sm"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Tên hoặc slug"
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">Trạng thái</label>
            <select
              className="rounded-lg border border-outline-variant px-3 py-2 text-sm"
              value={activeFilter}
              onChange={(e) => setActiveFilter(e.target.value)}
            >
              <option value="">Tất cả</option>
              <option value="true">Đang hoạt động</option>
              <option value="false">Đã vô hiệu</option>
            </select>
          </div>
          <button
            type="button"
            className="rounded-lg border border-outline-variant px-4 py-2 text-sm"
            onClick={load}
          >
            Lọc
          </button>
          {canWrite ? (
            <button
              type="button"
              className="rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary"
              onClick={() => setModal({ mode: "create" })}
            >
              Thêm danh mục
            </button>
          ) : null}
        </div>
      </AccountCard>

      {loadStatus === "loading" ? <AccountSkeleton lines={6} /> : null}
      {loadStatus === "error" ? <ErrorState message={errorMessage} onRetry={load} /> : null}

      {loadStatus === "ready" ? (
        <AccountCard className="overflow-x-auto p-0">
          <table className="min-w-full text-sm">
            <thead className="bg-surface-container-low text-left text-on-surface-variant">
              <tr>
                <th className="px-4 py-3">Tên</th>
                <th className="px-4 py-3">Slug</th>
                <th className="px-4 py-3">Cấp</th>
                <th className="px-4 py-3">Sản phẩm</th>
                <th className="px-4 py-3">Trạng thái</th>
                {canWrite ? <th className="px-4 py-3">Thao tác</th> : null}
              </tr>
            </thead>
            <tbody>
              {items.length === 0 ? (
                <tr>
                  <td colSpan={canWrite ? 6 : 5} className="px-4 py-8 text-center text-on-surface-variant">
                    Khong co danh muc phu hop.
                  </td>
                </tr>
              ) : (
                items.map((item) => (
                <tr key={item.id} className="border-t border-outline-variant/40">
                  <td className="px-4 py-3">
                    <span style={{ paddingLeft: `${item.level * 16}px` }}>{item.name}</span>
                  </td>
                  <td className="px-4 py-3 font-mono text-xs">{item.slug}</td>
                  <td className="px-4 py-3">{item.level}</td>
                  <td className="px-4 py-3">{item.productCount}</td>
                  <td className="px-4 py-3">{item.active ? "Hoạt động" : "Vô hiệu"}</td>
                  {canWrite ? (
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-2">
                        <button
                          type="button"
                          className="text-primary hover:underline"
                          onClick={() => setModal({ mode: "edit", item })}
                        >
                          Sửa
                        </button>
                        {item.active ? (
                          <button
                            type="button"
                            className="text-error hover:underline disabled:opacity-50"
                            disabled={actionId === item.id}
                            onClick={() => runToggle(item.id, false)}
                          >
                            Vô hiệu
                          </button>
                        ) : (
                          <button
                            type="button"
                            className="text-primary hover:underline disabled:opacity-50"
                            disabled={actionId === item.id}
                            onClick={() => runToggle(item.id, true)}
                          >
                            Kích hoạt
                          </button>
                        )}
                      </div>
                    </td>
                  ) : null}
                </tr>
              ))
              )}
            </tbody>
          </table>
        </AccountCard>
      ) : null}

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
    </div>
  );
}
