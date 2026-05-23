package com.twohands.admin_service.application.audit;

import com.twohands.admin_service.domain.audit.CriticalPayloadBuilder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DefaultCriticalPayloadBuilder implements CriticalPayloadBuilder {

	@Override
	public Map<String, Object> buildRequestPayload(
			String summary,
			Map<String, Object> before,
			Map<String, Object> after,
			Map<String, Object> additionalContext
	) {
		Map<String, Object> payload = new LinkedHashMap<>();
		if (summary != null && !summary.isBlank()) {
			payload.put("summary", summary.trim());
		}
		if (before != null && !before.isEmpty()) {
			payload.put("before", before);
		}
		if (after != null && !after.isEmpty()) {
			payload.put("after", after);
		}
		if (additionalContext != null && !additionalContext.isEmpty()) {
			payload.put("context", additionalContext);
		}
		return payload;
	}

	@Override
	public Map<String, Object> buildResponsePayload(Map<String, Object> resultSummary) {
		if (resultSummary == null || resultSummary.isEmpty()) {
			return Map.of();
		}
		return Map.of("result_summary", resultSummary);
	}
}
