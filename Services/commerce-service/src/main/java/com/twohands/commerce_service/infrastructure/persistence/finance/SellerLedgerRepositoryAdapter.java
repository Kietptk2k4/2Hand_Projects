package com.twohands.commerce_service.infrastructure.persistence.finance;







import com.twohands.commerce_service.common.pagination.PageQuery;



import com.twohands.commerce_service.domain.finance.OrderItemLedgerSnapshot;



import com.twohands.commerce_service.domain.finance.SellerBalanceSummary;



import com.twohands.commerce_service.domain.finance.SellerLedgerCreditDraft;



import com.twohands.commerce_service.domain.finance.SellerLedgerEntryStatus;



import com.twohands.commerce_service.domain.finance.SellerLedgerEntryType;



import com.twohands.commerce_service.domain.finance.SellerLedgerListEntry;



import com.twohands.commerce_service.domain.finance.SellerLedgerRepository;



import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;



import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;



import org.springframework.stereotype.Repository;







import java.math.BigDecimal;

import java.time.Instant;



import java.sql.ResultSet;



import java.sql.SQLException;



import java.sql.Timestamp;



import java.util.List;



import java.util.UUID;







@Repository



public class SellerLedgerRepositoryAdapter implements SellerLedgerRepository {







    private final NamedParameterJdbcTemplate jdbcTemplate;







    public SellerLedgerRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {



        this.jdbcTemplate = jdbcTemplate;



    }







    @Override



    public List<OrderItemLedgerSnapshot> findEligibleCreditSnapshots(List<UUID> orderItemIds) {



        if (orderItemIds == null || orderItemIds.isEmpty()) {



            return List.of();



        }







        String sql = """



                SELECT oi.id AS order_item_id,



                       oi.seller_id,



                       oi.final_price



                FROM order_items oi



                INNER JOIN orders o ON o.id = oi.order_id



                INNER JOIN payments p ON p.order_id = o.id



                WHERE oi.id IN (:orderItemIds)



                  AND oi.status = 'COMPLETED'



                  AND p.status = 'PAID'



                """;



        return jdbcTemplate.query(



                sql,



                new MapSqlParameterSource("orderItemIds", orderItemIds),



                (rs, rowNum) -> new OrderItemLedgerSnapshot(



                        UUID.fromString(rs.getString("order_item_id")),



                        UUID.fromString(rs.getString("seller_id")),



                        rs.getBigDecimal("final_price")



                )



        );



    }







    @Override



    public boolean insertCreditIfAbsent(SellerLedgerCreditDraft draft) {



        String sql = """



                INSERT INTO seller_ledger_entries (



                    id,



                    seller_id,



                    order_item_id,



                    entry_type,



                    gross_amount,



                    platform_fee_amount,



                    net_amount,



                    commission_rate_snapshot,



                    status,



                    created_at



                ) VALUES (



                    :id,



                    :sellerId,



                    :orderItemId,



                    'CREDIT'::seller_ledger_entry_type,



                    :grossAmount,



                    :platformFeeAmount,



                    :netAmount,



                    :commissionRateSnapshot,



                    'POSTED'::seller_ledger_entry_status,



                    :createdAt



                )



                ON CONFLICT (order_item_id) DO NOTHING



                """;



        int inserted = jdbcTemplate.update(sql, new MapSqlParameterSource()



                .addValue("id", UUID.randomUUID())



                .addValue("sellerId", draft.sellerId())



                .addValue("orderItemId", draft.orderItemId())



                .addValue("grossAmount", draft.amounts().grossAmount())



                .addValue("platformFeeAmount", draft.amounts().platformFeeAmount())



                .addValue("netAmount", draft.amounts().netAmount())



                .addValue("commissionRateSnapshot", draft.amounts().commissionRateSnapshot())



                .addValue("createdAt", Timestamp.from(draft.createdAt())));



        return inserted > 0;



    }







    @Override



    public boolean insertDebitForPayout(UUID sellerId, UUID payoutRequestId, BigDecimal amount, Instant createdAt) {



        String sql = """



                INSERT INTO seller_ledger_entries (



                    id,



                    seller_id,



                    order_item_id,



                    payout_request_id,



                    entry_type,



                    gross_amount,



                    platform_fee_amount,



                    net_amount,



                    commission_rate_snapshot,



                    status,



                    created_at



                ) VALUES (



                    :id,



                    :sellerId,



                    NULL,



                    :payoutRequestId,



                    'DEBIT'::seller_ledger_entry_type,



                    :amount,



                    0,



                    :amount,



                    0,



                    'POSTED'::seller_ledger_entry_status,



                    :createdAt



                )



                ON CONFLICT (payout_request_id) DO NOTHING



                """;



        int inserted = jdbcTemplate.update(sql, new MapSqlParameterSource()



                .addValue("id", UUID.randomUUID())



                .addValue("sellerId", sellerId)



                .addValue("payoutRequestId", payoutRequestId)



                .addValue("amount", amount)



                .addValue("createdAt", Timestamp.from(createdAt)));



        return inserted > 0;



    }







