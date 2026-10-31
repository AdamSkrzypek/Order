package org.example.order.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order.domain.entity.Order;
import org.example.order.domain.dto.event.OrderExpiredEvent;
import org.example.order.domain.enums.AggregateType;
import org.example.order.domain.enums.EventType;
import org.example.order.domain.enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.example.order.exceptions.OrderNotFoundException;
import org.example.order.repositories.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDeletionService {

     private static final ZoneId ZONE = ZoneId.of("Europe/Warsaw");
     private static final int BATCH_SIZE = 100;
     private static final Set<PaymentStatus> EXPIRABLE_PAYMENT_STATUSES =
             Set.of(PaymentStatus.NOT_PAID, PaymentStatus.FAILED);

     private final OrderRepository orderRepository;
     private final OutboxEventService outboxEventService;
     private final TransactionTemplate expirationTransaction;

     @Value("${payment.timeout.expiration}")
     private Long timeoutExpiration;

     public void deleteOrder(UUID orderId) {
          var order = orderRepository.findById(orderId)
                  .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
          order.setDeleted(Boolean.TRUE);
     }

     @Scheduled(cron = "0 * * * * *", zone = "Europe/Warsaw")
     public void deleteExpiredOrdersWithoutPayment() {
          Instant cutoff = ZonedDateTime.now(ZONE)
                  .minusMinutes(timeoutExpiration)
                  .toInstant();

          processExpiredOrders(cutoff);
     }

     private void processExpiredOrders(Instant cutoff) {
          UUID afterId = null;
          List<UUID> orderIds;
          do {
               orderIds = orderRepository.findExpiredOrderIds(cutoff, afterId, BATCH_SIZE);
               processExpiredBatch(orderIds, cutoff);
               afterId = orderIds.isEmpty() ? afterId : orderIds.getLast();
          } while (orderIds.size() == BATCH_SIZE);
     }

     private void processExpiredBatch(List<UUID> orderIds, Instant cutoff) {
          orderIds.forEach(orderId -> {
               try {
                    expireOrder(orderId, cutoff);
               } catch (OrderExpirationException e) {
                    log.error("Could not expire order {}", orderId, e);
               }
          });
     }

     private void expireOrder(UUID orderId, Instant cutoff) {
          try {
               expirationTransaction.executeWithoutResult(status -> expireOrderIfEligible(orderId, cutoff));
          } catch (RuntimeException e) {
               throw new OrderExpirationException(orderId, e);
          }
     }

     @Scheduled(cron = "0 0 23 1 * ?", zone = "Europe/Warsaw")
     @Transactional
     public void deleteOrdersOlderThanTwoYears() {
          Instant cutoff = ZonedDateTime.now(ZONE)
                  .minusYears(2)
                  .toInstant();

          orderRepository.deleteOldOrderItems(cutoff);
          orderRepository.deleteOldOrderVersions(cutoff);
          orderRepository.deleteOldOrders(cutoff);
     }
     private void expireOrderIfEligible(UUID orderId, Instant cutoff) {
          Order order = orderRepository.findByIdForUpdate(orderId).orElse(null);
          if (order == null
                  || order.getCreatedAt().isAfter(cutoff)
                  || !EXPIRABLE_PAYMENT_STATUSES.contains(order.getPaymentStatus())) {
               return;
          }

          outboxEventService.recordEvent(
                  AggregateType.ORDER,
                  order.getId(),
                  EventType.ORDER_EXPIRED,
                  new OrderExpiredEvent(order.getId()));
          orderRepository.delete(order);
     }
}
