package com.twohands.admin_service.delivery.http.moderation;

import com.twohands.admin_service.application.moderation.hidereview.HideReviewCommand;
import com.twohands.admin_service.application.moderation.hidereview.HideReviewResult;
import com.twohands.admin_service.application.moderation.hidereview.HideReviewUseCase;
import com.twohands.admin_service.application.moderation.removereview.RemoveReviewCommand;
import com.twohands.admin_service.application.moderation.removereview.RemoveReviewResult;
import com.twohands.admin_service.application.moderation.removereview.RemoveReviewUseCase;
import com.twohands.admin_service.application.moderation.restorereview.RestoreReviewCommand;
import com.twohands.admin_service.application.moderation.restorereview.RestoreReviewResult;
import com.twohands.admin_service.application.moderation.restorereview.RestoreReviewUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/reviews")
public class ReviewModerationController {

	private final HideReviewUseCase hideReviewUseCase;
	private final RemoveReviewUseCase removeReviewUseCase;
	private final RestoreReviewUseCase restoreReviewUseCase;

	public ReviewModerationController(
			HideReviewUseCase hideReviewUseCase,
			RemoveReviewUseCase removeReviewUseCase,
			RestoreReviewUseCase restoreReviewUseCase
	) {
		this.hideReviewUseCase = hideReviewUseCase;
		this.removeReviewUseCase = removeReviewUseCase;
		this.restoreReviewUseCase = restoreReviewUseCase;
	}

	@PostMapping("/{reviewId}/hide")
	@RequireAdminPermission(AdminPermission.REVIEW_HIDE)
	public ResponseEntity<ApiResponse<HideReviewResponse>> hide(
			@PathVariable UUID reviewId,
			@Valid @RequestBody HideReviewRequest request
	) {
		HideReviewResult result = hideReviewUseCase.execute(new HideReviewCommand(
				reviewId,
				request.reason(),
				request.note()
		));

		HideReviewResponse data = new HideReviewResponse(
				result.reviewId(),
				result.moderationLogId(),
				result.reason(),
				result.note(),
				result.hiddenBy(),
				result.hiddenAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), hideReviewUseCase.successMessage(), data));
	}

	@PostMapping("/{reviewId}/remove")
	@RequireAdminPermission({AdminPermission.REVIEW_REMOVE, AdminPermission.REVIEW_HIDE})
	public ResponseEntity<ApiResponse<RemoveReviewResponse>> remove(
			@PathVariable UUID reviewId,
			@Valid @RequestBody RemoveReviewRequest request
	) {
		RemoveReviewResult result = removeReviewUseCase.execute(new RemoveReviewCommand(
				reviewId,
				request.reason(),
				request.note()
		));

		RemoveReviewResponse data = new RemoveReviewResponse(
				result.reviewId(),
				result.moderationLogId(),
				result.reason(),
				result.note(),
				result.removedBy(),
				result.removedAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), removeReviewUseCase.successMessage(), data));
	}

	@PostMapping("/{reviewId}/restore")
	@RequireAdminPermission({AdminPermission.REVIEW_RESTORE, AdminPermission.REVIEW_HIDE})
	public ResponseEntity<ApiResponse<RestoreReviewResponse>> restore(
			@PathVariable UUID reviewId,
			@Valid @RequestBody RestoreReviewRequest request
	) {
		RestoreReviewResult result = restoreReviewUseCase.execute(new RestoreReviewCommand(
				reviewId,
				request.reason(),
				request.note()
		));

		RestoreReviewResponse data = new RestoreReviewResponse(
				result.reviewId(),
				result.moderationLogId(),
				result.reason(),
				result.note(),
				result.restoredBy(),
				result.restoredAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), restoreReviewUseCase.successMessage(), data));
	}
}
