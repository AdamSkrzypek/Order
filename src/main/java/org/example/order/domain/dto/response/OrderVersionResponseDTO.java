package org.example.order.domain.dto.response;

import org.example.order.domain.enums.SourceOfChange;
import org.example.order.domain.enums.OrderVersionType;

import java.time.Instant;

public record OrderVersionResponseDTO(StatusDTO orderStatusDTO, StatusDTO paymentStatusDto,
                                      Instant updatedAt, SourceOfChange sourceOfChange,
                                      OrderVersionType versionType) {
}
