package com.twohands.commerce_service.delivery.http.address;

import com.twohands.commerce_service.application.address.createuseraddress.CreateUserAddressCommand;
import com.twohands.commerce_service.application.address.createuseraddress.CreateUserAddressUseCase;
import com.twohands.commerce_service.application.address.deleteuseraddress.DeleteUserAddressCommand;
import com.twohands.commerce_service.application.address.deleteuseraddress.DeleteUserAddressUseCase;
import com.twohands.commerce_service.common.dto.ApiResponse;
import com.twohands.commerce_service.domain.address.CreateUserAddressResult;
import com.twohands.commerce_service.domain.address.DeleteUserAddressResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import com.twohands.commerce_service.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/commerce/api/v1/addresses")
public class AddressController {

    private final CreateUserAddressUseCase createUserAddressUseCase;
    private final DeleteUserAddressUseCase deleteUserAddressUseCase;

    public AddressController(
            CreateUserAddressUseCase createUserAddressUseCase,
            DeleteUserAddressUseCase deleteUserAddressUseCase
    ) {
        this.createUserAddressUseCase = createUserAddressUseCase;
        this.deleteUserAddressUseCase = deleteUserAddressUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateUserAddressResponse>> createAddress(
            @RequestBody @Valid CreateUserAddressRequest request,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        CreateUserAddressResult result = createUserAddressUseCase.execute(new CreateUserAddressCommand(
                userId,
                request.receiverName(),
                request.phone(),
                request.provinceCode(),
                request.districtCode(),
                request.wardCode(),
                request.addressDetail(),
                request.isDefault()
        ));

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                createUserAddressUseCase.successMessage(),
                toResponse(result)
        ));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return principal.userId();
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<DeleteUserAddressResponse>> deleteAddress(
            @PathVariable UUID addressId,
            Authentication authentication
    ) {
        UUID userId = resolveUserId(authentication);
        DeleteUserAddressResult result = deleteUserAddressUseCase.execute(
                new DeleteUserAddressCommand(userId, addressId)
        );

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                deleteUserAddressUseCase.successMessage(),
                toDeleteResponse(result)
        ));
    }

    private DeleteUserAddressResponse toDeleteResponse(DeleteUserAddressResult result) {
        return new DeleteUserAddressResponse(
                result.addressId(),
                result.userId(),
                result.wasDefault(),
                result.newDefaultAddressId(),
                result.deletedAt()
        );
    }

    private CreateUserAddressResponse toResponse(CreateUserAddressResult result) {
        return new CreateUserAddressResponse(
                result.addressId(),
                result.userId(),
                result.receiverName(),
                result.phone(),
                result.provinceCode(),
                result.districtCode(),
                result.wardCode(),
                result.addressDetail(),
                result.isDefault(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
