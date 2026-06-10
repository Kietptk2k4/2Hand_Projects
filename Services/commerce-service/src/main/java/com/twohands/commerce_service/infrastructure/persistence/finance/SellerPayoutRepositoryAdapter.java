package com.twohands.commerce_service.infrastructure.persistence.finance;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.finance.PayoutRequestStatus;
import com.twohands.commerce_service.domain.finance.SellerPayoutAccount;
import com.twohands.commerce_service.domain.finance.SellerPayoutRepository;
import com.twohands.commerce_service.domain.finance.SellerPayoutRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SellerPayoutRepositoryAdapter implements SellerPayoutRepository {

    private static final String PAYOUT_SELECT = """
            SELECT pr.id,
                   pr.seller_id,
                   pr.payout_account_id,
                   pr.amount,
                   pr.status::text AS status,
                   pr.admin_note,
                   pr.bank_transfer_ref,
                   pr.requested_at,
                   pr.approved_at,
                   pr.paid_at,
                   pr.rejected_at,
                   pr.cancelled_at,
                   spa.bank_name,
                   spa.bank_account_name,
                   spa.bank_account_number
            FROM seller_payout_requests pr
            INNER JOIN seller_payout_accounts spa ON spa.id = pr.payout_account_id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SellerPayoutRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<SellerPayoutAccount> findAccountsBySellerId(UUID sellerId) {
        return jdbcTemplate.query(
                """
                        SELECT id, seller_id, bank_name, bank_account_name, bank_account_number,
                               is_default, created_at, updated_at
                        FROM seller_payout_accounts
                        WHERE seller_id = :sellerId
                        ORDER BY is_default DESC, created_at ASC
                        """,
                new MapSqlParameterSource("sellerId", sellerId),
                this::mapAccount
        );
    }

    @Override
    public Optional<SellerPayoutAccount> findAccountById(UUID sellerId, UUID accountId) {
        List<SellerPayoutAccount> rows = jdbcTemplate.query(
                """
                        SELECT id, seller_id, bank_name, bank_account_name, bank_account_number,
                               is_default, created_at, updated_at
                        FROM seller_payout_accounts
                        WHERE seller_id = :sellerId AND id = :accountId
                        """,
                new MapSqlParameterSource()
                        .addValue("sellerId", sellerId)
                        .addValue("accountId", accountId),
                this::mapAccount
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public UUID createAccount(
            UUID sellerId,
            String bankName,
            String bankAccountName,
            String bankAccountNumber,
            boolean isDefault,
            Instant now
    ) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO seller_payout_accounts (
                            id, seller_id, bank_name, bank_account_name, bank_account_number,
                            is_default, created_at, updated_at
                        ) VALUES (
                            :id, :sellerId, :bankName, :bankAccountName, :bankAccountNumber,
                            :isDefault, :now, :now
                        )
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("sellerId", sellerId)
                        .addValue("bankName", bankName)
                        .addValue("bankAccountName", bankAccountName)
                        .addValue("bankAccountNumber", bankAccountNumber)
                        .addValue("isDefault", isDefault)
                        .addValue("now", Timestamp.from(now))
        );
        return id;
    }

    @Override
    public void updateAccount(
            UUID sellerId,
            UUID accountId,
            String bankName,
            String bankAccountName,
            String bankAccountNumber,
            boolean isDefault,
            Instant now
    ) {
        jdbcTemplate.update(
                """
                        UPDATE seller_payout_accounts
                        SET bank_name = :bankName,
                            bank_account_name = :bankAccountName,
                            bank_account_number = :bankAccountNumber,
                            is_default = :isDefault,
                            updated_at = :now
                        WHERE seller_id = :sellerId AND id = :accountId
                        """,
                new MapSqlParameterSource()
                        .addValue("sellerId", sellerId)
                        .addValue("accountId", accountId)
                        .addValue("bankName", bankName)
                        .addValue("bankAccountName", bankAccountName)
                        .addValue("bankAccountNumber", bankAccountNumber)
                        .addValue("isDefault", isDefault)
                        .addValue("now", Timestamp.from(now))
        );
    }

    @Override
    public void clearDefaultAccounts(UUID sellerId, Instant now) {
        jdbcTemplate.update(
                """
                        UPDATE seller_payout_accounts
                        SET is_default = FALSE, updated_at = :now
                        WHERE seller_id = :sellerId AND is_default = TRUE
                        """,
                new MapSqlParameterSource()
                        .addValue("sellerId", sellerId)
                        .addValue("now", Timestamp.from(now))
        );
    }

    @Override
    public BigDecimal sumPendingPayoutAmount(UUID sellerId) {
        BigDecimal amount = jdbcTemplate.queryForObject(
                """
                        SELECT COALESCE(SUM(amount), 0)
                        FROM seller_payout_requests
                        WHERE seller_id = :sellerId
                          AND status IN ('REQUESTED', 'APPROVED')
                        """,
                new MapSqlParameterSource("sellerId", sellerId),
                BigDecimal.class
        );
        return amount == null ? BigDecimal.ZERO : amount;
    }

    @Override
    public long countPayoutRequests(UUID sellerId, Optional<PayoutRequestStatus> status) {
        String sql = "SELECT COUNT(*) FROM seller_payout_requests WHERE seller_id = :sellerId"
                + statusFilter(status, "");
        Long count = jdbcTemplate.queryForObject(sql, payoutParams(sellerId, status), Long.class);
        return count == null ? 0L : count;
    }

    @Override
    public List<SellerPayoutRequest> findPayoutRequestsBySellerId(
            UUID sellerId,
            Optional<PayoutRequestStatus> status,
            PageQuery pageQuery
    ) {
        String sql = PAYOUT_SELECT
                + " WHERE pr.seller_id = :sellerId"
                + statusFilter(status, "pr.")
                + " ORDER BY pr.requested_at DESC, pr.id DESC LIMIT :limit OFFSET :offset";
        MapSqlParameterSource params = payoutParams(sellerId, status)
                .addValue("limit", pageQuery.limit())
                .addValue("offset", pageQuery.offset());
        return jdbcTemplate.query(sql, params, this::mapPayoutRequest);
    }

    @Override
    public long countAdminPayoutRequests(Optional<PayoutRequestStatus> status) {
        String sql = "SELECT COUNT(*) FROM seller_payout_requests WHERE 1=1" + statusFilter(status, "");
        Long count = jdbcTemplate.queryForObject(sql, payoutParams(null, status), Long.class);
        return count == null ? 0L : count;
    }

    @Override
    public List<SellerPayoutRequest> findAdminPayoutRequests(Optional<PayoutRequestStatus> status, PageQuery pageQuery) {
        String sql = PAYOUT_SELECT
                + " WHERE 1=1"
                + statusFilter(status, "pr.")
                + " ORDER BY pr.requested_at DESC, pr.id DESC LIMIT :limit OFFSET :offset";
        MapSqlParameterSource params = payoutParams(null, status)
                .addValue("limit", pageQuery.limit())
                .addValue("offset", pageQuery.offset());
        return jdbcTemplate.query(sql, params, this::mapPayoutRequest);
    }

    @Override
    public Optional<SellerPayoutRequest> findPayoutRequestById(UUID payoutRequestId) {
        List<SellerPayoutRequest> rows = jdbcTemplate.query(
                PAYOUT_SELECT + " WHERE pr.id = :payoutRequestId",
                new MapSqlParameterSource("payoutRequestId", payoutRequestId),
                this::mapPayoutRequest
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public Optional<SellerPayoutRequest> findPayoutRequestForSeller(UUID sellerId, UUID payoutRequestId) {
        List<SellerPayoutRequest> rows = jdbcTemplate.query(
                PAYOUT_SELECT + " WHERE pr.seller_id = :sellerId AND pr.id = :payoutRequestId",
                new MapSqlParameterSource()
                        .addValue("sellerId", sellerId)
                        .addValue("payoutRequestId", payoutRequestId),
                this::mapPayoutRequest
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public UUID createPayoutRequest(UUID sellerId, UUID payoutAccountId, BigDecimal amount, Instant now) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                """
                        INSERT INTO seller_payout_requests (
                            id, seller_id, payout_account_id, amount, status, requested_at, created_at, updated_at
                        ) VALUES (
                            :id, :sellerId, :payoutAccountId, :amount, 'REQUESTED', :now, :now, :now
                        )
                        """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("sellerId", sellerId)
                        .addValue("payoutAccountId", payoutAccountId)
                        .addValue("amount", amount)
                        .addValue("now", Timestamp.from(now))
        );
        return id;
    }

    @Override
    public boolean cancelPayoutRequest(UUID sellerId, UUID payoutRequestId, Instant now) {
        int updated = jdbcTemplate.update(
                """
                        UPDATE seller_payout_requests
                        SET status = 'CANCELLED',
                            cancelled_at = :now,
                            updated_at = :now
                        WHERE id = :payoutRequestId
                          AND seller_id = :sellerId
                          AND status = 'REQUESTED'
                        """,
                new MapSqlParameterSource()
                        .addValue("payoutRequestId", payoutRequestId)
                        .addValue("sellerId", sellerId)
                        .addValue("now", Timestamp.from(now))
        );
        return updated > 0;
    }

    @Override
    public boolean approvePayoutRequest(UUID payoutRequestId, Instant now) {
        int updated = jdbcTemplate.update(
                """
                        UPDATE seller_payout_requests
                        SET status = 'APPROVED',
                            approved_at = :now,
                            updated_at = :now
                        WHERE id = :payoutRequestId AND status = 'REQUESTED'
                        """,
                new MapSqlParameterSource()
                        .addValue("payoutRequestId", payoutRequestId)
                        .addValue("now", Timestamp.from(now))
        );
        return updated > 0;
    }

    @Override
    public boolean rejectPayoutRequest(UUID payoutRequestId, String adminNote, Instant now) {
        int updated = jdbcTemplate.update(
                """
                        UPDATE seller_payout_requests
                        SET status = 'REJECTED',
                            admin_note = :adminNote,
                            rejected_at = :now,
                            updated_at = :now
                        WHERE id = :payoutRequestId AND status = 'REQUESTED'
                        """,
                new MapSqlParameterSource()
                        .addValue("payoutRequestId", payoutRequestId)
                        .addValue("adminNote", adminNote)
                        .addValue("now", Timestamp.from(now))
        );
        return updated > 0;
    }

    @Override
    public boolean markPayoutRequestPaid(UUID payoutRequestId, String bankTransferRef, Instant now) {
        int updated = jdbcTemplate.update(
                """
                        UPDATE seller_payout_requests
                        SET status = 'PAID',
                            bank_transfer_ref = :bankTransferRef,
                            paid_at = :now,
                            updated_at = :now
                        WHERE id = :payoutRequestId AND status = 'APPROVED'
                        """,
                new MapSqlParameterSource()
                        .addValue("payoutRequestId", payoutRequestId)
                        .addValue("bankTransferRef", bankTransferRef)
                        .addValue("now", Timestamp.from(now))
        );
        return updated > 0;
    }

    private String statusFilter(Optional<PayoutRequestStatus> status, String columnPrefix) {
        return status
                .map(payoutRequestStatus -> " AND " + columnPrefix + "status = '" + payoutRequestStatus.name() + "'")
                .orElse("");
    }

    private MapSqlParameterSource payoutParams(UUID sellerId, Optional<PayoutRequestStatus> status) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (sellerId != null) {
            params.addValue("sellerId", sellerId);
        }
        status.ifPresent(value -> params.addValue("status", value.name()));
        return params;
    }

    private SellerPayoutAccount mapAccount(ResultSet rs, int rowNum) throws SQLException {
        return new SellerPayoutAccount(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                rs.getString("bank_name"),
                rs.getString("bank_account_name"),
                rs.getString("bank_account_number"),
                rs.getBoolean("is_default"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }

    private SellerPayoutRequest mapPayoutRequest(ResultSet rs, int rowNum) throws SQLException {
        return new SellerPayoutRequest(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("seller_id")),
                UUID.fromString(rs.getString("payout_account_id")),
                rs.getBigDecimal("amount"),
                PayoutRequestStatus.valueOf(rs.getString("status")),
                rs.getString("admin_note"),
                rs.getString("bank_transfer_ref"),
                toInstant(rs.getTimestamp("requested_at")),
                toInstant(rs.getTimestamp("approved_at")),
                toInstant(rs.getTimestamp("paid_at")),
                toInstant(rs.getTimestamp("rejected_at")),
                toInstant(rs.getTimestamp("cancelled_at")),
                rs.getString("bank_name"),
                rs.getString("bank_account_name"),
                rs.getString("bank_account_number")
        );
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
