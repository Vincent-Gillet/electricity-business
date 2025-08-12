package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.Terminal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TerminalRepositoryCustom {
    List<Terminal> searchTerminalsWithCriteria(
            BigDecimal longitude,
            BigDecimal latitude,
            Double radius,
            Boolean occupied,
            LocalDateTime startingDate,
            LocalDateTime endingDate
    );
}
