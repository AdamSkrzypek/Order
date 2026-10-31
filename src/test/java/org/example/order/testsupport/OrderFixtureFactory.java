package org.example.order.testsupport;

import org.example.order.domain.entity.Address;
import org.example.order.domain.entity.Customer;
import org.example.order.domain.entity.Order;
import org.example.order.domain.entity.OrderItem;
import org.example.order.domain.entity.OrderVersion;
import org.example.order.domain.enums.OrderStatus;
import org.example.order.domain.enums.PaymentStatus;
import org.example.order.domain.enums.SourceOfChange;
import org.example.order.repositories.CustomerRepository;
import org.example.order.repositories.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderFixtureFactory {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public OrderFixtureFactory(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public Customer createCustomer() {
        String suffix = UUID.randomUUID().toString();
        return customerRepository.saveAndFlush(Customer.builder()
                .firstName("Test")
                .lastName("Customer")
                .email(suffix + "@example.com")
                .customerAddress(Address.builder()
                        .street("Testowa 1")
                        .city("Warszawa")
                        .postalCode("00-001")
                        .country("Polska")
                        .build())
                .build());
    }

    public Order createOrder(Customer customer, PaymentStatus paymentStatus) {
        return orderRepository.saveAndFlush(buildOrder(customer, paymentStatus, false));
    }

    public Order createOrderWithChildren(Customer customer, PaymentStatus paymentStatus) {
        return orderRepository.saveAndFlush(buildOrder(customer, paymentStatus, true));
    }

    public List<Order> createOrders(Customer customer, PaymentStatus paymentStatus, int count) {
        List<Order> orders = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            orders.add(buildOrder(customer, paymentStatus, false));
        }
        return orderRepository.saveAllAndFlush(orders);
    }

    private Order buildOrder(Customer customer, PaymentStatus paymentStatus, boolean withChildren) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setTotalAmount(BigDecimal.ONE);
        order.applyStatusUpdate(OrderStatus.NEW, paymentStatus);

        if (withChildren) {
            OrderItem item = new OrderItem();
            item.setProductId(UUID.randomUUID());
            item.setProductName("Test product");
            item.setQuantity(1);
            item.setUnitPrice(BigDecimal.ONE);
            order.addOrderItem(item);

            order.addOrderVersion(OrderVersion.statusChanged(
                    OrderStatus.NEW, PaymentStatus.NEW,
                    OrderStatus.NEW, paymentStatus,
                    SourceOfChange.API));
        }

        return order;
    }
}
