package in.btm.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "outbox_events", indexes = { @Index(name = "idx_outbox_status_id", columnList = "status,id") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private UUID eventId;

	@Column(nullable = false, length = 100)
	private String aggregateType;

	@Column(nullable = false, length = 100)
	private String aggregateId;

	@Column(nullable = false, length = 100)
	private String eventType;

	@Lob
	@Column(nullable = false)
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OutboxStatus status;

	@Column(nullable = false)
	private Integer retryCount;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime processedAt;

	private LocalDateTime lastAttemptAt;

	@Column(length = 1000)
	private String errorMessage;
}
