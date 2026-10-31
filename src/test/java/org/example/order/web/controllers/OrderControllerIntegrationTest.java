package org.example.order.web.controllers;

import org.example.order.TestcontainersConfiguration;
import org.example.order.domain.dto.request.AddressRequestDTO;
import org.example.order.domain.dto.request.CustomerDetailsRequestDTO;
import org.example.order.domain.dto.request.OrderItemRequestDTO;
import org.example.order.domain.dto.request.OrderRequestDTO;
import org.example.order.domain.dto.request.PersonRequestDTO;
import org.example.order.domain.dto.response.OrderResponseDTO;
import org.example.order.domain.entity.Order;
import org.example.order.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OrderRepository orderRepository;

    @Test
    @Transactional
    void createOrder_persistsItemsWithBackReferenceAndCorrectTotal() throws Exception {
        OrderRequestDTO request = sampleOrderRequest("total-" + UUID.randomUUID() + "@example.com");

        String responseJson = mockMvc.perform(post("/v1/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("NEW"))
                .andReturn().getResponse().getContentAsString();

        OrderResponseDTO response = objectMapper.readValue(responseJson, OrderResponseDTO.class);

        Order saved = orderRepository.findById(response.orderId()).orElseThrow();
        assertThat(saved.getOrderItems()).hasSize(2);
        assertThat(saved.getOrderItems()).allMatch(item -> item.getOrder() == saved);
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("35.00");
    }

    private OrderRequestDTO sampleOrderRequest(String email) {
        PersonRequestDTO person = new PersonRequestDTO("Jan", "Kowalski", email, "+48123456789");
        AddressRequestDTO address = new AddressRequestDTO("Testowa 1", "Warszawa", "00-001", "Polska");
        CustomerDetailsRequestDTO customer = new CustomerDetailsRequestDTO(person, address);
        List<OrderItemRequestDTO> items = List.of(
                new OrderItemRequestDTO(UUID.randomUUID(), "Widget", 2, new BigDecimal("10.00")),
                new OrderItemRequestDTO(UUID.randomUUID(), "Gadget", 1, new BigDecimal("15.00"))
        );
        return new OrderRequestDTO(customer, items);
    }
}
