package com.twohands.commerce_service.application.finance.platform.viewplatformcodpipeline;

import com.twohands.commerce_service.domain.finance.PlatformCodPipeline;
import com.twohands.commerce_service.domain.finance.PlatformFinanceReadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewPlatformCodPipelineUseCase {

    private final PlatformFinanceReadRepository platformFinanceReadRepository;

    public ViewPlatformCodPipelineUseCase(PlatformFinanceReadRepository platformFinanceReadRepository) {
        this.platformFinanceReadRepository = platformFinanceReadRepository;
    }

    @Transactional(readOnly = true)
    public PlatformCodPipeline execute() {
        return platformFinanceReadRepository.findCodPipeline();
    }

    public String successMessage() {
        return "Lay COD pipeline san thanh cong.";
    }
}
