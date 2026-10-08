package in.btm.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_items", indexes = { @Index(name = "idx_order_item_order", columnList = "order_id"),
		@Index(name = "idx_order_item_product", columnList = "product_id") })
@Getter
@Setter
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderItemId;

	@Column(nullable = false)
	private Long productId;

	@Column(nullable = false)
	private String productName;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;
	
	@Column(length = 1000)
    private String productImageUrl;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal totalPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;
}