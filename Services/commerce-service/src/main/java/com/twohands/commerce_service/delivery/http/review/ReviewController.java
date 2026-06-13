package com.twohands.commerce_service.delivery.http.review;

import com.twohands.commerce_service.application.review.createproductreview.CreateProductReviewCommand;
import com.twohands.commerce_service.application.review.createproductreview.CreateProductReviewUseCase;
import com.twohands.commerce_service.application.review.updateproductreview.UpdateProductReviewCommand;
import com.twohands.commerce_service.application.review.updateproductreview.UpdateProductReviewUseCase;
import com.twohands.commerce_service.application.review.uploadreviewmedia.ReviewMediaFileCommand;
import com.twohands.commerce_service.application.review.uploadreviewmedia.UploadReviewMediaCommand;
import com.twohands.commerce_service.application.review.uploadreviewmedia.UploadReviewMediaUseCase;
import com.twohands.commerce_service.application.review.viewreviewcontext.ViewReviewContextCommand;
import com.twohands.commerce_service.application.review.viewreviewcontext.ViewReviewContextUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.review.CreateProductReviewResult;
import com.twohands.commerce_service.domain.review.ReviewContextSnapshot;
import com.twohands.commerce_service.domain.review.ReviewMediaItem;
import com.twohands.commerce_service.domain.review.UpdateProductReviewResult;
import com.twohands.commerce_service.domain.review.UploadReviewMediaResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/reviews")
public class ReviewController {

    private final CreateProductReviewUseCase createProductReviewUseCase;
    private final UpdateProductReviewUseCase updateProductReviewUseCase;
    private final UploadReviewMediaUseCase uploadReviewMediaUseCase;
    private final ViewReviewContextUseCase viewReviewContextUseCase;

    public ReviewController(
            CreateProductReviewUseCase createProductReviewUseCase,
            UpdateProductReviewUseCase updateProductReviewUseCase,
            UploadReviewMediaUseCase uploadReviewMediaUseCase,
            ViewReviewContextUseCase viewReviewContextUseCase
    ) {
        this.createProductReviewUseCase = createProductReviewUseCase;
        this.updateProductReviewUseCase = updateProductReviewUseCase;
        this.uploadReviewMediaUseCase = uploadReviewMediaUseCase;
        this.viewReviewContextUseCase = viewReviewContextUseCase;
    }

    @GetMapping("/context")
    public ResponseEntity<ApiResponse<ViewReviewContextResponse>> viewReviewContext(
            @RequestParam("order_item_id") UUID orderItemId,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        ReviewContextSnapshot snapshot = viewReviewContextUseCase.execute(
                new ViewReviewContextCommand(buyerId, orderItemId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                viewReviewContextUseCase.successMessage(),
                ViewReviewContextResponse.from(snapshot)
        ));
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<UpdateProductReviewResponse>> updateProductReview(
            @PathVariable UUID reviewId,
            @RequestBody @Valid UpdateProductReviewRequest request,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        UpdateProductReviewResult result = updateProductReviewUseCase.execute(
                new UpdateProductReviewCommand(
                        buyerId,
                        reviewId,
                        request.rating(),
                        request.comment()
                )
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                updateProductReviewUseCase.successMessage(),
                toUpdateResponse(result)
        ));
    }

    @PostMapping(value = "/{reviewId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadReviewMediaResponse>> uploadReviewMedia(
            @PathVariable UUID reviewId,
            @RequestPart("files") List<MultipartFile> files,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        UploadReviewMediaResult result = uploadReviewMediaUseCase.execute(
                new UploadReviewMediaCommand(buyerId, reviewId, toFileCommands(files))
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                uploadReviewMediaUseCase.successMessage(),
                toUploadMediaResponse(result)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateProductReviewResponse>> createProductReview(
            @RequestBody @Valid CreateProductReviewRequest request,
            Authentication authentication
    ) {
        UUID buyerId = resolveUserId(authentication);
        CreateProductReviewResult result = createProductReviewUseCase.execute(new CreateProductReviewCommand(
                buyerId,
                request.orderItemId(),
                request.rating(),
                request.comment()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                createProductReviewUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    private UpdateProductReviewResponse toUpdateResponse(UpdateProductReviewResult result) {
        return new UpdateProductReviewResponse(
                result.reviewId(),
                result.orderItemId(),
                result.sellerId(),
                result.buyerId(),
                result.rating(),
                result.comment(),
                result.status(),
                result.ratingChanged(),
                result.createdAt(),
                result.updatedAt(),
                result.sellerRatingAvg(),
                result.sellerRatingCount()
        );
    }

    private List<ReviewMediaFileCommand> toFileCommands(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "files",
                    "at least one file must be provided"
            );
        }
        List<ReviewMediaFileCommand> commands = new ArrayList<>();
        for (int index = 0; index < files.size(); index++) {
            MultipartFile file = files.get(index);
            if (file == null || file.isEmpty()) {
                throw new AppException(
                        ErrorCode.VALIDATION_ERROR,
                        "Validation failed",
                        "files[" + index + "]",
                        "file must not be empty"
                );
            }
            try {
                commands.add(new ReviewMediaFileCommand(
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getBytes()
                ));
            } catch (IOException ex) {
                throw new AppException(
                        ErrorCode.VALIDATION_ERROR,
                        "Failed to read uploaded file",
                        "files[" + index + "]",
                        "unable to read multipart content"
                );
            }
        }
        return commands;
    }

    private UploadReviewMediaResponse toUploadMediaResponse(UploadReviewMediaResult result) {
        List<ReviewMediaResponse> media = result.media().stream()
                .map(this::toMediaResponse)
                .toList();
        return new UploadReviewMediaResponse(media);
    }

    private ReviewMediaResponse toMediaResponse(ReviewMediaItem item) {
        return new ReviewMediaResponse(item.id(), item.url(), item.type().name());
    }

    private CreateProductReviewResponse toResponse(CreateProductReviewResult result) {
        return new CreateProductReviewResponse(
                result.reviewId(),
                result.orderItemId(),
                result.sellerId(),
                result.buyerId(),
                result.rating(),
                result.comment(),
                result.status(),
                result.createdAt(),
                result.sellerRatingAvg(),
                result.sellerRatingCount()
        );
    }
}
