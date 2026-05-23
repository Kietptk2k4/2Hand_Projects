package com.twohands.admin_service.application.support.viewpaymentdetail;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommercePaymentSupportGateway;
import com.twohands.admin_service.domain.support.PaymentSupportDetail;
import com.twohands.admin_service.domain.support.PaymentSupportDetailPolicy;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ViewPaymentSupportDetailUseCase {

	private static final String SUCCESS_MESSAGE = "Payment support detail retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommercePaymentSupportGateway commercePaymentSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ViewPaymentSupportDetailUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommercePaymentSupportGateway commercePaymentSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commercePaymentSupportGateway = commercePaymentSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public ViewPaymentSupportDetailResult execute(ViewPaymentSupportDetailQuery query) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.PAYMENT_SUPPORT_READ);

		if (!commercePaymentSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; payment support detail is unavailable"
			);
		}

		PaymentSupportDetail rawDetail = commercePaymentSupportGateway.fetchPaymentSupportDetail(
				query.paymentId(),
				query.bearerToken()
		);
		PaymentSupportDetail sanitizedDetail = PaymentSupportDetailPolicy.sanitize(rawDetail);

		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.PAYMENT_SUPPORT_VIEW.name(),
				AdminActionTargetType.PAYMENT,
				query.paymentId().toString(),
				SUCCESS_MESSAGE,
				Map.of("paymentId", query.paymentId().toString()),
				Map.of("reconciliationStatus", sanitizedDetail.reconciliationStatus())
		);

		return new ViewPaymentSupportDetailResult(sanitizedDetail);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}
}
