import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminFilterButton,
  AdminMobileCard,
  AdminMobileCardList,
  AdminSurfaceCard,
} from "../../components/ui";
import { ConfigActiveBadge } from "./ui/SystemOperationsBadges.jsx";

function SystemConfigMobileCard({ item, selected, canUpdate, onSelectConfig, onOpenHistory }) {
  return (
    <AdminMobileCard isSelected={selected} ariaLabel={`Cấu hình ${item.configKey}`}>
      <div className="flex items-start justify-between gap-2">
        <p className="min-w-0 flex-1 break-all font-mono text-xs font-medium text-admin-text" title={item.configKey}>
          {item.configKey}
        </p>
        <ConfigActiveBadge active={item.active} />
      </div>
      <p className="mt-2 break-all font-mono text-xs text-admin-text-secondary" title={item.configValue}>
        {item.configValue}
      </p>
      <p className="mt-2 text-xs text-admin-text-muted">
        {item.valueType} · {item.updatedAt ? formatDateTime(item.updatedAt) : formatDateTime(item.createdAt)}
      </p>
      <div className="mt-3 flex flex-wrap gap-2 border-t border-admin-border-subtle pt-3">
        <AdminFilterButton
          type="button"
          variant="secondary"
          className="min-h-11 flex-1 sm:flex-none"
          onClick={() => onSelectConfig?.(item.configId)}
        >
          {canUpdate ? "Sửa" : "Xem"}
        </AdminFilterButton>
        <AdminFilterButton
          type="button"
          variant="secondary"
          className="min-h-11 flex-1 sm:flex-none"
          onClick={() => onOpenHistory?.(item.configId)}
        >
          Lịch sử
        </AdminFilterButton>
      </div>
    </AdminMobileCard>
  );
}

export function SystemConfigTable({
  items,
  selectedConfigId,
  onSelectConfig,
  onOpenHistory,
  canUpdate,
}) {
  if (!items?.length) return null;

  return (
    <>
      <AdminMobileCardList className="p-4 md:hidden">
        {items.map((item) => (
          <SystemConfigMobileCard
            key={item.configId}
            item={item}
            selected={selectedConfigId === item.configId}
            canUpdate={canUpdate}
            onSelectConfig={onSelectConfig}
            onOpenHistory={onOpenHistory}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="720px" ariaLabel="Danh sách cấu hình hệ thống">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Key</AdminDataTableCell>
            <AdminDataTableCell header>Giá trị</AdminDataTableCell>
            <AdminDataTableCell header>Kiểu</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Cập nhật</AdminDataTableCell>
            <AdminDataTableCell header className="text-right">
              Thao tác
            </AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => {
            const selected = selectedConfigId === item.configId;
            return (
              <AdminDataTableRow key={item.configId} isSelected={selected}>
                <AdminDataTableCell className="font-mono text-xs">{item.configKey}</AdminDataTableCell>
                <AdminDataTableCell className="max-w-xs">
                  <span className="block truncate font-mono text-xs" title={item.configValue}>
                    {item.configValue}
                  </span>
                </AdminDataTableCell>
                <AdminDataTableCell>{item.valueType}</AdminDataTableCell>
                <AdminDataTableCell>
                  <ConfigActiveBadge active={item.active} />
                </AdminDataTableCell>
                <AdminDataTableCell className="text-xs text-admin-text-secondary">
                  {item.updatedAt ? formatDateTime(item.updatedAt) : formatDateTime(item.createdAt)}
                </AdminDataTableCell>
                <AdminDataTableCell className="text-right">
                  <div className="flex flex-wrap justify-end gap-2">
                    <AdminFilterButton
                      type="button"
                      variant="secondary"
                      className="min-h-9 px-2.5 py-1 text-xs"
                      onClick={() => onSelectConfig?.(item.configId)}
                    >
                      {canUpdate ? "Sửa" : "Xem"}
                    </AdminFilterButton>
                    <AdminFilterButton
                      type="button"
                      variant="secondary"
                      className="min-h-9 px-2.5 py-1 text-xs"
                      onClick={() => onOpenHistory?.(item.configId)}
                    >
                      Lịch sử
                    </AdminFilterButton>
                  </div>
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
