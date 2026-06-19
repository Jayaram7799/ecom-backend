package in.btm.service;

import in.btm.dto.ApiResponse;
import in.btm.dto.CartResponse;
import in.btm.dto.ProductDetailsResponse;
import in.btm.dto.ProductDto;
import in.btm.dto.ProductResponse;
import in.btm.entity.Cart;
import in.btm.entity.CartItem;
import in.btm.mapper.CartMapper;
import in.btm.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final ProductClient productClient;
	private final CartMapper cartMapper;

	@Override
	public CartResponse addItem(String email, Integer productId, Integer quantity) {

		validateQuantity(quantity);

		Cart cart = getOrCreateCart(email);

		ProductDto product = fetchProduct(productId);

		validateStock(product, quantity);

		CartItem item = findCartItem(cart, productId);

		if (item == null) {

			item = createCartItem(product, quantity);
			cart.getItems().add(item);

		} else {

			// refresh latest product snapshot
			item.setName(product.getName());
			item.setImageUrl(product.getImageUrl());
			item.setPrice(product.getPrice());

			item.setQuantity(item.getQuantity() + quantity);

			updateSubTotal(item);
		}

		updateCart(cart);

		log.info("Added product={} quantity={} for user={}", productId, quantity, email);

		return buildCartResponse(cart);
	}

	@Override
	public CartResponse removeItem(String email, Integer productId) {

		Cart cart = getExistingCart(email);

		cart.getItems().removeIf(item -> item.getProductId().equals(productId));

		updateCart(cart);

		log.info("Removed product={} from user={}", productId, email);

		return buildCartResponse(cart);
	}

	@Override
	public CartResponse getCart(String email) {

		Cart cart = getExistingCart(email);

		return buildCartResponse(cart);
	}

	@Override
	public void clearCart(String email) {

		cartRepository.delete(email);

		log.info("Cart cleared for user={}", email);
	}

// =====================================================
// PRIVATE METHODS
// =====================================================

	private Cart getOrCreateCart(String email) {

		return java.util.Optional.ofNullable(cartRepository.get(email)).orElseGet(() -> {

			Cart cart = new Cart();

			cart.setEmail(email);
			cart.setItems(new ArrayList<>());
			cart.setTotalItems(0);
			cart.setTotalPrice(BigDecimal.ZERO);
			cart.setCreatedAt(LocalDateTime.now());

			return cart;
		});
	}

	private Cart getExistingCart(String email) {

		return java.util.Optional.ofNullable(cartRepository.get(email))
				.orElseThrow(() -> new RuntimeException("Cart not found for user: " + email));
	}

	private ProductDto fetchProduct(Integer productId) {

		try {

			ApiResponse<ProductDetailsResponse> response = productClient.getProduct(productId);

			ProductResponse product = response.getData().getProduct();

			ProductDto dto = new ProductDto();

			dto.setId(product.getId());
			dto.setName(product.getName());
			dto.setImageUrl(product.getImageUrl());
			dto.setPrice(product.getPrice());

			// if ProductDto contains stock
			dto.setStock(product.getQuantity());

			return dto;

		} catch (Exception ex) {

			log.error("Failed to fetch product {}", productId, ex);

			throw new RuntimeException("Unable to fetch product: " + productId);
		}
	}

	private CartItem findCartItem(Cart cart, Integer productId) {

		return cart.getItems().stream().filter(item -> item.getProductId().equals(productId)).findFirst().orElse(null);
	}

	private CartItem createCartItem(ProductDto product, Integer quantity) {

		CartItem item = new CartItem();

		item.setProductId(product.getId());
		item.setName(product.getName());
		item.setImageUrl(product.getImageUrl());
		item.setPrice(product.getPrice());
		item.setQuantity(quantity);

		updateSubTotal(item);

		return item;
	}

	private void updateSubTotal(CartItem item) {

		item.setSubTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
	}

	private void updateCart(Cart cart) {

		recalculateTotals(cart);

		cart.setUpdatedAt(LocalDateTime.now());

		cartRepository.save(cart);
	}

	private void recalculateTotals(Cart cart) {

		BigDecimal totalPrice = BigDecimal.ZERO;
		int totalItems = 0;

		for (CartItem item : cart.getItems()) {

			totalPrice = totalPrice.add(item.getSubTotal());

			totalItems += item.getQuantity();
		}

		cart.setTotalPrice(totalPrice);
		cart.setTotalItems(totalItems);
	}

	private CartResponse buildCartResponse(Cart cart) {

		return cartMapper.toResponse(cart);
	}

	private void validateQuantity(Integer quantity) {

		if (quantity == null || quantity <= 0) {

			throw new IllegalArgumentException("Quantity must be greater than zero");
		}
	}

	private void validateStock(ProductDto product, Integer quantity) {

		if (product.getStock() < quantity) {

			throw new RuntimeException("Insufficient stock available");
		}
	}

}
