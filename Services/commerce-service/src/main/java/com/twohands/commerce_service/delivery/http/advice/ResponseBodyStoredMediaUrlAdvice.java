package com.twohands.commerce_service.delivery.http.advice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.commerce_service.common.media.StoredMediaUrlRewriter;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Map;
import java.util.function.UnaryOperator;

@Slf4j
@RestControllerAdvice
public class ResponseBodyStoredMediaUrlAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;
    private final CommerceObjectStorageProperties objectStorageProperties;

    public ResponseBodyStoredMediaUrlAdvice(
            ObjectMapper objectMapper,
            CommerceObjectStorageProperties objectStorageProperties
    ) {
        this.objectMapper = objectMapper;
        this.objectStorageProperties = objectStorageProperties;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        if (body == null) {
            return null;
        }
        String publicUrl = objectStorageProperties.getPublicUrl();
        if (publicUrl == null || publicUrl.isBlank()) {
            return body;
        }

        UnaryOperator<String> rewriter = url -> StoredMediaUrlRewriter.rewrite(url, publicUrl);
        try {
            JsonNode tree = objectMapper.valueToTree(body);
            rewriteJsonMediaUrls(tree, rewriter);
            return objectMapper.treeToValue(tree, body.getClass());
        } catch (Exception ex) {
            log.debug("Stored media URL rewrite skipped: {}", ex.getMessage());
            return body;
        }
    }

    private static void rewriteJsonMediaUrls(JsonNode node, UnaryOperator<String> rewriter) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            for (Map.Entry<String, JsonNode> field : objectNode.properties()) {
                String fieldName = field.getKey();
                JsonNode value = field.getValue();
                if (value.isTextual()) {
                    String rewritten = rewriter.apply(value.asText());
                    if (!rewritten.equals(value.asText())) {
                        objectNode.put(fieldName, rewritten);
                    }
                } else {
                    rewriteJsonMediaUrls(value, rewriter);
                }
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                rewriteJsonMediaUrls(child, rewriter);
            }
        }
    }
}
