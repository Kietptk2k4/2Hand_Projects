import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  createAdminRole,
  deleteAdminRole,
  getAdminRoles,
  updateAdminRole,
} from "../../../api/authApi";
import { useAuthSession } from "../../../hooks/useAuthSession.jsx";
import { RBAC_SECTION_EYEBROW } from "../rbacPageContract.js";
import {
  countSystemRoles,
  formatLatestRoleUpdateLabel,
  shouldShowCreatedColumn,
} from "../utils/roleListUtils.js";
import { filterAndSortRoles } from "./RoleListFilterBar.jsx";
import { RoleListTabView } from "./RoleListTabView.jsx";
import { DeleteRoleConfirmModalView } from "./modals/DeleteRoleConfirmModalView.jsx";
import { RoleFormModalView, validateRoleForm } from "./modals/RoleFormModalView.jsx";

const TITLE = "Danh sách vai trò";
const SEARCH_DEBOUNCE_MS = 200;

function buildSubtitle(totalCount) {
  if (totalCount === 0) {
    return "Chưa có vai trò nào trong hệ thống.";
  }
  return `${totalCount} vai trò hệ thống · dùng để phân quyền truy cập admin và người dùng.`;
}

function formatErrorMessage(error) {
  const base = error?.message || "Không tải được danh sách vai trò.";
  if (!error?.code) return base;
  if (error.code === 403 || error.code === 401) return base;
  return base;
}

function mapMutationErrorMessage(error, fallback) {
  if (error?.code === 409) {
    const fieldError = error?.errors?.find?.((item) => item.field === "code");
    if (fieldError?.reason === "DUPLICATE") {
      return "Mã vai trò đã tồn tại.";
    }
    const roleError = error?.errors?.find?.((item) => item.field === "role_id");
    if (roleError?.reason === "IN_USE") {
      return "Không thể xóa vai trò đang được gán cho người dùng.";
    }
  }
  return error?.message || fallback;
}

