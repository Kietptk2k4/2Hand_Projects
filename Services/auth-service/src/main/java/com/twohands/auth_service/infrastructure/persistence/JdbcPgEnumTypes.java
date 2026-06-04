package com.twohands.auth_service.infrastructure.persistence;

/**
 * PostgreSQL custom enum type names used in {@code CAST(:param AS type_name)} for JDBC bindings.
 */
public final class JdbcPgEnumTypes {

    public static final String USER_STATUS = "user_status";
    public static final String REFRESH_TOKEN_STATUS = "refresh_token_status";
    public static final String VERIFICATION_TOKEN_TYPE = "verification_token_type";
    public static final String OUTBOX_STATUS = "outbox_status";

    private JdbcPgEnumTypes() {
    }
}
