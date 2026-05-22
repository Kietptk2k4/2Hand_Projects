package com.twohands.commerce_service.application.address.updateuseraddress;

import com.twohands.commerce_service.domain.address.UpdateUserAddressDraft;
import com.twohands.commerce_service.domain.address.UpdateUserAddressRepository;
import com.twohands.commerce_service.domain.address.UpdateUserAddressResult;
import com.twohands.commerce_service.domain.address.UpdateUserAddressSnapshot;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.regex.Pattern;

@Service
public class UpdateUserAddressUseCase {

    private static final int RECEIVER_NAME_MAX_LENGTH = 255;
    private static final int PHONE_MAX_LENGTH = 50;
    private static final int LOCATION_CODE_MAX_LENGTH = 50;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)?[0-9]{9,10}$");

    private final UpdateUserAddressRepository updateUserAddressRepository;
    private final Clock clock;

    public UpdateUserAddressUseCase(
            UpdateUserAddressRepository updateUserAddressRepository,
            Clock clock
    ) {
        this.updateUserAddressRepository = updateUserAddressRepository;
        this.clock = clock;
    }

    @Transactional
    public UpdateUserAddressResult execute(UpdateUserAddressCommand command) {
        validateHasUpdates(command);

        UpdateUserAddressSnapshot existing = updateUserAddressRepository
                .findByIdAndUserId(command.addressId(), command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        String receiverName = resolveText(command.receiverName(), existing.receiverName());
        String phone = command.phone() != null
                ? normalizePhone(command.phone())
                : existing.phone();
        String provinceCode = resolveText(command.provinceCode(), existing.provinceCode());
        String districtCode = resolveText(command.districtCode(), existing.districtCode());
        String wardCode = resolveText(command.wardCode(), existing.wardCode());
        String addressDetail = resolveText(command.addressDetail(), existing.addressDetail());
        boolean isDefault = command.isDefault() != null ? command.isDefault() : existing.isDefault();

        validateMergedPayload(receiverName, phone, provinceCode, districtCode, wardCode, addressDetail);

        Instant now = clock.instant();
        return updateUserAddressRepository.update(
                new UpdateUserAddressDraft(
                        existing.addressId(),
                        existing.userId(),
                        receiverName,
                        phone,
                        provinceCode,
                        districtCode,
                        wardCode,
                        addressDetail,
                        isDefault
                ),
                now
        );
    }

    public String successMessage() {
        return "Cap nhat dia chi giao hang thanh cong.";
    }

    private void validateHasUpdates(UpdateUserAddressCommand command) {
        if (command.receiverName() == null
                && command.phone() == null
                && command.provinceCode() == null
                && command.districtCode() == null
                && command.wardCode() == null
                && command.addressDetail() == null
                && command.isDefault() == null) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Validation failed",
                    "request",
                    "at least one field must be provided"
            );
        }
    }

    private void validateMergedPayload(
            String receiverName,
            String phone,
            String provinceCode,
            String districtCode,
            String wardCode,
            String addressDetail
    ) {
        requireText(receiverName, "receiver_name");
        if (receiverName.length() > RECEIVER_NAME_MAX_LENGTH) {
            throw fieldError("receiver_name", "must be at most " + RECEIVER_NAME_MAX_LENGTH + " characters");
        }
        requireText(phone, "phone");
        if (phone.length() > PHONE_MAX_LENGTH) {
            throw fieldError("phone", "must be at most " + PHONE_MAX_LENGTH + " characters");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new AppException(ErrorCode.INVALID_PHONE, "Phone number format is invalid");
        }
        requireText(provinceCode, "province_code");
        requireText(districtCode, "district_code");
        requireText(wardCode, "ward_code");
        requireText(addressDetail, "address_detail");
        validateLocationCode(provinceCode, "province_code");
        validateLocationCode(districtCode, "district_code");
        validateLocationCode(wardCode, "ward_code");
    }

    private String resolveText(String requested, String existing) {
        if (requested == null) {
            return existing;
        }
        return requested.trim();
    }

    private String normalizePhone(String phone) {
        return phone.trim().replaceAll("\\s+", "");
    }

    private void validateLocationCode(String value, String field) {
        if (value.length() > LOCATION_CODE_MAX_LENGTH) {
            throw fieldError(field, "must be at most " + LOCATION_CODE_MAX_LENGTH + " characters");
        }
    }

    private void requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw fieldError(field, "must not be blank");
        }
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, "Validation failed", field, reason);
    }
}
