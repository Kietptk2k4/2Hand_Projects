export function mapActiveRefundRequest(refund) {
  if (!refund) return null;
  return {
    refundRequestId: refund.refund_request_id ?? refund.refundRequestId,
    status: refund.status,
    requestedBy: refund.requested_by ?? refund.requestedBy,
    amount: refund.amount,
    reason: refund.reason,
    requestedAt: refund.requested_at ?? refund.requestedAt,
  };
}
