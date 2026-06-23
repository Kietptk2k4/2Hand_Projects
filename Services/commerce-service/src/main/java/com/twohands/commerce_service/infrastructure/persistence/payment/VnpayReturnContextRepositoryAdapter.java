package com.twohands.commerce_service.infrastructure.persistence.payment;

import com.twohands.commerce_service.domain.payment.VnpayReturnContextRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VnpayReturnContextRepositoryAdapter implements VnpayReturnContextRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public VnpayReturnContextRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<String> findFrontendReturnUrlByTxnRef(String txnRef) {
        if (txnRef == null || txnRef.isBlank()) {
            return Optional.empty();
        }

        String sql = """
                SELECT vnpay_frontend_return_url
                FROM payments
                WHERE vnpay_txn_ref = :txnRef
                LIMIT 1
                """;

        List<String> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("txnRef", txnRef.trim()),
                (rs, rowNum) -> rs.getString("vnpay_frontend_return_url")
        );

        return rows.stream()
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
