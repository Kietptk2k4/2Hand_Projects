package com.twohands.admin_service.delivery.http.moderation;

import com.twohands.admin_service.application.moderation.moderatecomment.ModerateCommentCommand;
import com.twohands.admin_service.application.moderation.moderatecomment.ModerateCommentResult;
import com.twohands.admin_service.application.moderation.moderatecomment.ModerateCommentUseCase;
import com.twohands.admin_service.application.moderation.restorecomment.RestoreCommentCommand;
import com.twohands.admin_service.application.moderation.restorecomment.RestoreCommentResult;
import com.twohands.admin_service.application.moderation.restorecomment.RestoreCommentUseCase;
import com.twohands.admin_service.application.moderation.viewcommenthistory.ViewCommentModerationHistoryQuery;
import com.twohands.admin_service.application.moderation.viewcommenthistory.ViewCommentModerationHistoryResult;
import com.twohands.admin_service.application.moderation.viewcommenthistory.ViewCommentModerationHistoryUseCase;
import com.twohands.admin_service.common.dto.ApiResponse;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.moderation.ContentModerationAction;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.security.annotation.RequireAdminPermission;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/v1/social/comments")
public class CommentModerationController {

	private final ModerateCommentUseCase moderateCommentUseCase;
	private final RestoreCommentUseCase restoreCommentUseCase;
	private final ViewCommentModerationHistoryUseCase viewCommentModerationHistoryUseCase;

	public CommentModerationController(
			ModerateCommentUseCase moderateCommentUseCase,
			RestoreCommentUseCase restoreCommentUseCase,
			ViewCommentModerationHistoryUseCase viewCommentModerationHistoryUseCase
	) {
		this.moderateCommentUseCase = moderateCommentUseCase;
		this.restoreCommentUseCase = restoreCommentUseCase;
		this.viewCommentModerationHistoryUseCase = viewCommentModerationHistoryUseCase;
	}

	@GetMapping("/{commentId}/moderation-history")
	@RequireAdminPermission(AdminPermission.COMMENT_MODERATION_READ)
	public ResponseEntity<ApiResponse<ViewCommentModerationHistoryResponse>> viewModerationHistory(
			@PathVariable String commentId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		ViewCommentModerationHistoryResult result = viewCommentModerationHistoryUseCase.execute(
				new ViewCommentModerationHistoryQuery(commentId, page, size)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewCommentModerationHistoryUseCase.successMessage(),
				toHistoryResponse(result)
		));
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

	@PostMapping("/{commentId}/restore")
	@RequireAdminPermission({AdminPermission.COMMENT_RESTORE, AdminPermission.COMMENT_MODERATE})
	public ResponseEntity<ApiResponse<RestoreCommentResponse>> restore(
			@PathVariable String commentId,
			@Valid @RequestBody RestoreCommentRequest request
	) {
		RestoreCommentResult result = restoreCommentUseCase.execute(new RestoreCommentCommand(
				commentId,
				request.reason(),
				request.note()
		));

		RestoreCommentResponse data = new RestoreCommentResponse(
				result.commentId(),
				result.moderationLogId(),
				result.reason(),
				result.note(),
				result.restoredBy(),
				result.restoredAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), restoreCommentUseCase.successMessage(), data));
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

	private ViewCommentModerationHistoryResponse toHistoryResponse(ViewCommentModerationHistoryResult result) {
		return new ViewCommentModerationHistoryResponse(
				result.commentId(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages(),
				result.history().stream()
						.map(item -> new CommentModerationHistoryEntryResponse(
								item.moderationLogId(),
								item.action().name(),
								item.reason(),
								item.note(),
								item.adminId(),
								item.createdAt()
						))
						.toList()
		);
	}
}
