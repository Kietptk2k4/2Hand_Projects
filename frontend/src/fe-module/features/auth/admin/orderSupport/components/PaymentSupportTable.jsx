import {
  AdminDataTable,
  AdminDataTableBody,
  AdminDataTableCell,
  AdminDataTableHead,
  AdminDataTableRow,
  AdminMobileCard,
  AdminMobileCardList,
} from "../../components/ui";
import { SupportStatusBadge } from "./SupportStatusBadge.jsx";

export function PaymentSupportTable({
  payments,
  selectedPaymentId,
  onPaymentSelect,
  formatDateTime,
  formatVndPrice,
}) {
  if (!payments?.length) {
    return (
      <p className="py-6 text-center text-sm text-admin-text-secondary">
        Không có thanh toán phù hợp bộ lọc.
      </p>
    );
  }

  return (
    <>
      <AdminMobileCardList>
        {payments.map((payment) => {
          const isSelected = payment.payment_id === selectedPaymentId;
          return (
            <AdminMobileCard
              key={payment.payment_id}
              isSelected={isSelected}
              onClick={() => onPaymentSelect?.(payment.payment_id)}
              ariaLabel={`Chọn thanh toán ${payment.payment_id}`}
            >
              <p className="font-mono text-xs text-admin-text-muted">{payment.payment_id}</p>
              <p className="mt-1 text-sm font-medium text-admin-text">
                {formatVndPrice(payment.amount)}
              </p>
              <div className="mt-2 flex flex-wrap items-center gap-2">
                <SupportStatusBadge status={payment.status} />
                <span className="text-xs text-admin-text-secondary">{payment.payment_method}</span>
              </div>
              <p className="mt-1 text-xs text-admin-text-muted">
                Order {payment.order_id} · {formatDateTime(payment.created_at)}
              </p>
            </AdminMobileCard>
          );
        })}
      </AdminMobileCardList>

      <AdminDataTable minWidth="720px" ariaLabel="Danh sách thanh toán hỗ trợ">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Tạo lúc</AdminDataTableCell>
            <AdminDataTableCell header>Payment ID</AdminDataTableCell>
            <AdminDataTableCell header>Order ID</AdminDataTableCell>
            <AdminDataTableCell header>Phương thức</AdminDataTableCell>
            <AdminDataTableCell header>Số tiền</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {payments.map((payment) => {
            const isSelected = payment.payment_id === selectedPaymentId;
            return (
              <AdminDataTableRow
                key={payment.payment_id}
                isSelected={isSelected}
                onClick={() => onPaymentSelect?.(payment.payment_id)}
              >
                <AdminDataTableCell className="text-sm text-admin-text-secondary">
                  {formatDateTime(payment.created_at)}
                </AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs">{payment.payment_id}</AdminDataTableCell>
                <AdminDataTableCell className="font-mono text-xs">{payment.order_id}</AdminDataTableCell>
                <AdminDataTableCell>{payment.payment_method}</AdminDataTableCell>
                <AdminDataTableCell>{formatVndPrice(payment.amount)}</AdminDataTableCell>
                <AdminDataTableCell>
                  <SupportStatusBadge status={payment.status} />
                </AdminDataTableCell>
              </AdminDataTableRow>
            );
          })}
        </AdminDataTableBody>
      </AdminDataTable>
    </>
  );
}
