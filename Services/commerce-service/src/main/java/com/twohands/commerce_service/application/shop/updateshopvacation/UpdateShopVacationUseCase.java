package com.twohands.commerce_service.application.shop.updateshopvacation;

import com.twohands.commerce_service.application.shop.common.ShopVacationUpdatedOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationDraft;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationRepository;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationResult;
import com.twohands.commerce_service.domain.shop.UpdateShopVacationSnapshot;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;

@Service
public class UpdateShopVacationUseCase {

    private static final int VACATION_MESSAGE_MAX_LENGTH = 500;

    private final UpdateShopVacationRepository updateShopVacationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ShopVacationUpdatedOutboxService shopVacationUpdatedOutboxService;
    private final Clock clock;

    public UpdateShopVacationUseCase(
            UpdateShopVacationRepository updateShopVacationRepository,
            OutboxEventRepository outboxEventRepository,
            ShopVacationUpdatedOutboxService shopVacationUpdatedOutboxService,
            Clock clock
    ) {
        this.updateShopVacationRepository = updateShopVacationRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.shopVacationUpdatedOutboxService = shopVacationUpdatedOutboxService;
        this.clock = clock;
    }

    @Transactional
    public UpdateShopVacationResult execute(UpdateShopVacationCommand command) {
        UpdateShopVacationSnapshot existing = updateShopVacationRepository
                .findBySellerId(command.sellerId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_FOUND));

        String vacationMessage = resolveVacationMessage(
                command.isVacation(),
                command.vacationMessage(),
                existing.vacationMessage()
        );
        validateVacationMessage(vacationMessage);

        Instant now = clock.instant();
        UpdateShopVacationResult updated = updateShopVacationRepository.updateVacationSettings(
                new UpdateShopVacationDraft(
                        existing.shopId(),
                        command.isVacation(),
                        vacationMessage
                ),
                now
        );

        outboxEventRepository.save(shopVacationUpdatedOutboxService.build(
                updated.shopId(),
                updated.sellerId(),
                updated.status(),
                updated.isVacation(),
                updated.vacationMessage(),
                now
        ));

        return updated;
    }

    public String successMessage(boolean isVacation) {
        return isVacation
                ? "Bat che do nghi shop thanh cong."
                : "Tat che do nghi shop thanh cong.";
    }

    private String resolveVacationMessage(boolean isVacation, String requested, String existing) {
        if (!isVacation) {
            if (requested == null) {
                return null;
            }
            return normalizeMessage(requested);
        }
        if (requested == null) {
            return existing;
        }
        return normalizeMessage(requested);
    }

    private String normalizeMessage(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void validateVacationMessage(String vacationMessage) {
        if (vacationMessage != null && vacationMessage.length() > VACATION_MESSAGE_MAX_LENGTH) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "vacation_message",
                    "must be at most " + VACATION_MESSAGE_MAX_LENGTH + " characters"
            );
        }
    }
}
