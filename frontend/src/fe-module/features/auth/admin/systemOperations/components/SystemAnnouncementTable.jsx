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
} from "../../components/ui";
import { AnnouncementSeverityBadge, AnnouncementStatusBadge } from "./ui/SystemOperationsBadges.jsx";

function AnnouncementMobileCard({ item, onActionRequest }) {
  return (
    <AdminMobileCard ariaLabel={`Thông báo ${item.title}`}>
      <div className="flex items-start justify-between gap-2">
        <p className="min-w-0 flex-1 font-medium text-admin-text">{item.title}</p>
        <AnnouncementStatusBadge status={item.status} />
      </div>
      <p className="mt-2 line-clamp-3 text-xs text-admin-text-secondary">{item.content}</p>
      <div className="mt-2 flex flex-wrap items-center gap-2">
        <AnnouncementSeverityBadge severity={item.severity} />
        <span className="text-xs text-admin-text-muted">Pin: {item.pinned ? "Có" : "Không"}</span>
      </div>
      <p className="mt-1 text-xs text-admin-text-muted">
        {item.sentAt ? formatDateTime(item.sentAt) : "—"}
      </p>
      <div className="mt-3 flex flex-wrap justify-end gap-2 border-t border-admin-border-subtle pt-3">
        {item.status === "DRAFT" ? (
          <AdminFilterButton
            type="button"
            variant="primary"
            className="min-h-9 w-full text-xs sm:w-auto"
            onClick={() => onActionRequest?.({ type: "publish", item })}
          >
            Publish
          </AdminFilterButton>
        ) : null}
        {item.status === "SENT" ? (
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-9 w-full text-xs sm:w-auto"
            onClick={() => onActionRequest?.({ type: item.pinned ? "unpin" : "pin", item })}
          >
            {item.pinned ? "Bỏ pin" : "Pin"}
          </AdminFilterButton>
        ) : null}
        {item.status !== "CANCELLED" ? (
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="min-h-9 w-full border-admin-danger/30 text-admin-danger hover:bg-admin-danger-soft sm:w-auto"
            onClick={() => onActionRequest?.({ type: "cancel", item })}
          >
            Hủy
          </AdminFilterButton>
        ) : null}
      </div>
    </AdminMobileCard>
  );
}

export function SystemAnnouncementTable({ items, onActionRequest }) {
  if (!items?.length) return null;

  return (
    <>
      <AdminMobileCardList className="p-4 md:hidden">
        {items.map((item) => (
          <AnnouncementMobileCard key={item.announcementId} item={item} onActionRequest={onActionRequest} />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="800px" ariaLabel="Danh sách thông báo hệ thống">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Tiêu đề</AdminDataTableCell>
            <AdminDataTableCell header>Mức độ</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header className="hidden sm:table-cell">
              Pin
            </AdminDataTableCell>
            <AdminDataTableCell header className="hidden md:table-cell">
              Gửi lúc
            </AdminDataTableCell>
            <AdminDataTableCell header className="text-right">
              Thao tác
            </AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => (
            <AdminDataTableRow key={item.announcementId}>
              <AdminDataTableCell>
                <p className="font-medium text-admin-text">{item.title}</p>
                <p className="mt-1 line-clamp-2 text-xs text-admin-text-secondary">{item.content}</p>
              </AdminDataTableCell>
              <AdminDataTableCell>
                <AnnouncementSeverityBadge severity={item.severity} />
              </AdminDataTableCell>
              <AdminDataTableCell>
                <AnnouncementStatusBadge status={item.status} />
              </AdminDataTableCell>
              <AdminDataTableCell className="hidden text-xs sm:table-cell">
                {item.pinned ? "Có" : "Không"}
              </AdminDataTableCell>
              <AdminDataTableCell className="hidden text-xs text-admin-text-secondary md:table-cell">
                {item.sentAt ? formatDateTime(item.sentAt) : "—"}
              </AdminDataTableCell>
              <AdminDataTableCell>
                <div className="flex flex-wrap justify-end gap-2">
                  {item.status === "DRAFT" ? (
                    <AdminFilterButton
                      type="button"
                      variant="primary"
                      className="min-h-9 px-2.5 py-1 text-xs"
                      onClick={() => onActionRequest?.({ type: "publish", item })}
                    >
                      Publish
                    </AdminFilterButton>
                  ) : null}
                  {item.status === "SENT" ? (
                    <AdminFilterButton
                      type="button"
                      variant="secondary"
                      className="min-h-9 px-2.5 py-1 text-xs"
                      onClick={() =>
                        onActionRequest?.({ type: item.pinned ? "unpin" : "pin", item })
                      }
                    >
                      {item.pinned ? "Bỏ pin" : "Pin"}
                    </AdminFilterButton>
                  ) : null}
                  {item.status !== "CANCELLED" ? (
                    <AdminFilterButton
                      type="button"
                      variant="secondary"
                      className="min-h-9 border-admin-danger/30 px-2.5 py-1 text-xs text-admin-danger hover:bg-admin-danger-soft"
                      onClick={() => onActionRequest?.({ type: "cancel", item })}
                    >
                      Hủy
                    </AdminFilterButton>
                  ) : null}
                </div>
              </AdminDataTableCell>
            </AdminDataTableRow>
          ))}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
