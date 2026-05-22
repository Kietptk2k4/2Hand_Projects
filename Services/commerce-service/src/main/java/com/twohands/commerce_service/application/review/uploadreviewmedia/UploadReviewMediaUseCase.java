package com.twohands.commerce_service.application.review.uploadreviewmedia;

import com.twohands.commerce_service.common.media.ReviewMediaFileValidator;
import com.twohands.commerce_service.config.CommerceObjectStorageProperties;
import com.twohands.commerce_service.domain.review.ReviewMediaInsertDraft;
import com.twohands.commerce_service.domain.review.ReviewMediaItem;
import com.twohands.commerce_service.domain.review.ReviewMediaType;
import com.twohands.commerce_service.domain.review.ReviewStatus;
import com.twohands.commerce_service.domain.review.UploadReviewMediaOwnedReview;
import com.twohands.commerce_service.domain.review.UploadReviewMediaRepository;
import com.twohands.commerce_service.domain.review.UploadReviewMediaResult;
import com.twohands.commerce_service.domain.storage.ReviewMediaStorageGateway;
import com.twohands.commerce_service.domain.storage.ReviewMediaUploadPayload;
import com.twohands.commerce_service.domain.storage.StoredReviewMedia;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class UploadReviewMediaUseCase {

    private final UploadReviewMediaRepository uploadReviewMediaRepository;
    private final ReviewMediaStorageGateway reviewMediaStorageGateway;
    private final ReviewMediaFileValidator reviewMediaFileValidator;
    private final CommerceObjectStorageProperties objectStorageProperties;

    public UploadReviewMediaUseCase(
            UploadReviewMediaRepository uploadReviewMediaRepository,
            ReviewMediaStorageGateway reviewMediaStorageGateway,
            ReviewMediaFileValidator reviewMediaFileValidator,
            CommerceObjectStorageProperties objectStorageProperties
    ) {
        this.uploadReviewMediaRepository = uploadReviewMediaRepository;
        this.reviewMediaStorageGateway = reviewMediaStorageGateway;
        this.reviewMediaFileValidator = reviewMediaFileValidator;
        this.objectStorageProperties = objectStorageProperties;
    }

    public UploadReviewMediaResult execute(UploadReviewMediaCommand command) {
        ensureObjectStorageEnabled();
        validateFilesProvided(command.files());

        UploadReviewMediaOwnedReview review = uploadReviewMediaRepository
                .findOwnedReview(command.reviewId(), command.buyerId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        if (review.status() != ReviewStatus.VISIBLE) {
            throw new AppException(ErrorCode.REVIEW_NOT_VISIBLE, "Hidden review cannot receive media uploads");
        }

        int existingCount = uploadReviewMediaRepository.countMediaByReviewId(command.reviewId());
        if (existingCount + command.files().size() > ReviewMediaFileValidator.MAX_MEDIA_PER_REVIEW) {
            throw new AppException(
                    ErrorCode.REVIEW_MEDIA_LIMIT_EXCEEDED,
                    "Review media count limit exceeded",
                    "files",
                    "maximum " + ReviewMediaFileValidator.MAX_MEDIA_PER_REVIEW + " media items per review"
            );
        }

        List<StoredReviewMedia> uploadedObjects = new ArrayList<>();
        List<ReviewMediaInsertDraft> insertDrafts = new ArrayList<>();
        try {
            for (int index = 0; index < command.files().size(); index++) {
                ReviewMediaFileCommand file = command.files().get(index);
                String fieldName = "files[" + index + "]";
                reviewMediaFileValidator.validateFile(fieldName, file.contentType(), file.content().length);
                ReviewMediaType mediaType = reviewMediaFileValidator.resolveMediaType(file.contentType());

                StoredReviewMedia stored = reviewMediaStorageGateway.upload(
                        review.buyerId(),
                        review.reviewId(),
                        new ReviewMediaUploadPayload(
                                file.originalFilename(),
                                file.contentType(),
                                file.content()
                        )
                );
                uploadedObjects.add(stored);
                insertDrafts.add(new ReviewMediaInsertDraft(stored.publicUrl(), mediaType));
            }

            List<ReviewMediaItem> saved = uploadReviewMediaRepository.insertMedia(review.reviewId(), insertDrafts);
            return new UploadReviewMediaResult(saved);
        } catch (AppException ex) {
            cleanupUploadedObjects(uploadedObjects);
            throw ex;
        } catch (RuntimeException ex) {
            cleanupUploadedObjects(uploadedObjects);
            throw new AppException(
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                    "Failed to upload review media",
                    ex
            );
        }
    }

    public String successMessage() {
        return "Upload media danh gia thanh cong.";
    }

    private void ensureObjectStorageEnabled() {
        if (!objectStorageProperties.isEnabled()) {
            throw new AppException(
                    ErrorCode.OBJECT_STORAGE_UNAVAILABLE,
                    "Object storage is disabled; enable COMMERCE_MINIO_ENABLED to upload review media"
            );
        }
    }

    private void validateFilesProvided(List<ReviewMediaFileCommand> files) {
        if (files == null || files.isEmpty()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "files",
                    "at least one file must be provided"
            );
        }
        for (int index = 0; index < files.size(); index++) {
            ReviewMediaFileCommand file = files.get(index);
            if (file.content() == null) {
                throw fieldError("files[" + index + "]", "file content is required");
            }
            if (!StringUtils.hasText(file.contentType())) {
                throw fieldError("files[" + index + "]", "content type is required");
            }
        }
    }

    private void cleanupUploadedObjects(List<StoredReviewMedia> uploadedObjects) {
        for (StoredReviewMedia stored : uploadedObjects) {
            reviewMediaStorageGateway.deleteBestEffort(stored.objectKey());
        }
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
