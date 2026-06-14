package com.twohands.commerce_service.infrastructure.vnpay;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class VnpayParamSigner {

    public String sign(SortedMap<String, String> params, String hashSecret) {
        return hmacSha512(hashSecret, buildSignData(params));
    }

    public boolean verify(Map<String, String> params, String secureHash, String hashSecret) {
        if (secureHash == null || secureHash.isBlank()) {
            return false;
        }
        SortedMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) {
                continue;
            }
            String value = entry.getValue();
            if (value != null && !value.isBlank()) {
                sorted.put(key, value);
            }
        }
        String expected = sign(sorted, hashSecret);
        return secureHash.equalsIgnoreCase(expected);
    }

    public String buildSignData(SortedMap<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append(entry.getKey())
                    .append('=')
                    .append(encodeURIComponent(entry.getValue()));
        }
        return builder.toString();
    }

    public String buildQueryString(SortedMap<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append(encodeURIComponent(entry.getKey()))
                    .append('=')
                    .append(encodeURIComponent(entry.getValue()));
        }
        return builder.toString();
    }

    private String hmacSha512(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot generate VNPay signature", ex);
        }
    }

    private String encodeURIComponent(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%21", "!")
                .replace("%27", "'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%7E", "~");
    }
}
