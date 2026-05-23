package com.twohands.admin_service.delivery.http.moderation;

import com.twohands.admin_service.application.moderation.hidereview.HideReviewCommand;
import com.twohands.admin_service.application.moderation.hidereview.HideReviewResult;
import com.twohands.admin_service.application.moderation.hidereview.HideReviewUseCase;
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

	public ReviewModerationController(HideReviewUseCase hideReviewUseCase) {
		this.hideReviewUseCase = hideReviewUseCase;
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
}
