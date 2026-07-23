package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.application.support.exportwebhooklogs.ExportWebhookLogsForSupportUseCase;
import com.twohands.commerce_service.application.support.viewwebhooklogdetail.ViewWebhookLogDetailForSupportUseCase;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportQuery;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportResult;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportStatsQuery;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportStatsResult;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportStatsUseCase;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.delivery.http.support.ViewWebhookLogsForSupportResponse;
import com.twohands.commerce_service.delivery.http.support.ViewWebhookLogsForSupportStatsResponse;
import com.twohands.commerce_service.domain.support.WebhookLogSupportEntry;
import com.twohands.commerce_service.security.AuthenticatedUser;
import com.twohands.commerce_service.security.CommerceAdminAuthorization;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/admin/support/webhook-logs")
public class AdminWebhookSupportController {

    private final ViewWebhookLogsForSupportUseCase viewWebhookLogsForSupportUseCase;
    private final ViewWebhookLogsForSupportStatsUseCase viewWebhookLogsForSupportStatsUseCase;
    private final ViewWebhookLogDetailForSupportUseCase viewWebhookLogDetailForSupportUseCase;
    private final ExportWebhookLogsForSupportUseCase exportWebhookLogsForSupportUseCase;
    private final CommerceAdminAuthorization commerceAdminAuthorization;

    public AdminWebhookSupportController(
            ViewWebhookLogsForSupportUseCase viewWebhookLogsForSupportUseCase,
            ViewWebhookLogsForSupportStatsUseCase viewWebhookLogsForSupportStatsUseCase,
            ViewWebhookLogDetailForSupportUseCase viewWebhookLogDetailForSupportUseCase,
            ExportWebhookLogsForSupportUseCase exportWebhookLogsForSupportUseCase,
            CommerceAdminAuthorization commerceAdminAuthorization
    ) {
        this.viewWebhookLogsForSupportUseCase = viewWebhookLogsForSupportUseCase;
        this.viewWebhookLogsForSupportStatsUseCase = viewWebhookLogsForSupportStatsUseCase;
        this.viewWebhookLogDetailForSupportUseCase = viewWebhookLogDetailForSupportUseCase;
        this.exportWebhookLogsForSupportUseCase = exportWebhookLogsForSupportUseCase;
        this.commerceAdminAuthorization = commerceAdminAuthorization;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ViewWebhookLogsForSupportResponse>> listWebhookLogs(
            @RequestParam(required = false) String provider,
            @RequestParam(name = "reference_id", required = false) String referenceId,
            @RequestParam(required = false) String q,
            @RequestParam(name = "event_type", required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_WEBHOOK_SUPPORT_READ
        );

        ViewWebhookLogsForSupportResult result = viewWebhookLogsForSupportUseCase.execute(
                new ViewWebhookLogsForSupportQuery(provider, referenceId, q, eventType, status, from, to, page, size)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewWebhookLogsForSupportUseCase.successMessage(),
                ViewWebhookLogsForSupportResponse.from(result)
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ViewWebhookLogsForSupportStatsResponse>> webhookLogStats(
            @RequestParam(required = false) String provider,
            @RequestParam(name = "reference_id", required = false) String referenceId,
            @RequestParam(required = false) String q,
            @RequestParam(name = "event_type", required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_WEBHOOK_SUPPORT_READ
        );

        ViewWebhookLogsForSupportStatsResult result = viewWebhookLogsForSupportStatsUseCase.execute(
                new ViewWebhookLogsForSupportStatsQuery(provider, referenceId, q, eventType, status, from, to)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewWebhookLogsForSupportStatsUseCase.successMessage(),
                ViewWebhookLogsForSupportStatsResponse.from(result)
        ));
    }

    @GetMapping("/{logId}")
    public ResponseEntity<ApiResponse<ViewWebhookLogsForSupportResponse.WebhookLogEntryResponse>> getWebhookLog(
            @PathVariable UUID logId,
            @RequestParam String provider,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_WEBHOOK_SUPPORT_READ
        );

        WebhookLogSupportEntry entry = viewWebhookLogDetailForSupportUseCase.execute(logId, provider);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewWebhookLogDetailForSupportUseCase.successMessage(),
                ViewWebhookLogsForSupportResponse.WebhookLogEntryResponse.from(entry)
        ));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportWebhookLogs(
            @RequestParam(required = false) String provider,
            @RequestParam(name = "reference_id", required = false) String referenceId,
            @RequestParam(required = false) String q,
            @RequestParam(name = "event_type", required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "csv") String format,
            Authentication authentication
    ) {
        AuthenticatedUser admin = resolveAuthenticatedUser(authentication);
        commerceAdminAuthorization.requirePermission(
                admin,
                CommerceAdminAuthorization.PERMISSION_WEBHOOK_SUPPORT_READ
        );

        if (!"csv".equalsIgnoreCase(format)) {
            return ResponseEntity.badRequest().build();
        }

        List<WebhookLogSupportEntry> rows = exportWebhookLogsForSupportUseCase.execute(
                new ViewWebhookLogsForSupportQuery(provider, referenceId, q, eventType, status, from, to, null, null)
        );

        String csv = buildCsv(rows);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"webhook-logs.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    private String buildCsv(List<WebhookLogSupportEntry> rows) {
        StringBuilder builder = new StringBuilder();
        builder.append("log_id,provider,reference_id,event_type,processing_status,signature_valid,idempotency_key,received_at,payment_id,shipment_id,order_id\n");
        for (WebhookLogSupportEntry row : rows) {
            builder.append(csv(row.logId()))
                    .append(',').append(csv(row.provider()))
                    .append(',').append(csv(row.referenceId()))
                    .append(',').append(csv(row.eventType()))
                    .append(',').append(csv(row.processingStatus()))
                    .append(',').append(row.signatureValid() == null ? "" : row.signatureValid())
                    .append(',').append(csv(row.idempotencyKey()))
                    .append(',').append(csv(row.receivedAt()))
                    .append(',').append(csv(row.paymentId()))
                    .append(',').append(csv(row.shipmentId()))
                    .append(',').append(csv(row.orderId()))
                    .append('\n');
        }
        return builder.toString();
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private AuthenticatedUser resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Authenticated admin user is required");
        }
        return user;
    }
}
