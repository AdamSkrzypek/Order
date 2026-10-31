package org.example.order.services.impl;

import org.example.order.TestcontainersConfiguration;
import org.example.order.domain.entity.Customer;
import org.example.order.domain.entity.Order;
import org.example.order.domain.enums.EventType;
import org.example.order.domain.enums.OrderStatus;
import org.example.order.domain.enums.OutboxEventStatus;
import org.example.order.domain.enums.PaymentStatus;
import org.example.order.domain.enums.SourceOfChange;
import org.example.order.repositories.CustomerRepository;
import org.example.order.exceptions.OrderNotFoundException;
import org.example.order.repositories.OrderRepository;
import org.example.order.repositories.OutboxEventRepository;
import org.example.order.testsupport.ExpirationTestConfiguration;
import org.example.order.testsupport.FailingOutboxEventService;
import org.example.order.testsupport.OrderFixtureFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({TestcontainersConfiguration.class, ExpirationTestConfiguration.class})
@SpringBootTest
class OrderDeletionServiceIntegrationTest {

    @Autowired
    private OrderDeletionService orderDeletionService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OrderFixtureFactory orderFixtureFactory;

    @Autowired
    private FailingOutboxEventService failingOutboxEventService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        failingOutboxEventService.failForOrder(null);
        outboxEventRepository.deleteAll();
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        ReflectionTestUtils.setField(orderDeletionService, "timeoutExpiration", 0L);
    }

    @Test
    void expiresNotPaidAndFailedOrdersWithChildrenAndOutboxEvents() {
        Customer customer = orderFixtureFactory.createCustomer();
        Order notPaid = orderFixtureFactory.createOrderWithChildren(customer, PaymentStatus.NOT_PAID);
        Order failed = orderFixtureFactory.createOrderWithChildren(customer, PaymentStatus.FAILED);

        orderDeletionService.deleteExpiredOrdersWithoutPayment();

        assertThat(orderRepository.findById(notPaid.getId())).isEmpty();
        assertThat(orderRepository.findById(failed.getId())).isEmpty();
        assertThat(countChildren("order_item", notPaid.getId())).isZero();
        assertThat(countChildren("order_versions", notPaid.getId())).isZero();

        var events = outboxEventRepository.findAll();
        assertThat(events)
                .hasSize(2)
                .allSatisfy(event -> {
                    assertThat(event.getEventType()).isEqualTo(EventType.ORDER_EXPIRED);
                    assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
                    assertThat(event.getPayload()).contains(event.getAggregateId().toString());
                });
        assertThat(events).extracting(event -> event.getAggregateId())
                .containsExactlyInAnyOrder(notPaid.getId(), failed.getId());
    }

    @Test
    void keepsPaidAndUnresolvedOrders() {
        Customer customer = orderFixtureFactory.createCustomer();
        Order paid = orderFixtureFactory.createOrder(customer, PaymentStatus.PAID);
        Order unresolved = orderFixtureFactory.createOrder(customer, PaymentStatus.NEW);

        orderDeletionService.deleteExpiredOrdersWithoutPayment();

        assertThat(orderRepository.findById(paid.getId())).isPresent();
        assertThat(orderRepository.findById(unresolved.getId())).isPresent();
        assertThat(outboxEventRepository.findAll()).isEmpty();
    }

    @Test
    void failureForOneOrderDoesNotBlockOthersAndRollsBackItsOutboxEvent() {
        Customer customer = orderFixtureFactory.createCustomer();
        Order failingOrder = orderFixtureFactory.createOrder(customer, PaymentStatus.NOT_PAID);
        Order successfulOrder = orderFixtureFactory.createOrder(customer, PaymentStatus.NOT_PAID);
        failingOutboxEventService.failForOrder(failingOrder.getId());

        orderDeletionService.deleteExpiredOrdersWithoutPayment();

        assertThat(orderRepository.findById(failingOrder.getId())).isPresent();
        assertThat(orderRepository.findById(successfulOrder.getId())).isEmpty();
        assertThat(outboxEventRepository.findAll())
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getAggregateId()).isEqualTo(successfulOrder.getId());
                    assertThat(event.getEventType()).isEqualTo(EventType.ORDER_EXPIRED);
                });
    }

    @Test
    void processesMultipleBatchesAndIsIdempotentAcrossRuns() {
        Customer customer = orderFixtureFactory.createCustomer();
        orderFixtureFactory.createOrders(customer, PaymentStatus.NOT_PAID, 101);

        orderDeletionService.deleteExpiredOrdersWithoutPayment();

        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(outboxEventRepository.findAll()).hasSize(101);

        orderDeletionService.deleteExpiredOrdersWithoutPayment();

        assertThat(outboxEventRepository.findAll()).hasSize(101);
    }

    @Test
    void delayedPaymentEventDoesNotRecreateAnExpiredOrder() {
        Order expired = orderFixtureFactory.createOrder(
                orderFixtureFactory.createCustomer(), PaymentStatus.NOT_PAID);
        UUID expiredOrderId = expired.getId();

        orderDeletionService.deleteExpiredOrdersWithoutPayment();

        assertThatThrownBy(() -> orderService.updateOrderStatus(
                expiredOrderId,
                OrderStatus.PENDING,
                PaymentStatus.PAID,
                SourceOfChange.DOMAIN_EVENT))
                .isInstanceOf(OrderNotFoundException.class);
        assertThat(orderRepository.findById(expiredOrderId)).isEmpty();
    }

    private long countChildren(String table, UUID orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE order_id = ?",
                Long.class,
                orderId);
    }
}
