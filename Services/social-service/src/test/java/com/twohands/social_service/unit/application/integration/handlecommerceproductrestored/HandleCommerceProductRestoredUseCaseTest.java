package com.twohands.social_service.unit.application.integration.handlecommerceproductrestored;

import com.twohands.social_service.application.integration.handlecommerceproductremoved.HandleCommerceProductRemovedCommand;
import com.twohands.social_service.application.integration.handlecommerceproductrestored.HandleCommerceProductRestoredResult;
import com.twohands.social_service.application.integration.handlecommerceproductrestored.HandleCommerceProductRestoredUseCase;
import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.domain.post.PostRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HandleCommerceProductRestoredUseCaseTest {

    private final ProcessedDomainEventRepository processedDomainEventRepository =
            org.mockito.Mockito.mock(ProcessedDomainEventRepository.class);
    private final PostRepository postRepository = org.mockito.Mockito.mock(PostRepository.class);
    private final HandleCommerceProductRestoredUseCase useCase = new HandleCommerceProductRestoredUseCase(
            processedDomainEventRepository,
            postRepository
    );

    @Test
    void shouldMarkProductTagsAvailableAndMarkProcessed() {
        UUID eventId = UUID.randomUUID();
        String productId = UUID.randomUUID().toString();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(postRepository.markProductTagsAvailable(productId)).thenReturn(2L);

        HandleCommerceProductRestoredResult result = useCase.execute(
                new HandleCommerceProductRemovedCommand(eventId, productId)
        );

        assertThat(result.duplicate()).isFalse();
        assertThat(result.postsUpdated()).isEqualTo(2L);
        verify(postRepository).markProductTagsAvailable(productId);
        verify(processedDomainEventRepository).markProcessed(
                eq(eventId),
                eq(HandleCommerceProductRestoredUseCase.CONSUMER_NAME),
                eq("COMMERCE_PRODUCT_RESTORED")
        );
    }

    @Test
    void shouldSkipDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        String productId = UUID.randomUUID().toString();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(true);

        HandleCommerceProductRestoredResult result = useCase.execute(
                new HandleCommerceProductRemovedCommand(eventId, productId)
        );

        assertThat(result.duplicate()).isTrue();
        assertThat(result.postsUpdated()).isZero();
    }
}
