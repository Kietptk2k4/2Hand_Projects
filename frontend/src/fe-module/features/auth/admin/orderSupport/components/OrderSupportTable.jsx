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

function OrderMobileCard({ order, selected, onOrderSelect, formatVndPrice, onCopied }) {
  const formatted = formatPostListDateTime(order.created_at);
  return (
    <AdminMobileCard
      isSelected={selected}
      onClick={() => onOrderSelect?.(order.order_id)}
      ariaLabel={`Chọn đơn hàng ${order.order_id}`}
    >
      <OrderSupportUuidCell value={order.order_id} onCopied={onCopied} />
      <p className="mt-1 text-sm font-medium tabular-nums text-admin-text">
        {formatVndPrice(order.final_amount)}
      </p>
      <div className="mt-2 flex flex-wrap items-center gap-2">
        <SupportStatusBadge status={order.order_status} />
        <SupportStatusBadge status={order.payment_status} kind="payment" />
      </div>
      <p className="mt-2 text-xs text-admin-text-secondary">
        {formatPaymentMethodLabel(order.payment_method)} · {formatted.date} {formatted.time}
      </p>
    </AdminMobileCard>
  );
}

export function OrderSupportTable({
  orders,
  selectedOrderId,
  onOrderSelect,
  formatVndPrice,
  onCopied,
}) {
  if (!orders?.length) {
    return (
      <p className="py-6 text-center text-sm text-admin-text-secondary">
        Không có đơn hàng phù hợp bộ lọc.
      </p>
    );
  }

  return (
    <>
      <AdminMobileCardList className="md:hidden">
        {orders.map((order) => (
          <OrderMobileCard
            key={order.order_id}
            order={order}
            selected={order.order_id === selectedOrderId}
            onOrderSelect={onOrderSelect}
            formatVndPrice={formatVndPrice}
            onCopied={onCopied}
          />
        ))}
      </AdminMobileCardList>

      <AdminDataTable minWidth="960px" ariaLabel="Danh sách đơn hàng hỗ trợ">
        <AdminDataTableHead>
          <AdminDataTableRow>
            <AdminDataTableCell header>Tạo lúc</AdminDataTableCell>
            <AdminDataTableCell header>Mã đơn</AdminDataTableCell>
            <AdminDataTableCell header>Người mua</AdminDataTableCell>
            <AdminDataTableCell header>Phương thức</AdminDataTableCell>
            <AdminDataTableCell header>Số tiền</AdminDataTableCell>
            <AdminDataTableCell header>Trạng thái</AdminDataTableCell>
            <AdminDataTableCell header>Thanh toán</AdminDataTableCell>
            <AdminDataTableCell header className="w-10" />
          </AdminDataTableRow>
        </AdminDataTableHead>
        <AdminDataTableBody>
          {orders.map((order) => {
            const selected = order.order_id === selectedOrderId;
            return (
              <AdminDataTableRow
                key={order.order_id}
                interactive
                selected={selected}
                onClick={() => onOrderSelect?.(order.order_id)}
              >
                <AdminDataTableCell>
                  <CreatedAtCell value={order.created_at} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <OrderSupportUuidCell value={order.order_id} onCopied={onCopied} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <OrderSupportUuidCell value={order.buyer_id} onCopied={onCopied} />
                </AdminDataTableCell>
                <AdminDataTableCell>{formatPaymentMethodLabel(order.payment_method)}</AdminDataTableCell>
                <AdminDataTableCell>
                  <span className="tabular-nums text-admin-text">{formatVndPrice(order.final_amount)}</span>
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <SupportStatusBadge status={order.order_status} />
                </AdminDataTableCell>
                <AdminDataTableCell>
                  <SupportStatusBadge status={order.payment_status} kind="payment" />
                </AdminDataTableCell>
                <AdminDataTableCell>
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
