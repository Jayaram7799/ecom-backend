package in.btm.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import in.btm.enums.OrderStatus;
import in.btm.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders", indexes = { @Index(name = "idx_order_number", columnList = "orderNumber"),
		@Index(name = "idx_user_id", columnList = "userId") })
@Getter
@Setter
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderId;

	@Column(nullable = false, unique = true, length = 50)
	private String orderNumber;

	@Column(nullable = false)
	private Integer userId;

	@Column(nullable = false)
	private Long shippingAddressId;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(nullable = false)
	private Integer totalQuantity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus orderStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus paymentStatus;

	@Column(length = 100)
	private String razorpayOrderId;

	@Column(length = 100)
	private String razorpayPaymentId;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<OrderItem> orderItems = new ArrayList<>();

	@Version
	private Long version;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;
}