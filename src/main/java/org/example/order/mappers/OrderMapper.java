package org.example.order.mappers;


import org.example.order.domain.dto.request.OrderItemRequestDTO;
import org.example.order.domain.dto.request.OrderKafkaRequestDTO;
import org.example.order.domain.dto.request.OrderRequestDTO;
import org.example.order.domain.dto.response.OrderResponseDTO;

import org.example.order.domain.dto.response.OrderVersionResponseDTO;
import org.example.order.domain.dto.response.OrdersListResponseDTO;
import org.example.order.domain.dto.response.AddressResponseDTO;
import org.example.order.domain.dto.response.CustomerResponseDTO;
import org.example.order.domain.dto.response.OrderItemResponseDTO;
import org.example.order.domain.dto.response.PersonResponseDTO;
import org.example.order.domain.entity.Order;
import org.example.order.domain.entity.OrderItem;
import org.example.order.domain.entity.OrderVersion;
import org.example.order.domain.entity.Customer;
import org.example.order.domain.entity.ShippingAddress;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderMapper {


    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "orderStatus", source = "status")
    OrderResponseDTO toOrderDto(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "shippingAddress", source = "customerDetailsRequest.addressDTO")
    Order toEntity(OrderRequestDTO order);

    OrderItem toOrderItem(OrderItemRequestDTO orderItemRequestDTO);

    @AfterMapping
    default void mapOrderItems(OrderRequestDTO source, @MappingTarget Order target) {
        source.orderItems().forEach(itemDto -> target.addOrderItem(toOrderItem(itemDto)));
    }

    @Mapping(target = "customerId", source = "order.customer.id")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "request", source = "request")
    OrderKafkaRequestDTO toKafkaRequest(Order order, OrderRequestDTO request);

    @Mapping(target = "orderStatusDTO.oldValue", source = "version.previousOrderStatus")
    @Mapping(target = "orderStatusDTO.newValue", source = "version.nextOrderStatus")
    @Mapping(target = "paymentStatusDto.oldValue", source = "version.previousPaymentStatus")
    @Mapping(target = "paymentStatusDto.newValue", source = "version.nextPaymentStatus")
    OrderVersionResponseDTO toOrderVersionDto(OrderVersion version);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "customerResponseDTO", source = ".")
    @Mapping(target = "orderItemResponseDTO", source = "orderItems")
    @Mapping(target = "totalCost", source = "totalAmount")
    OrdersListResponseDTO toOrderListDto(Order order);

    default CustomerResponseDTO toCustomerResponseDto(Order order) {
        return new CustomerResponseDTO(
                toPersonResponseDto(order.getCustomer()),
                toAddressResponseDto(order.getShippingAddress()));
    }

    PersonResponseDTO toPersonResponseDto(Customer customer);

    AddressResponseDTO toAddressResponseDto(ShippingAddress address);

    OrderItemResponseDTO toOrderItemResponseDto(OrderItem orderItem);
}
