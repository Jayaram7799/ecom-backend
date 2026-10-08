package in.btm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.btm.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}