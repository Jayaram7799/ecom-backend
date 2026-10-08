package in.btm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.btm.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByCustomerEmail(String customerEmail);
	
	List<Order> findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(
            String customerEmail
    );
}