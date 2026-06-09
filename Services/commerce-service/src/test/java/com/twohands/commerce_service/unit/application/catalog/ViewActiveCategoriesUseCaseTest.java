package com.twohands.commerce_service.unit.application.catalog;

import com.twohands.commerce_service.application.catalog.viewactivecategories.ViewActiveCategoriesCommand;
import com.twohands.commerce_service.application.catalog.viewactivecategories.ViewActiveCategoriesUseCase;
import com.twohands.commerce_service.domain.catalog.CategoryReadRepository;
import com.twohands.commerce_service.domain.catalog.CategorySummary;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewActiveCategoriesUseCaseTest {

    @Mock
    private CategoryReadRepository categoryReadRepository;

    private ViewActiveCategoriesUseCase useCase;

    private final Instant now = Instant.parse("2026-05-21T16:00:00Z");

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(now, ZoneOffset.UTC);
        useCase = new ViewActiveCategoriesUseCase(categoryReadRepository, clock);
    }

    @Test
    void execute_returnsCategoriesFromRepository() {
        UUID categoryId = UUID.randomUUID();
        CategorySummary summary = new CategorySummary(
                categoryId,
                "Ao nu",
                "ao-nu",
                UUID.randomUUID(),
                2,
                true,
                3L
        );

        when(categoryReadRepository.findActiveSummaries(null, null, false, true, now))
                .thenReturn(List.of(summary));

        List<CategorySummary> result = useCase.execute(
                new ViewActiveCategoriesCommand(null, null, false, true)
        );

        assertThat(result).containsExactly(summary);
        verify(categoryReadRepository).findActiveSummaries(eq(null), eq(null), eq(false), eq(true), eq(now));
    }

    @Test
    void execute_passesLeafOnlyFilter() {
        when(categoryReadRepository.findActiveSummaries(1, 2, true, false, now))
                .thenReturn(List.of());

        useCase.execute(new ViewActiveCategoriesCommand(1, 2, true, false));

        verify(categoryReadRepository).findActiveSummaries(1, 2, true, false, now);
    }

    @Test
    void execute_rejectsInvalidLevelRange() {
        assertThatThrownBy(() -> useCase.execute(new ViewActiveCategoriesCommand(2, 1, false, true)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }
}
