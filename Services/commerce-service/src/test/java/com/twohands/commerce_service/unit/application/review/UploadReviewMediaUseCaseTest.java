package com.twohands.commerce_service.unit.application.review;

import com.twohands.commerce_service.application.review.uploadreviewmedia.ReviewMediaFileCommand;
import com.twohands.commerce_service.application.review.uploadreviewmedia.UploadReviewMediaCommand;
import com.twohands.commerce_service.application.review.uploadreviewmedia.UploadReviewMediaUseCase;
import com.twohands.commerce_service.common.media.ReviewMediaFileValidator;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.domain.review.ReviewMediaItem;
import com.twohands.commerce_service.domain.review.ReviewMediaType;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.UploadReviewMediaOwnedReview;
import com.twohands.commerce_service.domain.review.UploadReviewMediaRepository;
import com.twohands.commerce_service.domain.review.UploadReviewMediaResult;
import com.twohands.commerce_service.domain.storage.ReviewMediaStorageGateway;
import com.twohands.commerce_service.domain.storage.StoredReviewMedia;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadReviewMediaUseCaseTest {

    @Mock
    private UploadReviewMediaRepository uploadReviewMediaRepository;

    @Mock
    private ReviewMediaStorageGateway reviewMediaStorageGateway;

    private UploadReviewMediaUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID reviewId = UUID.randomUUID();
    private final UUID mediaId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        CommerceObjectStorageProperties properties = new CommerceObjectStorageProperties();
        properties.setEnabled(true);
        useCase = new UploadReviewMediaUseCase(
                uploadReviewMediaRepository,
                reviewMediaStorageGateway,
                new ReviewMediaFileValidator(),
                properties
        );
    }

    @Test
    void shouldUploadAndPersistMedia() {
        when(uploadReviewMediaRepository.findOwnedReview(reviewId, buyerId))
                .thenReturn(Optional.of(new UploadReviewMediaOwnedReview(reviewId, buyerId, ReviewStatus.VISIBLE)));
        when(uploadReviewMediaRepository.countMediaByReviewId(reviewId)).thenReturn(0);
        when(reviewMediaStorageGateway.upload(eq(buyerId), eq(reviewId), any()))
                .thenReturn(new StoredReviewMedia("reviews/key.jpg", "http://localhost:9000/2hands-commerce-review/reviews/key.jpg"));
        when(uploadReviewMediaRepository.insertMedia(eq(reviewId), any()))
                .thenReturn(List.of(new ReviewMediaItem(
                        mediaId,
                        "http://localhost:9000/2hands-commerce-review/reviews/key.jpg",
                        ReviewMediaType.IMAGE
                )));

        UploadReviewMediaResult result = useCase.execute(new UploadReviewMediaCommand(
                buyerId,
                reviewId,
                List.of(imageFile())
        ));

        assertThat(result.media()).hasSize(1);
        assertThat(result.media().getFirst().type()).isEqualTo(ReviewMediaType.IMAGE);
        verify(reviewMediaStorageGateway).upload(eq(buyerId), eq(reviewId), any());
        verify(uploadReviewMediaRepository).insertMedia(eq(reviewId), any());
    }

    @Test
    void shouldRejectWhenMinioDisabled() {
        CommerceObjectStorageProperties properties = new CommerceObjectStorageProperties();
        properties.setEnabled(false);
        UploadReviewMediaUseCase disabledUseCase = new UploadReviewMediaUseCase(
                uploadReviewMediaRepository,
                reviewMediaStorageGateway,
                new ReviewMediaFileValidator(),
                properties
        );

        assertThatThrownBy(() -> disabledUseCase.execute(new UploadReviewMediaCommand(
                buyerId,
                reviewId,
                List.of(imageFile())
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.OBJECT_STORAGE_UNAVAILABLE);

        verify(uploadReviewMediaRepository, never()).findOwnedReview(any(), any());
    }

    @Test
    void shouldRejectReviewNotFound() {
        when(uploadReviewMediaRepository.findOwnedReview(reviewId, buyerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UploadReviewMediaCommand(
                buyerId,
                reviewId,
                List.of(imageFile())
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    void shouldRejectHiddenReview() {
        when(uploadReviewMediaRepository.findOwnedReview(reviewId, buyerId))
                .thenReturn(Optional.of(new UploadReviewMediaOwnedReview(reviewId, buyerId, ReviewStatus.HIDDEN)));

        assertThatThrownBy(() -> useCase.execute(new UploadReviewMediaCommand(
                buyerId,
                reviewId,
                List.of(imageFile())
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_NOT_VISIBLE);
    }

    @Test
    void shouldRejectMediaCountExceeded() {
        when(uploadReviewMediaRepository.findOwnedReview(reviewId, buyerId))
                .thenReturn(Optional.of(new UploadReviewMediaOwnedReview(reviewId, buyerId, ReviewStatus.VISIBLE)));
        when(uploadReviewMediaRepository.countMediaByReviewId(reviewId)).thenReturn(10);

        assertThatThrownBy(() -> useCase.execute(new UploadReviewMediaCommand(
                buyerId,
                reviewId,
                List.of(imageFile())
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_MEDIA_LIMIT_EXCEEDED);

        verify(reviewMediaStorageGateway, never()).upload(any(), any(), any());
    }

    @Test
    void shouldCleanupOrphanObjectWhenDbInsertFails() {
        when(uploadReviewMediaRepository.findOwnedReview(reviewId, buyerId))
                .thenReturn(Optional.of(new UploadReviewMediaOwnedReview(reviewId, buyerId, ReviewStatus.VISIBLE)));
        when(uploadReviewMediaRepository.countMediaByReviewId(reviewId)).thenReturn(0);
        when(reviewMediaStorageGateway.upload(eq(buyerId), eq(reviewId), any()))
                .thenReturn(new StoredReviewMedia("reviews/orphan.jpg", "http://localhost:9000/2hands-commerce-review/reviews/orphan.jpg"));
        when(uploadReviewMediaRepository.insertMedia(eq(reviewId), any()))
                .thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> useCase.execute(new UploadReviewMediaCommand(
                buyerId,
                reviewId,
                List.of(imageFile())
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.OBJECT_STORAGE_UNAVAILABLE);

        verify(reviewMediaStorageGateway).deleteBestEffort("reviews/orphan.jpg");
    }

    @Test
    void shouldRejectInvalidMediaType() {
        when(uploadReviewMediaRepository.findOwnedReview(reviewId, buyerId))
                .thenReturn(Optional.of(new UploadReviewMediaOwnedReview(reviewId, buyerId, ReviewStatus.VISIBLE)));
        when(uploadReviewMediaRepository.countMediaByReviewId(reviewId)).thenReturn(0);

        assertThatThrownBy(() -> useCase.execute(new UploadReviewMediaCommand(
                buyerId,
                reviewId,
                List.of(new ReviewMediaFileCommand("file.txt", "text/plain", "hello".getBytes()))
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_MEDIA_TYPE);

        verify(reviewMediaStorageGateway, never()).upload(any(), any(), any());
    }

    private ReviewMediaFileCommand imageFile() {
        return new ReviewMediaFileCommand("photo.jpg", "image/jpeg", new byte[1024]);
    }
}
