package com.diplom.levanteak.repository;

import com.diplom.levanteak.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order, Long> {
}
