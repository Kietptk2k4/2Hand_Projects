package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.support.PaymentSupportDetail;
import com.twohands.admin_service.domain.support.PaymentSupportStatusTimelineEntry;
import com.twohands.admin_service.domain.support.PaymentSupportWebhookEvent;

import java.util.List;

final class CommercePaymentSupportDetailMapper {

	private CommercePaymentSupportDetailMapper() {
	}

	static PaymentSupportDetail toDomain(CommercePaymentSupportDetailPayload payload) {
		if (payload == null) {
			return null;
		}
		return new PaymentSupportDetail(
				payload.paymentId(),
				payload.orderId(),
				payload.payerId(),
				payload.paymentMethod(),
				payload.amount(),
				payload.currency(),
				payload.status(),
				payload.paidAt(),
				payload.expiredAt(),
				payload.createdAt(),
				payload.updatedAt(),
				payload.providerOrderCode(),
				payload.providerTransactionId(),
				payload.checkoutUrlAvailable(),
				payload.checkoutUrlExpiredAt(),
				payload.orderStatus(),
				payload.orderPaymentStatus(),
				payload.reconciliationStatus(),
				toTimeline(payload.statusTimeline()),
				toWebhookEvents(payload.webhookEvents())
		);
	}

	private static List<PaymentSupportStatusTimelineEntry> toTimeline(
			List<CommercePaymentSupportDetailPayload.StatusTimelinePayload> timeline
	) {
		if (timeline == null) {
			return List.of();
		}
		return timeline.stream()
				.map(entry -> new PaymentSupportStatusTimelineEntry(
						entry.oldStatus(),
						entry.newStatus(),
						entry.occurredAt()
				))
				.toList();
	}

	private static List<PaymentSupportWebhookEvent> toWebhookEvents(
			List<CommercePaymentSupportDetailPayload.WebhookEventPayload> events
	) {
		if (events == null) {
			return List.of();
		}
		return events.stream()
				.map(event -> new PaymentSupportWebhookEvent(
						event.provider(),
						event.eventType(),
						event.signatureValid(),
						event.processed(),
						event.receivedAt()
				))
				.toList();
	}
}
