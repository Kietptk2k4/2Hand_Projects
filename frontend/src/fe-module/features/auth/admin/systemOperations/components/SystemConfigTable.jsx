import { AuditCopyableId } from "../../adminAudit/components/AuditCopyableId.jsx";
import { formatPostListDateTime } from "../../contentModeration/utils/postDateTimeDisplay.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
  AdminStatusBadge,
} from "../../components/ui";
import { SYSTEM_CONFIG_VALUE_TYPE_LABELS } from "../constants/systemConfigListConstants.js";
import { getDisplayConfigValue } from "../utils/systemConfigDisplayUtils.js";
import { ConfigActiveBadge } from "./ui/SystemOperationsBadges.jsx";

function ValueTypeBadge({ valueType }) {
  return (
    <AdminStatusBadge variant="neutral">
      {SYSTEM_CONFIG_VALUE_TYPE_LABELS[valueType] || valueType}
    </AdminStatusBadge>
  );
}

function UpdatedAtCell({ value }) {
  const formatted = formatPostListDateTime(value);
  return (
    <div className="text-xs tabular-nums text-admin-text-secondary">
      <div>{formatted.date}</div>
      <div className="text-admin-text-muted">{formatted.time}</div>
    </div>
  );
}

function SystemConfigMobileCard({ item, selected, onRowSelect }) {
  const displayValue = getDisplayConfigValue(item);

  return (
    <AdminMobileCard
      isSelected={selected}
      onClick={() => onRowSelect?.(item)}
      ariaLabel={`Cấu hình ${item.configKey}`}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0 flex-1">
          <AuditCopyableId value={item.configKey} label="Config key" mono />
          <p className="mt-2 break-all font-mono text-xs text-admin-text-secondary" title={displayValue}>
            {displayValue}
          </p>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <ValueTypeBadge valueType={item.valueType} />
            <ConfigActiveBadge active={item.active} />
          </div>
          {item.description ? (
            <p className="mt-2 line-clamp-2 text-xs text-admin-text-muted">{item.description}</p>
          ) : null}
          <div className="mt-2">
            <UpdatedAtCell value={item.updatedAt || item.createdAt} />
          </div>
        </div>
        <span className="material-symbols-outlined shrink-0 text-admin-text-muted" aria-hidden="true">
          chevron_right
        </span>
      </div>
    </AdminMobileCard>
  );
}

export function SystemConfigTable({ items, selectedConfigId, onRowSelect }) {
  if (!items?.length) return null;

  return (
    <>
      <AdminMobileCardList className="md:hidden">
        {items.map((item) => (
          <SystemConfigMobileCard
            key={item.configId}
            item={item}
            selected={selectedConfigId === item.configId}
            onRowSelect={onRowSelect}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="880px" ariaLabel="Danh sách cấu hình hệ thống">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Config key</AdminDataTableCell>
            <AdminDataTableCell header>Giá trị</AdminDataTableCell>
            <AdminDataTableCell header>Mô tả</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Cập nhật</AdminDataTableCell>
            <AdminDataTableCell header className="w-10" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => {
            const selected = selectedConfigId === item.configId;
            const displayValue = getDisplayConfigValue(item);

            return (
              <AdminDataTableRow
                key={item.configId}
                isSelected={selected}
                onClick={() => onRowSelect?.(item)}
                className="cursor-pointer"
              >
                <AdminDataTableCell className="max-w-[14rem]">
                  <div onClick={(event) => event.stopPropagation()}>
                    <AuditCopyableId value={item.configKey} label="Config key" mono />
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell className="max-w-xs">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="block truncate font-mono text-xs" title={displayValue}>
                      {item.valueMasked || displayValue === "********" ? (
                        <span className="inline-flex items-center gap-1 text-admin-text-muted">
                          <span className="material-symbols-outlined text-[14px]" aria-hidden="true">
                            lock
                          </span>
                          {displayValue}
                        </span>
                      ) : (
                        displayValue
                      )}
                    </span>
                    <ValueTypeBadge valueType={item.valueType} />
                  </div>
                </AdminDataTableCell>
                <AdminDataTableCell className="max-w-xs">
                  <span className="line-clamp-2 text-sm text-admin-text-secondary">
                    {item.description || "—"}
                  </span>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <ConfigActiveBadge active={item.active} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <UpdatedAtCell value={item.updatedAt || item.createdAt} />
                </AdminDataTableCell>
                <AdminDataTableCell className="text-right">
                  <span className="material-symbols-outlined text-admin-text-muted" aria-hidden="true">
                    chevron_right
                  </span>
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
