package com.diplom.levanteak.service.interf;

import com.diplom.levanteak.dto.OrderRequest;
import com.diplom.levanteak.dto.Response;
import com.diplom.levanteak.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderItemService {
    Response placeOrder(OrderRequest orderRequest);
    Response updateOrderItemStatus(Long orderItemId, String status);
    Response filterOrderItems(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Long itemId, Pageable pageable);
}
