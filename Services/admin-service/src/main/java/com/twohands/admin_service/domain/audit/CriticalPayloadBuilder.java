package com.twohands.admin_service.domain.audit;

import java.util.Map;

public interface CriticalPayloadBuilder {

	Map<String, Object> buildRequestPayload(
			String summary,
			Map<String, Object> before,
			Map<String, Object> after,
			Map<String, Object> additionalContext
	);

	Map<String, Object> buildResponsePayload(Map<String, Object> resultSummary);
}
