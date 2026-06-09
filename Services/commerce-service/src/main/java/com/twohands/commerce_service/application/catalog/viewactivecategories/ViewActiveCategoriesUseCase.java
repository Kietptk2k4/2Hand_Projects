package com.twohands.commerce_service.application.catalog.viewactivecategories;

import com.twohands.commerce_service.domain.catalog.CategoryReadRepository;
import com.twohands.commerce_service.domain.catalog.CategorySummary;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class ViewActiveCategoriesUseCase {

    private final CategoryReadRepository categoryReadRepository;
    private final Clock clock;

    public ViewActiveCategoriesUseCase(CategoryReadRepository categoryReadRepository, Clock clock) {
        this.categoryReadRepository = categoryReadRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CategorySummary> execute(ViewActiveCategoriesCommand command) {
        validateLevels(command.minLevel(), command.maxLevel());

        Instant now = clock.instant();
        return categoryReadRepository.findActiveSummaries(
                command.minLevel(),
                command.maxLevel(),
                command.isLeafOnly(),
                command.shouldIncludeProductCounts(),
                now
        );
    }

    public String successMessage() {
        return "Lay danh sach danh muc thanh cong.";
    }

    private void validateLevels(Integer minLevel, Integer maxLevel) {
        if (minLevel != null && minLevel < 0) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "min_level must be >= 0",
                    "min_level",
                    "must be >= 0"
            );
        }
        if (maxLevel != null && maxLevel < 0) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "max_level must be >= 0",
                    "max_level",
                    "must be >= 0"
            );
        }
        if (minLevel != null && maxLevel != null && minLevel > maxLevel) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "min_level must be <= max_level",
                    "min_level",
                    "must be <= max_level"
            );
        }
    }
}
