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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders", indexes = {

		@Index(name = "idx_order_customer_email", columnList = "customer_email"),

		@Index(name = "idx_order_status", columnList = "order_status"),

		@Index(name = "idx_order_payment_status", columnList = "payment_status") })
@Getter
@Setter
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderId;

	@Column(nullable = false, unique = true, length = 30)
	private String orderNumber;

	@Column(name = "customer_email", nullable = false, length = 150)
	private String customerEmail;

	@Column(nullable = false)
	private Integer shippingAddressId;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Column(nullable = false)
	private Integer totalQuantity;

	@Enumerated(EnumType.STRING)
	@Column(name = "order_status", nullable = false, length = 30)
	private OrderStatus orderStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_status", nullable = false, length = 30)
	private PaymentStatus paymentStatus;

	@Column(name = "razorpay_order_id", length = 100)
	private String razorpayOrderId;

	@Column(name = "razorpay_payment_id", length = 100)
	private String razorpayPaymentId;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<OrderItem> orderItems = new ArrayList<>();

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private LocalDateTime updatedAt;
}