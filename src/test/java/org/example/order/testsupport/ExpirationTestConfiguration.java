package org.example.order.testsupport;

import org.example.order.repositories.CustomerRepository;
import org.example.order.repositories.OrderRepository;
import org.example.order.repositories.OutboxEventRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.ObjectMapper;

@TestConfiguration(proxyBeanMethods = false)
public class ExpirationTestConfiguration {

    @Bean
    @Primary
    public FailingOutboxEventService failingOutboxEventService(
            ObjectMapper objectMapper,
            OutboxEventRepository outboxEventRepository) {
        return new FailingOutboxEventService(objectMapper, outboxEventRepository);
    }

    @Bean
    public OrderFixtureFactory orderFixtureFactory(
            CustomerRepository customerRepository,
            OrderRepository orderRepository) {
        return new OrderFixtureFactory(customerRepository, orderRepository);
    }
}
