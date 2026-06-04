package com.twohands.commerce_service.infrastructure.persistence.order;

import com.twohands.commerce_service.domain.order.OrderBuyerRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderBuyerRepositoryAdapter implements OrderBuyerRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OrderBuyerRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UUID> findBuyerIdByOrderId(UUID orderId) {
        String sql = """
                SELECT buyer_id
                FROM orders
                WHERE id = :orderId
                """;
        List<UUID> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> UUID.fromString(rs.getString("buyer_id"))
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }
}
