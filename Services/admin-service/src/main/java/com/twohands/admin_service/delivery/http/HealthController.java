package com.twohands.admin_service.delivery.http;

import com.twohands.admin_service.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/api/v1")
public class HealthController {

	@GetMapping("/health")
	public ResponseEntity<ApiResponse<Map<String, String>>> health() {
		return ResponseEntity.ok(ApiResponse.success(
				200,
				"Admin service is healthy",
				Map.of("service", "admin-service")
		));
	}
}