export function RoleListTab({ onViewRolePermissions, onTabChange, onNotify }) {
  const { showSessionExpired } = useAuthSession();
  const [roles, setRoles] = useState([]);
  const [status, setStatus] = useState("loading");
  const [errorMessage, setErrorMessage] = useState("");
  const [errorCode, setErrorCode] = useState("");
  const [query, setQuery] = useState("");
  const [debouncedQuery, setDebouncedQuery] = useState("");
  const [sort, setSort] = useState("code_asc");

  const [formMode, setFormMode] = useState(null);
  const [editingRole, setEditingRole] = useState(null);
  const [formCode, setFormCode] = useState("");
  const [formName, setFormName] = useState("");
  const [formError, setFormError] = useState("");
  const [isFormSubmitting, setIsFormSubmitting] = useState(false);

  const [deletingRole, setDeletingRole] = useState(null);
  const [deleteError, setDeleteError] = useState("");
  const [isDeleteSubmitting, setIsDeleteSubmitting] = useState(false);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      setDebouncedQuery(query);
    }, SEARCH_DEBOUNCE_MS);

    return () => window.clearTimeout(timer);
  }, [query]);

  const load = useCallback(async () => {
    setStatus("loading");
    setErrorMessage("");
    setErrorCode("");
    try {
      const data = await getAdminRoles();
      setRoles(data?.roles || []);
      setStatus("ready");
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      if (error?.code === 403) {
        setStatus("forbidden");
        setErrorMessage(error?.message || "Bạn không có quyền truy cập.");
        setErrorCode(String(error.code));
        return;
      }
      setStatus("error");
      setErrorMessage(formatErrorMessage(error));
      setErrorCode(error?.code ? String(error.code) : "");
    }
  }, [showSessionExpired]);

  useEffect(() => {
    load();
  }, [load]);

  const filteredRoles = useMemo(
    () => filterAndSortRoles(roles, { query: debouncedQuery, sort }),
    [roles, debouncedQuery, sort],
  );

  const viewStatus = useMemo(() => {
    if (status !== "ready") return status;
    if (roles.length === 0) return "empty";
    if (filteredRoles.length === 0) return "empty";
    return "ready";
  }, [filteredRoles.length, roles.length, status]);

  const resultSummary = useMemo(() => {
    if (status !== "ready") return "";
    if (debouncedQuery.trim()) {
      return `Hiển thị ${filteredRoles.length} / ${roles.length} vai trò`;
    }
    return `${roles.length} vai trò`;
  }, [debouncedQuery, filteredRoles.length, roles.length, status]);

  const systemRoleCount = useMemo(() => countSystemRoles(roles), [roles]);
  const showCreatedColumn = useMemo(() => shouldShowCreatedColumn(roles), [roles]);
  const lastUpdatedLabel = useMemo(() => formatLatestRoleUpdateLabel(roles), [roles]);

  const closeFormModal = useCallback(() => {
    if (isFormSubmitting) return;
    setFormMode(null);
    setEditingRole(null);
    setFormCode("");
    setFormName("");
    setFormError("");
  }, [isFormSubmitting]);

  const openCreateModal = useCallback(() => {
    setFormMode("create");
    setEditingRole(null);
    setFormCode("");
    setFormName("");
    setFormError("");
  }, []);

  const openEditModal = useCallback((role) => {
    setFormMode("edit");
    setEditingRole(role);
    setFormCode(role.code || "");
    setFormName(role.name || "");
    setFormError("");
  }, []);

  const closeDeleteModal = useCallback(() => {
    if (isDeleteSubmitting) return;
    setDeletingRole(null);
    setDeleteError("");
  }, [isDeleteSubmitting]);

  const openDeleteModal = useCallback((role) => {
    setDeletingRole(role);
    setDeleteError("");
  }, []);

  const handleFormSubmit = useCallback(async () => {
    const validationError = validateRoleForm({
      mode: formMode,
      code: formCode,
      name: formName,
    });
    if (validationError) {
      setFormError(validationError);
      return;
    }

    setIsFormSubmitting(true);
    setFormError("");

    try {
      if (formMode === "create") {
        await createAdminRole({
          code: formCode.trim().toUpperCase(),
          name: formName.trim(),
        });
        onNotify?.({ variant: "success", message: "Tạo vai trò thành công." });
      } else if (formMode === "edit" && editingRole?.id) {
        await updateAdminRole(editingRole.id, { name: formName.trim() });
        onNotify?.({ variant: "success", message: "Cập nhật vai trò thành công." });
      }

      closeFormModal();
      await load();
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      const message = mapMutationErrorMessage(
        error,
        formMode === "create" ? "Không thể tạo vai trò." : "Không thể cập nhật vai trò.",
      );
      setFormError(message);
      onNotify?.({ variant: "error", message });
    } finally {
      setIsFormSubmitting(false);
    }
  }, [
    closeFormModal,
    editingRole?.id,
    formCode,
    formMode,
    formName,
    load,
    onNotify,
    showSessionExpired,
  ]);

  const handleDeleteConfirm = useCallback(async () => {
    if (!deletingRole?.id) return;

    setIsDeleteSubmitting(true);
    setDeleteError("");

    try {
      await deleteAdminRole(deletingRole.id);
      onNotify?.({ variant: "success", message: "Xóa vai trò thành công." });
      closeDeleteModal();
      await load();
    } catch (error) {
      if (error?.code === 401) {
        showSessionExpired(error?.message);
        return;
      }
      const message = mapMutationErrorMessage(error, "Không thể xóa vai trò.");
      setDeleteError(message);
      onNotify?.({ variant: "error", message });
    } finally {
      setIsDeleteSubmitting(false);
    }
  }, [closeDeleteModal, deletingRole?.id, load, onNotify, showSessionExpired]);

  const canManageRoles = status !== "forbidden";

  return (
    <>
      <RoleListTabView
        eyebrow={RBAC_SECTION_EYEBROW}
        title={TITLE}
        subtitle={buildSubtitle(roles.length)}
        status={viewStatus}
        errorMessage={errorMessage}
        errorCode={errorCode}
        forbiddenMessage={errorMessage}
        forbiddenAction={
          <Link
            to="/admin?section=rolePermission&tab=role-list"
            className="inline-flex min-h-11 items-center text-sm font-medium text-admin-warning underline-offset-2 transition-colors hover:text-admin-text hover:underline"
          >
            Về trang quản trị
          </Link>
        }
        emptyMessage={
          roles.length === 0
            ? "Chưa có vai trò nào."
            : "Không có vai trò phù hợp với bộ lọc."
        }
        emptyHint={
          roles.length === 0
            ? "Tạo vai trò mới để bắt đầu phân quyền."
            : "Thử đổi từ khóa hoặc bộ sắp xếp."
        }
        roles={filteredRoles}
        totalCount={roles.length}
        systemRoleCount={systemRoleCount}
        showCreatedColumn={showCreatedColumn}
        lastUpdatedLabel={lastUpdatedLabel}
        query={query}
        sort={sort}
        resultSummary={resultSummary}
        onQueryChange={setQuery}
        onSortChange={setSort}
        onViewRolePermissions={onViewRolePermissions}
        onEditRole={openEditModal}
        onDeleteRole={openDeleteModal}
        onCreateRole={openCreateModal}
        canManageRoles={canManageRoles}
        onTabChange={onTabChange}
        onRetry={load}
        isRefreshing={status === "loading"}
      />

      <RoleFormModalView
        open={formMode !== null}
        mode={formMode || "create"}
        code={formCode}
        name={formName}
        error={formError}
        isSubmitting={isFormSubmitting}
        onCodeChange={setFormCode}
        onNameChange={setFormName}
        onClose={closeFormModal}
        onSubmit={handleFormSubmit}
      />

      <DeleteRoleConfirmModalView
        open={Boolean(deletingRole)}
        role={deletingRole}
        error={deleteError}
        isSubmitting={isDeleteSubmitting}
        onClose={closeDeleteModal}
        onConfirm={handleDeleteConfirm}
      />
    </>
  );
}
