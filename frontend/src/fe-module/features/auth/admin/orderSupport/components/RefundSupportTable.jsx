import { formatPostListDateTime } from "../../contentModeration/utils/postDateTimeDisplay.js";
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
import { formatPaymentMethodLabel } from "../utils/orderSupportDisplayUtils.js";
import { formatRefundRequestedByLabel } from "../utils/refundSupportFilterHelpers.js";
import { OrderSupportUuidCell } from "./OrderSupportUuidCell.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

function RequestedAtCell({ value }) {
  const formatted = formatPostListDateTime(value);
  return (
    <div className="text-xs tabular-nums text-admin-text-secondary">
      <div>{formatted.date}</div>
      <div className="text-admin-text-muted">{formatted.time}</div>
    </div>
  );
}

function RefundMobileCard({
  item,
  selected,
  actionId,
  canApproveRefund,
  onRefundSelect,
  onConfirm,
  onReject,
  formatVndPrice,
  onCopied,
}) {
  const formatted = formatPostListDateTime(item.requestedAt);
  const isRequested = item.status === "REQUESTED";

  return (
    <AdminMobileCard
      isSelected={selected}
      onClick={() => onRefundSelect?.(item.id)}
      ariaLabel={`Chọn yêu cầu hoàn tiền ${item.id}`}
    >
      <OrderSupportUuidCell value={item.id} onCopied={onCopied} />
      <p className="mt-1 text-sm font-medium tabular-nums text-admin-text">
        {formatVndPrice(item.amount)}
      </p>
      <div className="mt-2 flex flex-wrap items-center gap-2">
        <SupportStatusBadge status={item.status} kind="refund" />
        <span className="text-xs text-admin-text-muted">
          {formatPaymentMethodLabel(item.paymentMethod)}
        </span>
      </div>
      <p className="mt-2 text-xs text-admin-text-secondary">
        {formatRefundRequestedByLabel(item.requestedBy)} · {formatted.date} {formatted.time}
      </p>
      {isRequested && canApproveRefund ? (
        <div className="mt-3 flex flex-col gap-2 sm:flex-row sm:flex-wrap">
          <AdminFilterButton
            type="button"
            variant="primary"
            className="w-full sm:w-auto"
            disabled={actionId === item.id}
            onClick={(event) => {
              event.stopPropagation();
              onConfirm?.(item.id);
            }}
          >
            Xác nhận
          </AdminFilterButton>
          <AdminFilterButton
            type="button"
            variant="secondary"
            className="w-full sm:w-auto"
            disabled={actionId === item.id}
            onClick={(event) => {
              event.stopPropagation();
              onReject?.(item.id);
            }}
          >
            Từ chối
          </AdminFilterButton>
        </div>
      ) : null}
    </AdminMobileCard>
  );
}

export function RefundSupportTable({
  items,
  selectedRefundId,
  actionId,
  canApproveRefund,
  onRefundSelect,
  onConfirm,
  onReject,
  formatVndPrice,
  onCopied,
}) {
  if (!items?.length) {
    return null;
  }

  return (
    <>
      <AdminMobileCardList className="md:hidden">
        {items.map((item) => (
          <RefundMobileCard
            key={item.id}
            item={item}
            selected={item.id === selectedRefundId}
            actionId={actionId}
            canApproveRefund={canApproveRefund}
            onRefundSelect={onRefundSelect}
            onConfirm={onConfirm}
            onReject={onReject}
            formatVndPrice={formatVndPrice}
            onCopied={onCopied}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable
        minWidth="0"
        tableLayout="auto"
        ariaLabel="Danh sách duyệt hoàn tiền"
        className="w-full"
      >
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header className="whitespace-nowrap px-4 py-3">
              Thời gian
            </AdminDataTableCell>
            <AdminDataTableCell header className="px-4 py-3">
              Refund ID
            </AdminDataTableCell>
            <AdminDataTableCell header className="px-4 py-3">
              Order ID
            </AdminDataTableCell>
            <AdminDataTableCell header className="px-4 py-3">
              Payment ID
            </AdminDataTableCell>
            <AdminDataTableCell header className="px-4 py-3">
              Người yêu cầu
            </AdminDataTableCell>
            <AdminDataTableCell header className="px-4 py-3">
              Số tiền
            </AdminDataTableCell>
            <AdminDataTableCell header className="px-4 py-3">
              Thanh toán
            </AdminDataTableCell>
            <AdminDataTableCell header className="px-4 py-3">
              Trạng thái
            </AdminDataTableCell>
            {canApproveRefund ? (
              <AdminDataTableCell header className="px-4 py-3">
                Thao tác
              </AdminDataTableCell>
            ) : null}
            <AdminDataTableCell header className="w-12 px-2" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {items.map((item) => {
            const selected = item.id === selectedRefundId;
            const isRequested = item.status === "REQUESTED";

            return (
              <AdminDataTableRow
                key={item.id}
                isSelected={selected}
                onClick={() => onRefundSelect?.(item.id)}
                className="cursor-pointer"
              >
                <AdminDataTableCell className="px-4 py-3">
                  <RequestedAtCell value={item.requestedAt} />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3" onClick={(event) => event.stopPropagation()}>
                  <OrderSupportUuidCell value={item.id} onCopied={onCopied} />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3" onClick={(event) => event.stopPropagation()}>
                  <OrderSupportUuidCell value={item.orderId} onCopied={onCopied} />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3" onClick={(event) => event.stopPropagation()}>
                  <OrderSupportUuidCell value={item.paymentId} onCopied={onCopied} />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3 text-sm text-admin-text-secondary">
                  {formatRefundRequestedByLabel(item.requestedBy)}
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3 text-sm font-medium tabular-nums text-admin-text">
                  {formatVndPrice(item.amount)}
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3 text-sm text-admin-text-secondary">
                  {formatPaymentMethodLabel(item.paymentMethod)}
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3">
                  <SupportStatusBadge status={item.status} kind="refund" />
                </AdminDataTableCell>
                {canApproveRefund ? (
                  <AdminDataTableCell className="px-4 py-3" onClick={(event) => event.stopPropagation()}>
                    {isRequested ? (
                      <div className="flex flex-wrap gap-2">
                        <AdminFilterButton
                          type="button"
                          variant="primary"
                          disabled={actionId === item.id}
                          onClick={() => onConfirm?.(item.id)}
                        >
                          Xác nhận
                        </AdminFilterButton>
                        <AdminFilterButton
                          type="button"
                          variant="secondary"
                          disabled={actionId === item.id}
                          onClick={() => onReject?.(item.id)}
                        >
                          Từ chối
                        </AdminFilterButton>
                      </div>
                    ) : (
                      <span className="text-xs text-admin-text-muted">—</span>
                    )}
                  </AdminDataTableCell>
                ) : null}
                <AdminDataTableCell className="px-2 py-3 text-admin-text-muted">
                  <span className="material-symbols-outlined text-[20px]" aria-hidden="true">
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
