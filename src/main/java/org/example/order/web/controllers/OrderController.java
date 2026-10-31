package org.example.order.web.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;

import org.example.order.domain.dto.request.OrderRequestDTO;
import org.example.order.domain.dto.response.OrderResponseDTO;
import org.example.order.domain.dto.response.OrderVersionsPageResponseDTO;
import org.example.order.domain.dto.response.OrdersPageResponseDTO;
import org.example.order.services.impl.OrderService;
import org.example.order.services.impl.OrderVersionServiceImpl;
import org.example.order.web.request.DefaultPageRequest;
import org.example.order.web.request.FilterDto;
import org.example.order.web.request.SortField;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("v1/api")
@AllArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;
    private final OrderVersionServiceImpl orderVersionService;

    @PostMapping("/orders")
    public OrderResponseDTO createOrder(@RequestBody @Valid OrderRequestDTO orderDto) {
        return orderService.createOrder(orderDto);
    }

    @GetMapping("/orders")
    public OrdersPageResponseDTO getAllOrders(
            @Min(0) @RequestParam(defaultValue = "0") Integer page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) SortField sortBy,
            @RequestParam(required = false) Sort.Direction direction) {
        DefaultPageRequest request =
                DefaultPageRequest.fromQueryParams(page, size, sortBy, direction);
        return orderService.getAllOrders(request);
    }

    @GetMapping("/orders/{id}")
    public OrderResponseDTO getOrderById(@PathVariable UUID id) {
        return orderService.getOrderById(id);
    }

    @PutMapping("/orders/{id}/cancel")
    public void cancelOrder(@PathVariable UUID id) {
        orderService.cancelOrder(id);
    }

    @PatchMapping("/orders/{id}/update")
    public void updateOrderAdditionalNotes(@PathVariable UUID id, @RequestBody @NotEmpty String orderNotes) {
        orderService.updateOrderAdditionalNotes(id,orderNotes);
    }

    @DeleteMapping("/orders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDeleteOrder(@PathVariable UUID id) {
        orderService.softDeleteOrder(id);
    }

    @PutMapping("/orders/{id}/restore")
    public void restoreCanceledOrder(@PathVariable UUID id) {
        orderService.restoreCanceledOrder(id);
    }

    @GetMapping("orders/{id}/versions")
    public OrderVersionsPageResponseDTO getOrderVersions(
            @PathVariable UUID id,
            @Min(0) @RequestParam(defaultValue = "0") Integer page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) SortField sortBy,
            @RequestParam(required = false) Sort.Direction direction,
            @ModelAttribute FilterDto filters) {

        DefaultPageRequest pageRequest =
                DefaultPageRequest.fromQueryParams(page, limit, sortBy, direction);
        return orderVersionService.getOrderVersions(id, filters, pageRequest);
    }
}
