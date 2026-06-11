package com.twohands.social_service.unit.application.integration.handlecommerceproductremoved;

import com.twohands.social_service.application.integration.handlecommerceproductremoved.HandleCommerceProductRemovedCommand;
import com.twohands.social_service.application.integration.handlecommerceproductremoved.HandleCommerceProductRemovedResult;
import com.twohands.social_service.application.integration.handlecommerceproductremoved.HandleCommerceProductRemovedUseCase;
import com.twohands.social_service.domain.integration.ProcessedDomainEventRepository;
import com.twohands.social_service.domain.post.PostRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class HandleCommerceProductRemovedUseCaseTest {

    private final ProcessedDomainEventRepository processedDomainEventRepository =
            mock(ProcessedDomainEventRepository.class);
    private final PostRepository postRepository = mock(PostRepository.class);
    private final HandleCommerceProductRemovedUseCase useCase = new HandleCommerceProductRemovedUseCase(
            processedDomainEventRepository,
            postRepository
    );

    @Test
    void shouldMarkProductTagsUnavailableAndRecordProcessedEvent() {
        UUID eventId = UUID.randomUUID();
        String productId = UUID.randomUUID().toString();

        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(postRepository.markProductTagsUnavailable(productId)).thenReturn(3L);

        HandleCommerceProductRemovedResult result = useCase.execute(
                new HandleCommerceProductRemovedCommand(eventId, productId)
        );

        assertThat(result.duplicate()).isFalse();
        assertThat(result.postsUpdated()).isEqualTo(3);
        verify(postRepository).markProductTagsUnavailable(productId);
        verify(processedDomainEventRepository).markProcessed(
                eq(eventId),
                eq(HandleCommerceProductRemovedUseCase.CONSUMER_NAME),
                eq("COMMERCE_PRODUCT_REMOVED")
        );
    }

    @Test
    void shouldSkipDuplicateEvent() {
        UUID eventId = UUID.randomUUID();
        String productId = UUID.randomUUID().toString();
        when(processedDomainEventRepository.existsByEventId(eventId)).thenReturn(true);

        HandleCommerceProductRemovedResult result = useCase.execute(
                new HandleCommerceProductRemovedCommand(eventId, productId)
        );

        assertThat(result.duplicate()).isTrue();
        assertThat(result.postsUpdated()).isZero();
        verifyNoInteractions(postRepository);
    }
}
