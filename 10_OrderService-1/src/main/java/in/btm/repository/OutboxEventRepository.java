package in.btm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import in.btm.entity.OutboxEvent;
import in.btm.entity.OutboxStatus;

import jakarta.persistence.LockModeType;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			    SELECT e
			    FROM OutboxEvent e
			    WHERE e.status = :status
			    ORDER BY e.id
			""")
	List<OutboxEvent> findPendingEvents(@Param("status") OutboxStatus status);
}