package com.twohands.social_service.application.integration.handlecommerceproductrestored;

import com.twohands.social_service.application.integration.handlecommerceproductremoved.HandleCommerceProductRemovedCommand;
import com.twohands.social_service.application.integration.handlecommerceproductremoved.InvalidCommerceProductRemovedEventException;
import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.domain.post.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class HandleCommerceProductRestoredUseCase {

    public static final String CONSUMER_NAME = "social-commerce-product-restored";
    private static final String EVENT_TYPE = "COMMERCE_PRODUCT_RESTORED";

    private static final Logger log = LoggerFactory.getLogger(HandleCommerceProductRestoredUseCase.class);

    private final ProcessedDomainEventRepository processedDomainEventRepository;
    private final PostRepository postRepository;

    public HandleCommerceProductRestoredUseCase(
            ProcessedDomainEventRepository processedDomainEventRepository,
            PostRepository postRepository
    ) {
        this.processedDomainEventRepository = processedDomainEventRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public HandleCommerceProductRestoredResult execute(HandleCommerceProductRemovedCommand command) {
        requireEventId(command.eventId());
        requireProductId(command.productId());

        if (processedDomainEventRepository.existsByEventId(command.eventId())) {
            log.debug(
                    "Skip duplicate commerce product restored event. eventId={}, productId={}",
                    command.eventId(),
                    command.productId()
            );
            return new HandleCommerceProductRestoredResult(command.productId(), 0, true);
        }

        long postsUpdated = postRepository.markProductTagsAvailable(command.productId());
        processedDomainEventRepository.markProcessed(
                command.eventId(),
                CONSUMER_NAME,
                EVENT_TYPE
        );

        log.info(
                "Marked product tags available after commerce restore. eventId={}, productId={}, postsUpdated={}",
                command.eventId(),
                command.productId(),
                postsUpdated
        );

        return new HandleCommerceProductRestoredResult(command.productId(), postsUpdated, false);
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
