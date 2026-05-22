package com.twohands.commerce_service.infrastructure.payos;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.commerce_service.config.CommerceIntegrationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class PayosWebhookSignatureVerifier {

    private final CommerceIntegrationProperties.Payos payosProperties;
    private final PayosSignatureGenerator signatureGenerator;

    public PayosWebhookSignatureVerifier(
            CommerceIntegrationProperties integrationProperties,
            PayosSignatureGenerator signatureGenerator
    ) {
        this.payosProperties = integrationProperties.getPayos();
        this.signatureGenerator = signatureGenerator;
    }

    public boolean verify(JsonNode webhookBody) {
        if (!payosProperties.isLiveClientConfigured()) {
            return true;
        }

        JsonNode data = webhookBody.get("data");
        JsonNode signatureNode = webhookBody.get("signature");
        if (data == null || data.isNull() || signatureNode == null || signatureNode.isNull()) {
            return false;
        }

        String receivedSignature = signatureNode.asText("");
        if (receivedSignature.isBlank()) {
            return false;
        }

        String expectedSignature = signatureGenerator.sign(
                payosProperties.getChecksumKey(),
                buildSignatureData(data)
        );
        return expectedSignature.equalsIgnoreCase(receivedSignature);
    }

    private String buildSignatureData(JsonNode dataNode) {
        if (dataNode == null || !dataNode.isObject()) {
            return "";
        }
        Map<String, String> sorted = new TreeMap<>();
        dataNode.fields().forEachRemaining(entry -> {
            if (entry.getValue().isValueNode()) {
                sorted.put(entry.getKey(), entry.getValue().asText());
            }
        });
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            parts.add(entry.getKey() + "=" + entry.getValue());
        }
        return String.join("&", parts);
    }
}
