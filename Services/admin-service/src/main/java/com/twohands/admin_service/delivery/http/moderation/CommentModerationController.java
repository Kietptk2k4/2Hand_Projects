package com.twohands.admin_service.delivery.http.moderation;

import com.twohands.admin_service.application.moderation.moderatecomment.ModerateCommentCommand;
import com.twohands.admin_service.application.moderation.moderatecomment.ModerateCommentResult;
import com.twohands.admin_service.application.moderation.moderatecomment.ModerateCommentUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1/social/comments")
public class CommentModerationController {

	private final ModerateCommentUseCase moderateCommentUseCase;

	public CommentModerationController(ModerateCommentUseCase moderateCommentUseCase) {
		this.moderateCommentUseCase = moderateCommentUseCase;
	}

	@PostMapping("/{commentId}/moderate")
	@RequireAdminPermission(AdminPermission.COMMENT_MODERATE)
	public ResponseEntity<ApiResponse<ModerateCommentResponse>> moderate(
			@PathVariable String commentId,
			@Valid @RequestBody ModerateCommentRequest request
	) {
		ContentModerationAction action = parseAction(request.action());

		ModerateCommentResult result = moderateCommentUseCase.execute(new ModerateCommentCommand(
				commentId,
				action,
				request.reason(),
				request.note()
		));

		ModerateCommentResponse data = new ModerateCommentResponse(
				result.commentId(),
				result.action().name(),
				result.moderationLogId(),
				result.reason(),
				result.note(),
				result.moderatedBy(),
				result.moderatedAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(
						HttpStatus.OK.value(),
						moderateCommentUseCase.successMessage(action),
						data
				));
	}

	private ContentModerationAction parseAction(String action) {
		if (action == null || action.isBlank()) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					ErrorCode.VALIDATION_ERROR.defaultMessage(),
					"action",
					"Action is required"
			);
		}
		try {
			return ContentModerationAction.valueOf(action.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new AppException(
					ErrorCode.VALIDATION_ERROR,
					ErrorCode.VALIDATION_ERROR.defaultMessage(),
					"action",
					"Action must be HIDE or REMOVE"
			);
		}
	}
}
