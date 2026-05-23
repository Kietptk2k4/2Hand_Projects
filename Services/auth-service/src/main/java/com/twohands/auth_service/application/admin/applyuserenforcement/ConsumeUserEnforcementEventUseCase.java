package com.twohands.auth_service.application.admin.applyuserenforcement;

import com.twohands.auth_service.domain.enforcement.UserEnforcementActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumeUserEnforcementEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsumeUserEnforcementEventUseCase.class);

    private final ApplyUserEnforcementUseCase applyUserEnforcementUseCase;

    public ConsumeUserEnforcementEventUseCase(ApplyUserEnforcementUseCase applyUserEnforcementUseCase) {
        this.applyUserEnforcementUseCase = applyUserEnforcementUseCase;
    }

    @Transactional
    public ApplyUserEnforcementResult execute(ConsumeUserEnforcementEventCommand command) {
        UserEnforcementActionType actionType = resolveActionType(command);

        log.info(
                "Applying user enforcement from event. eventId={}, eventType={}, enforcementId={}, userId={}, actionType={}",
                command.eventId(),
                command.eventType(),
                command.enforcementId(),
                command.userId(),
                actionType
        );

        return applyUserEnforcementUseCase.execute(ApplyUserEnforcementCommand.forEventApply(
                command.eventId(),
                command.enforcementId(),
                command.userId(),
                actionType,
                command.reasonCode(),
                command.description(),
                command.expiresAt()
        ));
    }

    private UserEnforcementActionType resolveActionType(ConsumeUserEnforcementEventCommand command) {
        if (command.actionType() != null && !command.actionType().isBlank()) {
            return UserEnforcementActionType.valueOf(command.actionType().trim().toUpperCase());
        }
        return UserEnforcementActionType.fromEventType(command.eventType());
    }
}
