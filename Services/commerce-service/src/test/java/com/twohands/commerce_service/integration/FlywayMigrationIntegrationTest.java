package com.twohands.commerce_service.integration;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationIntegrationTest {

    @Test
    void migrationShouldDefineCommerceOutboxSchema() throws IOException {
        String sql = new ClassPathResource("db/migration/V1__init_commerce_tables.sql")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(sql).contains("CREATE TABLE outbox_events");
        assertThat(sql).contains("event_key VARCHAR(255) NOT NULL UNIQUE");
        assertThat(sql).contains("source VARCHAR(100) NOT NULL");
        assertThat(sql).contains("CREATE TABLE orders");
        assertThat(sql).contains("CREATE TABLE product_inventories");
        assertThat(sql).contains("CREATE TYPE outbox_status AS ENUM");
    }
}
