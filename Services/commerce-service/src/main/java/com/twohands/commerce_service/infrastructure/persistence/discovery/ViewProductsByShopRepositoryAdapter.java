package com.twohands.commerce_service.infrastructure.persistence.discovery;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;
import com.twohands.commerce_service.domain.discovery.ProductDiscoveryRepository;
import com.twohands.commerce_service.domain.discovery.ProductDiscoverySort;
import com.twohands.commerce_service.domain.discovery.PublicShopSummary;
import com.twohands.commerce_service.domain.discovery.ViewProductsByShopRepository;
import com.twohands.commerce_service.domain.discovery.ViewProductsByShopResult;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ViewProductsByShopRepositoryAdapter implements ViewProductsByShopRepository {

    private static final String ACTIVE_PUBLIC_SHOP_SQL = """
            SELECT s.id AS shop_id,
                   s.seller_id,
                   s.shop_name,
                   s.description,
                   s.avatar_url,
                   s.cover_url,
                   s.rating_avg,
                   s.rating_count,
                   COALESCE(ss.is_vacation, FALSE) AS shop_vacation,
                   ss.vacation_message
            FROM seller_shops s
            LEFT JOIN shop_settings ss ON ss.shop_id = s.id
            WHERE s.id = :shopId
              AND s.status = 'ACTIVE'
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ProductDiscoveryRepository productDiscoveryRepository;

    public ViewProductsByShopRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            ProductDiscoveryRepository productDiscoveryRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.productDiscoveryRepository = productDiscoveryRepository;
    }

    @Override
    public Optional<ViewProductsByShopResult> findVisibleProductsByShopId(
            UUID shopId,
            ProductDiscoverySort sort,
            PageQuery pageQuery,
            Instant now
    ) {
        List<PublicShopSummary> shops = jdbcTemplate.query(
                ACTIVE_PUBLIC_SHOP_SQL,
                new MapSqlParameterSource("shopId", shopId),
                this::mapShop
        );
        if (shops.isEmpty()) {
            return Optional.empty();
        }

        PublicShopSummary shop = shops.getFirst();
        long totalItems = productDiscoveryRepository.countVisibleProductsByShop(shopId, now);
        List<ProductCardSummary> items = totalItems == 0
                ? List.of()
                : productDiscoveryRepository.findVisibleProductsByShop(shopId, sort, pageQuery, now);

        return Optional.of(new ViewProductsByShopResult(
                shop,
                items,
                PageMeta.of(pageQuery.page(), pageQuery.limit(), totalItems)
        ));
    }

    private PublicShopSummary mapShop(ResultSet rs, int rowNum) throws SQLException {
        BigDecimal ratingAvg = rs.getBigDecimal("rating_avg");
        return new PublicShopSummary(
                UUID.fromString(rs.getString("shop_id")),
                rs.getString("shop_name"),
                rs.getString("description"),
                rs.getString("avatar_url"),
                rs.getString("cover_url"),
                ratingAvg == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : ratingAvg.setScale(2, RoundingMode.HALF_UP),
                rs.getInt("rating_count"),
                rs.getBoolean("shop_vacation"),
                rs.getString("vacation_message"),
                UUID.fromString(rs.getString("seller_id"))
        );
    }
}
