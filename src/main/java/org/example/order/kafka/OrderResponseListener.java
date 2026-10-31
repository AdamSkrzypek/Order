package org.example.order.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.domain.dto.response.OrderResponseDTO;
import org.example.order.domain.enums.SourceOfChange;
import org.example.order.services.impl.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderResponseListener {

    private final OrderService orderService;

    @KafkaListener(topics = "${topics.order.response}")
    public void handleOrderResponse(OrderResponseDTO dto) {
        log.info("Received order response for order {}: status={}, paymentStatus={}",
                dto.orderId(), dto.orderStatus(), dto.paymentStatus());
        orderService.updateOrderStatus(dto.orderId(), dto.orderStatus(), dto.paymentStatus(),
                SourceOfChange.DOMAIN_EVENT);
        log.info("Order {} updated to status {}", dto.orderId(), dto.orderStatus());
    }
}
