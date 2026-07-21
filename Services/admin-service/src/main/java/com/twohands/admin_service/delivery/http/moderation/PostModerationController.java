package com.twohands.admin_service.delivery.http.moderation;

import com.twohands.admin_service.application.moderation.moderatepost.ModeratePostCommand;
import com.twohands.admin_service.application.moderation.moderatepost.ModeratePostResult;
import com.twohands.admin_service.application.moderation.moderatepost.ModeratePostUseCase;
import com.twohands.admin_service.application.moderation.restorepost.RestorePostCommand;
import com.twohands.admin_service.application.moderation.restorepost.RestorePostResult;
import com.twohands.admin_service.application.moderation.restorepost.RestorePostUseCase;
import com.twohands.admin_service.application.moderation.viewposthistory.ViewPostModerationHistoryQuery;
import com.twohands.admin_service.application.moderation.viewposthistory.ViewPostModerationHistoryResult;
import com.twohands.admin_service.application.moderation.viewposthistory.ViewPostModerationHistoryUseCase;
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
@RequestMapping("/admin/api/v1/social/posts")
public class PostModerationController {

	private final ModeratePostUseCase moderatePostUseCase;
	private final RestorePostUseCase restorePostUseCase;
	private final ViewPostModerationHistoryUseCase viewPostModerationHistoryUseCase;

	public PostModerationController(
			ModeratePostUseCase moderatePostUseCase,
			RestorePostUseCase restorePostUseCase,
			ViewPostModerationHistoryUseCase viewPostModerationHistoryUseCase
	) {
		this.moderatePostUseCase = moderatePostUseCase;
		this.restorePostUseCase = restorePostUseCase;
		this.viewPostModerationHistoryUseCase = viewPostModerationHistoryUseCase;
	}

	@GetMapping("/{postId}/moderation-history")
	@RequireAdminPermission(AdminPermission.POST_MODERATION_READ)
	public ResponseEntity<ApiResponse<ViewPostModerationHistoryResponse>> viewModerationHistory(
			@PathVariable String postId,
			@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size
	) {
		ViewPostModerationHistoryResult result = viewPostModerationHistoryUseCase.execute(
				new ViewPostModerationHistoryQuery(postId, page, size)
		);

		return ResponseEntity.ok(ApiResponse.success(
				HttpStatus.OK.value(),
				viewPostModerationHistoryUseCase.successMessage(),
				toHistoryResponse(result)
		));
	}

	@PostMapping("/{postId}/moderate")
	@RequireAdminPermission(AdminPermission.POST_MODERATE)
	public ResponseEntity<ApiResponse<ModeratePostResponse>> moderate(
			@PathVariable String postId,
			@Valid @RequestBody ModeratePostRequest request
	) {
		ContentModerationAction action = parseAction(request.action());

		ModeratePostResult result = moderatePostUseCase.execute(new ModeratePostCommand(
				postId,
				action,
				request.reason(),
				request.note()
		));

		ModeratePostResponse data = new ModeratePostResponse(
				result.postId(),
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
						moderatePostUseCase.successMessage(action),
						data
				));
	}

	@PostMapping("/{postId}/restore")
	@RequireAdminPermission({AdminPermission.POST_RESTORE, AdminPermission.POST_MODERATE})
	public ResponseEntity<ApiResponse<RestorePostResponse>> restore(
			@PathVariable String postId,
			@Valid @RequestBody RestorePostRequest request
	) {
		RestorePostResult result = restorePostUseCase.execute(new RestorePostCommand(
				postId,
				request.reason(),
				request.note()
		));

		RestorePostResponse data = new RestorePostResponse(
				result.postId(),
				result.moderationLogId(),
				result.reason(),
				result.note(),
				result.restoredBy(),
				result.restoredAt(),
				result.outboxEventId()
		);

		return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponse.success(HttpStatus.OK.value(), restorePostUseCase.successMessage(), data));
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

	private ViewPostModerationHistoryResponse toHistoryResponse(ViewPostModerationHistoryResult result) {
		return new ViewPostModerationHistoryResponse(
				result.postId(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages(),
				result.history().stream()
						.map(item -> new PostModerationHistoryEntryResponse(
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
