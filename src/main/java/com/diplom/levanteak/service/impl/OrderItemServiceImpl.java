package com.diplom.levanteak.service.impl;

import com.diplom.levanteak.exception.NotFoundException;
import com.diplom.levanteak.service.interf.OrderItemService;
import com.diplom.levanteak.service.interf.UserService;
import com.diplom.levanteak.specification.OrderItemSpecification;
import com.diplom.levanteak.dto.OrderItemDto;
import com.diplom.levanteak.dto.OrderRequest;
import com.diplom.levanteak.dto.Response;
import com.diplom.levanteak.entity.Order;
import com.diplom.levanteak.entity.OrderItem;
import com.diplom.levanteak.entity.Product;
import com.diplom.levanteak.entity.User;
import com.diplom.levanteak.enums.OrderStatus;
import com.diplom.levanteak.mapper.EntityDtoMapper;
import com.diplom.levanteak.repository.OrderItemRepo;
import com.diplom.levanteak.repository.OrderRepo;
import com.diplom.levanteak.repository.ProductRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {


    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final ProductRepo productRepo;
    private final UserService userService;
    private final EntityDtoMapper entityDtoMapper;


    @Override
    public Response placeOrder(OrderRequest orderRequest) {
        log.info("Placing a new order for user.");
        User user = userService.getLoginUser();
        //map order request items to order entities
        log.debug("Current user ID: {}", user.getId());


        List<OrderItem> orderItems = orderRequest.getItems().stream().map(orderItemRequest -> {
            log.debug("Fetching product with ID: {}", orderItemRequest.getProductId());
            Product product = productRepo.findById(orderItemRequest.getProductId())
                    .orElseThrow(() -> {
                        log.warn("Product not found with ID: {}", orderItemRequest.getProductId());
                        return new NotFoundException("Product Not Found");
                    });

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(orderItemRequest.getQuantity());
            orderItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity()))); //set price according to the quantity
            orderItem.setStatus(OrderStatus.PENDING);
            orderItem.setUser(user);
            log.debug("Created OrderItem for product '{}', quantity: {}, total price: {}",
                    product.getName(), orderItem.getQuantity(), orderItem.getPrice());
            return orderItem;

        }).collect(Collectors.toList());

        //calculate the total price
        BigDecimal totalPrice = orderRequest.getTotalPrice() != null && orderRequest.getTotalPrice().compareTo(BigDecimal.ZERO) > 0
                ? orderRequest.getTotalPrice()
                : orderItems.stream().map(OrderItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Calculated total order price: {}", totalPrice);

        //create order entity
        Order order = new Order();
        order.setOrderItemList(orderItems);
        order.setTotalPrice(totalPrice);

        //set the order reference in each orderitem
        orderItems.forEach(orderItem -> orderItem.setOrder(order));

        orderRepo.save(order);
        log.info("Order placed successfully with {} items", orderItems.size());


        return Response.builder()
                .status(200)
                .message("Order was successfully placed")
                .build();

    }

    @Override
    public Response updateOrderItemStatus(Long orderItemId, String status) {
        log.info("Updating status for OrderItem with ID: {}", orderItemId);

        OrderItem orderItem = orderItemRepo.findById(orderItemId)
                .orElseThrow(() -> {
                    log.warn("OrderItem not found with ID: {}", orderItemId);
                    return new NotFoundException("Order Item not found");
                });

        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        orderItem.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        orderItemRepo.save(orderItem);
        log.info("OrderItem status updated to '{}' for ID: {}", newStatus, orderItemId);

        return Response.builder()
                .status(200)
                .message("Order status updated successfully")
                .build();
    }

    @Override
    public Response filterOrderItems(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Long itemId, Pageable pageable) {
        log.info("Filtering OrderItems with status: {}, startDate: {}, endDate: {}, itemId: {}", status, startDate, endDate, itemId);
        Specification<OrderItem> spec = Specification.where(OrderItemSpecification.hasStatus(status))
                .and(OrderItemSpecification.createdBetween(startDate, endDate))
                .and(OrderItemSpecification.hasItemId(itemId));

        Page<OrderItem> orderItemPage = orderItemRepo.findAll(spec, pageable);

        if (orderItemPage.isEmpty()) {
            log.warn("No order items found for the provided filters");
            throw new NotFoundException("No Order Found");
        }
        List<OrderItemDto> orderItemDtos = orderItemPage.getContent().stream()
                .map(entityDtoMapper::mapOrderItemToDtoPlusProductAndUser)
                .collect(Collectors.toList());
        log.info("Found {} order items matching filter criteria", orderItemDtos.size());


        return Response.builder()
                .status(200)
                .orderItemList(orderItemDtos)
                .totalPage(orderItemPage.getTotalPages())
                .totalElement(orderItemPage.getTotalElements())
                .build();
    }

}
