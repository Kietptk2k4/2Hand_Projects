package com.twohands.commerce_service.application.shipment.viewshipmentsupportlist;

import com.twohands.commerce_service.domain.shipment.ShipmentSupportListPagedResult;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportListQueryPolicy;
import com.twohands.commerce_service.domain.shipment.ShipmentSupportListSearchCriteria;
import com.twohands.commerce_service.domain.shipment.ViewShipmentSupportListRepository;
import com.twohands.commerce_service.domain.support.WebhookSupportPageRequest;
import com.twohands.commerce_service.domain.support.WebhookSupportPaginationPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewShipmentSupportListUseCase {

    private final ViewShipmentSupportListRepository viewShipmentSupportListRepository;

    public ViewShipmentSupportListUseCase(ViewShipmentSupportListRepository viewShipmentSupportListRepository) {
        this.viewShipmentSupportListRepository = viewShipmentSupportListRepository;
    }

    @Transactional(readOnly = true)
    public ViewShipmentSupportListResult execute(ViewShipmentSupportListQuery query) {
        ShipmentSupportListSearchCriteria criteria = new ShipmentSupportListSearchCriteria(
                ShipmentSupportListQueryPolicy.parseStatus(query.status()),
                ShipmentSupportListQueryPolicy.parseCarrier(query.carrier()),
                ShipmentSupportListQueryPolicy.parseSortField(query.sort())
        );
        WebhookSupportPageRequest pageRequest = WebhookSupportPaginationPolicy.normalize(query.page(), query.size());
        ShipmentSupportListPagedResult page = viewShipmentSupportListRepository.search(criteria, pageRequest);

        return new ViewShipmentSupportListResult(
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.items()
        );
    }

    public String successMessage() {
        return "Shipment support list retrieved successfully";
    }
}