    @Override



    public SellerBalanceSummary findBalanceSummary(UUID sellerId) {



        String sql = """



                SELECT COALESCE((



                           SELECT SUM(net_amount)



                           FROM seller_ledger_entries



                           WHERE seller_id = :sellerId



                             AND entry_type = 'CREDIT'



                             AND status = 'POSTED'



                       ), 0) AS total_net_credited,



                       COALESCE((



                           SELECT SUM(platform_fee_amount)



                           FROM seller_ledger_entries



                           WHERE seller_id = :sellerId



                             AND entry_type = 'CREDIT'



                             AND status = 'POSTED'



                       ), 0) AS total_platform_fee,



                       COALESCE((



                           SELECT SUM(net_amount)



                           FROM seller_ledger_entries



                           WHERE seller_id = :sellerId



                             AND entry_type = 'DEBIT'



                             AND status = 'POSTED'



                       ), 0) AS total_debited,



                       COALESCE((



                           SELECT SUM(amount)



                           FROM seller_payout_requests



                           WHERE seller_id = :sellerId



                             AND status IN ('REQUESTED', 'APPROVED')



                       ), 0) AS pending_payout_amount,



                       COALESCE((



                           SELECT COUNT(*)



                           FROM seller_ledger_entries



                           WHERE seller_id = :sellerId



                             AND entry_type = 'CREDIT'



                             AND status = 'POSTED'



                       ), 0) AS credit_entry_count



                """;



        return jdbcTemplate.queryForObject(



                sql,



                new MapSqlParameterSource("sellerId", sellerId),



                (rs, rowNum) -> {



                    BigDecimal totalNetCredited = rs.getBigDecimal("total_net_credited");



                    BigDecimal totalDebited = rs.getBigDecimal("total_debited");



                    BigDecimal pendingPayout = rs.getBigDecimal("pending_payout_amount");



                    BigDecimal available = totalNetCredited.subtract(totalDebited).subtract(pendingPayout);



                    return new SellerBalanceSummary(



                            available,



                            rs.getBigDecimal("total_platform_fee"),



                            totalNetCredited,



                            pendingPayout,



                            rs.getLong("credit_entry_count")



                    );



                }



        );



    }







    @Override



    public long countLedgerEntries(UUID sellerId) {



        Long count = jdbcTemplate.queryForObject(



                """



                        SELECT COUNT(*)



                        FROM seller_ledger_entries



                        WHERE seller_id = :sellerId



                        """,



                new MapSqlParameterSource("sellerId", sellerId),



                Long.class



        );



        return count == null ? 0L : count;



    }







    @Override



    public List<SellerLedgerListEntry> findLedgerEntries(UUID sellerId, PageQuery pageQuery) {



        String sql = """



                SELECT id,



                       order_item_id,



                       entry_type::text AS entry_type,



                       gross_amount,



                       platform_fee_amount,



                       net_amount,



                       commission_rate_snapshot,



                       status::text AS status,



                       created_at



                FROM seller_ledger_entries



                WHERE seller_id = :sellerId



                ORDER BY created_at DESC, id DESC



                LIMIT :limit OFFSET :offset



                """;



        return jdbcTemplate.query(



                sql,



                new MapSqlParameterSource()



                        .addValue("sellerId", sellerId)



                        .addValue("limit", pageQuery.limit())



                        .addValue("offset", pageQuery.offset()),



                this::mapLedgerEntry



        );



    }







    private SellerLedgerListEntry mapLedgerEntry(ResultSet rs, int rowNum) throws SQLException {



        return new SellerLedgerListEntry(



                UUID.fromString(rs.getString("id")),



                UUID.fromString(rs.getString("order_item_id")),



                SellerLedgerEntryType.valueOf(rs.getString("entry_type")),



                rs.getBigDecimal("gross_amount"),



                rs.getBigDecimal("platform_fee_amount"),



                rs.getBigDecimal("net_amount"),



                rs.getBigDecimal("commission_rate_snapshot"),



                SellerLedgerEntryStatus.valueOf(rs.getString("status")),



                rs.getTimestamp("created_at").toInstant()



        );



    }



}



