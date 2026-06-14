package com.twohands.commerce_service.application.catalog.common;

import java.text.Normalizer;
import java.util.Locale;

public final class CatalogSlugNormalizer {

    private CatalogSlugNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        normalized = normalized
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s_]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return normalized;
    }
}
