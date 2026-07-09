import { formatDateTime } from "../../../security/utils/formatDateTime.js";
import { formatVndPrice } from "../../../../social/utils/formatPrice";
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
import { PayoutStatusBadge } from "./ui/PayoutStatusBadge.jsx";

function PayoutQueueMobileCard({ item, actionId, onAction }) {
  return (
    <AdminMobileCard ariaLabel={`Yêu cầu rút tiền ${item.sellerId}`}>
      <div className="flex items-start justify-between gap-2">
        <p className="break-all font-mono text-xs text-admin-text" title={item.sellerId}>
          {item.sellerId}
        </p>
        <PayoutStatusBadge status={item.status} />
      </div>
      <p
        className="mt-2 text-lg font-semibold tabular-nums text-admin-text"
        title={formatVndPrice(item.amount)}
      >
        {formatVndPrice(item.amount)}
      </p>
      <p className="mt-1 text-xs text-admin-text-muted">
        {item.requestedAt ? formatDateTime(item.requestedAt) : "—"}
      </p>
      <div className="mt-3 flex flex-wrap justify-end gap-2 border-t border-admin-border-subtle pt-3">
        {item.status === "REQUESTED" ? (
          <>
            <AdminFilterButton
              type="button"
              variant="primary"
              className="min-h-11 w-full text-xs sm:w-auto"
              disabled={actionId === item.id}
              onClick={() => onAction?.(item.id, "approve")}
            >
              Duyệt
            </AdminFilterButton>
            <AdminFilterButton
              type="button"
              variant="secondary"
              className="min-h-11 w-full border-admin-danger/30 text-admin-danger hover:bg-admin-danger-soft sm:w-auto"
              disabled={actionId === item.id}
              onClick={() => onAction?.(item.id, "reject")}
            >
              Từ chối
            </AdminFilterButton>
          </>
        ) : null}
        {item.status === "APPROVED" ? (
          <AdminFilterButton
            type="button"
            variant="primary"
            className="min-h-11 w-full text-xs sm:w-auto"
            disabled={actionId === item.id}
            onClick={() => onAction?.(item.id, "mark-paid")}
          >
            Đã chuyển
          </AdminFilterButton>
        ) : null}
      </div>
    </AdminMobileCard>
  );
}

export function PayoutQueueTable({ items, actionId, onAction }) {
  if (!items?.length) {
    return null;
  }

  return (
    <>
      <AdminMobileCardList className="p-4 md:hidden">
        {items.map((item) => (
          <PayoutQueueMobileCard
            key={item.id}
            item={item}
            actionId={actionId}
            onAction={onAction}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="880px" ariaLabel="Hàng đợi rút tiền">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Thời gian</AdminDataTableCell>
            <AdminDataTableCell header>Seller</AdminDataTableCell>
            <AdminDataTableCell header>Số tiền</AdminDataTableCell>
            <AdminDataTableCell header className="hidden md:table-cell">
              Tài khoản
            </AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header className="text-right">
              Thao tác
            </AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => (
            <AdminDataTableRow key={item.id}>
              <AdminDataTableCell className="py-3 text-admin-text-secondary">
                {item.requestedAt ? formatDateTime(item.requestedAt) : "—"}
              </AdminDataTableCell>
              <AdminDataTableCell className="py-3">
                <span className="break-all font-mono text-xs" title={item.sellerId}>
                  {item.sellerId}
                </span>
              </AdminDataTableCell>
              <AdminDataTableCell
                className="py-3 font-medium tabular-nums"
                title={formatVndPrice(item.amount)}
              >
                {formatVndPrice(item.amount)}
              </AdminDataTableCell>
              <AdminDataTableCell className="hidden py-3 md:table-cell">
                <p className="text-sm text-admin-text">{item.bankName}</p>
                <p className="mt-0.5 break-all text-xs text-admin-text-secondary">
                  {item.bankAccountName} · {item.bankAccountNumber}
                </p>
              </AdminDataTableCell>
              <AdminDataTableCell className="py-3">
                <PayoutStatusBadge status={item.status} />
              </AdminDataTableCell>
              <AdminDataTableCell className="py-3">
                <div className="flex flex-wrap justify-end gap-2">
                  {item.status === "REQUESTED" ? (
                    <>
                      <AdminFilterButton
                        type="button"
                        variant="primary"
                        className="min-h-11 px-2.5 py-1 text-xs"
                        disabled={actionId === item.id}
                        onClick={() => onAction?.(item.id, "approve")}
                      >
                        Duyệt
                      </AdminFilterButton>
                      <AdminFilterButton
                        type="button"
                        variant="secondary"
                        className="min-h-11 border-admin-danger/30 px-2.5 py-1 text-xs text-admin-danger hover:bg-admin-danger-soft"
                        disabled={actionId === item.id}
                        onClick={() => onAction?.(item.id, "reject")}
                      >
                        Từ chối
                      </AdminFilterButton>
                    </>
                  ) : null}
                  {item.status === "APPROVED" ? (
                    <AdminFilterButton
                      type="button"
                      variant="primary"
                      className="min-h-11 px-2.5 py-1 text-xs"
                      disabled={actionId === item.id}
                      onClick={() => onAction?.(item.id, "mark-paid")}
                    >
                      Đã chuyển
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
