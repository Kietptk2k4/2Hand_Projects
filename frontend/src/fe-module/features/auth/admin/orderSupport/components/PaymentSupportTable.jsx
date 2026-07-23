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
import { formatPaymentMethodLabel } from "../utils/orderSupportDisplayUtils.js";
import { OrderSupportUuidCell } from "./OrderSupportUuidCell.jsx";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

function CreatedAtCell({ value }) {
  const formatted = formatPostListDateTime(value);
  return (
    <div className="text-xs tabular-nums text-admin-text-secondary">
      <div>{formatted.date}</div>
      <div className="text-admin-text-muted">{formatted.time}</div>
    </div>
  );
}

function PaymentMobileCard({ payment, selected, onPaymentSelect, formatVndPrice, onCopied }) {
  const formatted = formatPostListDateTime(payment.created_at);
  return (
    <AdminMobileCard
      isSelected={selected}
      onClick={() => onPaymentSelect?.(payment.payment_id)}
      ariaLabel={`Chọn thanh toán ${payment.payment_id}`}
    >
      <OrderSupportUuidCell value={payment.payment_id} onCopied={onCopied} />
      <p className="mt-1 text-sm font-medium tabular-nums text-admin-text">
        {formatVndPrice(payment.amount)}
      </p>
      <div className="mt-2 flex flex-wrap items-center gap-2">
        <SupportStatusBadge status={payment.status} kind="payment" />
      </div>
      <p className="mt-2 text-xs text-admin-text-secondary">
        {formatPaymentMethodLabel(payment.payment_method)} · {formatted.date} {formatted.time}
      </p>
    </AdminMobileCard>
  );
}

export function PaymentSupportTable({
  payments,
  selectedPaymentId,
  onPaymentSelect,
  formatVndPrice,
  onCopied,
}) {
  if (!payments?.length) {
    return null;
  }

  return (
    <>
      <AdminMobileCardList className="md:hidden">
        {payments.map((payment) => (
          <PaymentMobileCard
            key={payment.payment_id}
            payment={payment}
            selected={payment.payment_id === selectedPaymentId}
            onPaymentSelect={onPaymentSelect}
            formatVndPrice={formatVndPrice}
            onCopied={onCopied}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable
        minWidth="0"
        tableLayout="auto"
        ariaLabel="Danh sách thanh toán hỗ trợ"
        className="w-full"
      >
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header className="w-[10%] whitespace-nowrap px-4 py-3">
              Tạo lúc
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-[18%] px-4 py-3">
              Payment ID
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-[18%] px-4 py-3">
              Order ID
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-[12%] px-4 py-3">
              Phương thức
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-[12%] px-4 py-3">
              Số tiền
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-[12%] px-4 py-3">
              Trạng thái
            </AdminDataTableCell>
            <AdminDataTableCell header className="w-12 px-2" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {payments.map((payment) => {
            const selected = payment.payment_id === selectedPaymentId;
            return (
              <AdminDataTableRow
                key={payment.payment_id}
                interactive
                selected={selected}
                onClick={() => onPaymentSelect?.(payment.payment_id)}
              >
                <AdminDataTableCell className="px-4 py-3.5">
                  <CreatedAtCell value={payment.created_at} />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3.5">
                  <OrderSupportUuidCell value={payment.payment_id} onCopied={onCopied} />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3.5">
                  <OrderSupportUuidCell value={payment.order_id} onCopied={onCopied} />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3.5">
                  {formatPaymentMethodLabel(payment.payment_method)}
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3.5">
                  <span className="tabular-nums text-admin-text">{formatVndPrice(payment.amount)}</span>
                </AdminDataTableCell>
                <AdminDataTableCell className="px-4 py-3.5">
                  <SupportStatusBadge status={payment.status} kind="payment" />
                </AdminDataTableCell>
                <AdminDataTableCell className="px-2">
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
