import { formatPostListDateTime } from "../../contentModeration/utils/postDateTimeDisplay.js";
import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";
import {
  ANNOUNCEMENT_SEVERITY_LABELS,
  ANNOUNCEMENT_STATUS_LABELS,
} from "../constants/announcementListConstants.js";
import { AnnouncementSeverityBadge, AnnouncementStatusBadge } from "./ui/SystemOperationsBadges.jsx";

function UpdatedAtCell({ value }) {
  const formatted = formatPostListDateTime(value);
  return (
    <div className="text-xs tabular-nums text-admin-text-secondary">
      <div>{formatted.date}</div>
      <div className="text-admin-text-muted">{formatted.time}</div>
    </div>
  );
}

function PinIndicator({ pinned }) {
  if (!pinned) {
    return <span className="text-xs text-admin-text-muted">—</span>;
  }
  return (
    <span className="inline-flex items-center gap-1 text-xs text-admin-accent">
      <span className="material-symbols-outlined text-[16px]" aria-hidden="true">
        push_pin
      </span>
      Đã pin
    </span>
  );
}

function AnnouncementMobileCard({ item, selected, onRowSelect }) {
  return (
    <AdminMobileCard
      isSelected={selected}
      onClick={() => onRowSelect?.(item)}
      ariaLabel={`Thông báo ${item.title}`}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0 flex-1">
          <p className="font-medium text-admin-text">{item.title}</p>
          <p className="mt-1 line-clamp-2 text-xs text-admin-text-secondary">{item.content}</p>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <AnnouncementSeverityBadge severity={item.severity} />
            <AnnouncementStatusBadge status={item.status} />
            <PinIndicator pinned={item.pinned} />
          </div>
          <div className="mt-2">
            <UpdatedAtCell value={item.sentAt || item.createdAt} />
          </div>
        </div>
        <span className="material-symbols-outlined shrink-0 text-admin-text-muted" aria-hidden="true">
          chevron_right
        </span>
      </div>
    </AdminMobileCard>
  );
}

export function SystemAnnouncementTable({ items, selectedAnnouncementId, onRowSelect }) {
  if (!items?.length) return null;

  return (
    <>
      <AdminMobileCardList className="md:hidden">
        {items.map((item) => (
          <AnnouncementMobileCard
            key={item.announcementId}
            item={item}
            selected={selectedAnnouncementId === item.announcementId}
            onRowSelect={onRowSelect}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="880px" ariaLabel="Danh sách thông báo hệ thống">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Tiêu đề</AdminDataTableCell>
            <AdminDataTableCell header>Mức độ</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Pin</AdminDataTableCell>
            <AdminDataTableCell header>Gửi lúc</AdminDataTableCell>
            <AdminDataTableCell header className="w-10" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => {
            const selected = selectedAnnouncementId === item.announcementId;

            return (
              <AdminDataTableRow
                key={item.announcementId}
                isSelected={selected}
                onClick={() => onRowSelect?.(item)}
                className="cursor-pointer"
              >
                <AdminDataTableCell className="max-w-sm">
                  <p className="font-medium text-admin-text">{item.title}</p>
                  <p className="mt-1 line-clamp-2 text-xs text-admin-text-secondary">{item.content}</p>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AnnouncementSeverityBadge severity={item.severity} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <AnnouncementStatusBadge status={item.status} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <PinIndicator pinned={item.pinned} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <UpdatedAtCell value={item.sentAt || item.createdAt} />
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
