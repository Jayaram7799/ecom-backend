package in.btm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import in.btm.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUserId(Integer customerId);

	@Query("""
			SELECT o
			FROM Order o
			LEFT JOIN FETCH o.orderItems
			WHERE o.orderId = :orderId
			""")
	Optional<Order> findOrderWithItems(Long orderId);
}