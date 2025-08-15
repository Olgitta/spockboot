package com.olg.postgressql.orders;

import com.olg.postgressql.orders.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
