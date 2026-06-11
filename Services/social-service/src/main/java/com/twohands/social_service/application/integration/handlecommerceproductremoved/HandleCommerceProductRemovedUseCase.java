package com.twohands.social_service.application.integration.handlecommerceproductremoved;

import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.domain.post.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class HandleCommerceProductRemovedUseCase {

    public static final String CONSUMER_NAME = "social-commerce-product-removed";
    private static final String EVENT_TYPE = "COMMERCE_PRODUCT_REMOVED";

    private static final Logger log = LoggerFactory.getLogger(HandleCommerceProductRemovedUseCase.class);

    private final ProcessedDomainEventRepository processedDomainEventRepository;
    private final PostRepository postRepository;

    public HandleCommerceProductRemovedUseCase(
            ProcessedDomainEventRepository processedDomainEventRepository,
            PostRepository postRepository
    ) {
        this.processedDomainEventRepository = processedDomainEventRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public HandleCommerceProductRemovedResult execute(HandleCommerceProductRemovedCommand command) {
        requireEventId(command.eventId());
        requireProductId(command.productId());

        if (processedDomainEventRepository.existsByEventId(command.eventId())) {
            log.debug(
                    "Skip duplicate commerce product removed event. eventId={}, productId={}",
                    command.eventId(),
                    command.productId()
            );
            return new HandleCommerceProductRemovedResult(command.productId(), 0, true);
        }

        long postsUpdated = postRepository.markProductTagsUnavailable(command.productId());
        processedDomainEventRepository.markProcessed(
                command.eventId(),
                CONSUMER_NAME,
                EVENT_TYPE
        );

        log.info(
                "Marked product tags unavailable after commerce removal. eventId={}, productId={}, postsUpdated={}",
                command.eventId(),
                command.productId(),
                postsUpdated
        );

        return new HandleCommerceProductRemovedResult(command.productId(), postsUpdated, false);
    }

    private void requireEventId(UUID eventId) {
        if (eventId == null) {
            throw new InvalidCommerceProductRemovedEventException("event_id is required");
        }
    }

    private void requireProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new InvalidCommerceProductRemovedEventException("product_id is required");
        }
    }
}
