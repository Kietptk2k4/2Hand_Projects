package com.twohands.admin_service.unit.announcement;

import com.twohands.admin_service.domain.announcement.SystemAnnouncementPolicy;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementSeverity;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SystemAnnouncementPolicyTest {

	@Test
	void shouldAcceptValidCreateRequest() {
		SystemAnnouncementPolicy.validateCreateRequest(
				"Maintenance tonight",
				"We will perform maintenance from 02:00 to 04:00 UTC.",
				SystemAnnouncementSeverity.WARNING
		);
	}

	@Test
	void shouldRejectBlankTitle() {
		assertThatThrownBy(() -> SystemAnnouncementPolicy.validateCreateRequest(
				"   ",
				"content",
				SystemAnnouncementSeverity.INFO
		))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.VALIDATION_ERROR);
	}

	@Test
	void shouldParseSeverityCaseInsensitive() {
		assertThat(SystemAnnouncementPolicy.parseSeverity("warning"))
				.isEqualTo(SystemAnnouncementSeverity.WARNING);
	}

	@Test
	void shouldDefaultDismissibleToTrue() {
		assertThat(SystemAnnouncementPolicy.resolveDismissible(null)).isTrue();
		assertThat(SystemAnnouncementPolicy.resolveDismissible(false)).isFalse();
	}

	@Test
	void shouldRejectPinWhenCancelled() {
		assertThatThrownBy(() -> SystemAnnouncementPolicy.assertPinAllowed(
				com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus.CANCELLED
		))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT);
	}

	@Test
	void shouldRejectPublishWhenNotDraft() {
		assertThatThrownBy(() -> SystemAnnouncementPolicy.assertDraftForPublish(
				com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus.SENT
		))
				.isInstanceOf(AppException.class)
				.extracting(ex -> ((AppException) ex).getErrorCode())
				.isEqualTo(ErrorCode.SYSTEM_ANNOUNCEMENT_CONFLICT);
	}
}
