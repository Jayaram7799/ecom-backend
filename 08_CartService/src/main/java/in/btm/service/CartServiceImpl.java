
package in.btm.service;

import in.btm.dto.ApiResponse;
import in.btm.dto.CartResponse;
import in.btm.dto.ProductDetailsResponse;
import in.btm.dto.ProductDto;
import in.btm.dto.ProductResponse;
import in.btm.entity.Cart;
import in.btm.entity.CartItem;
import in.btm.exception.CartItemNotFoundException;
import in.btm.exception.InsufficientStockException;
import in.btm.exception.InvalidQuantityException;
import in.btm.exception.ProductFetchException;
import in.btm.mapper.CartMapper;
import in.btm.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

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

		CartItem item = findCartItem(cart, productId);

		if (item == null) {

			validateStock(product, quantity);

			item = createCartItem(product, quantity);

			cart.getItems().add(item);

		} else {

			int newQuantity = item.getQuantity() + quantity;

			validateStock(product, newQuantity);

			item.setName(product.getName());
			item.setImageUrl(product.getImageUrl());
			item.setPrice(product.getPrice());
			item.setQuantity(newQuantity);

			updateSubTotal(item);
		}

		updateCart(cart);

		log.info("Added product={} quantity={} for user={}", productId, quantity, email);

		return buildCartResponse(cart);
	}

	@Override
	public CartResponse removeItem(String email, Integer productId) {

		Cart cart = getOrCreateCart(email);

		cart.getItems().removeIf(item -> item.getProductId().equals(productId));

		updateCart(cart);

		log.info("Removed product={} from user={}", productId, email);

		return buildCartResponse(cart);
	}

	@Override
	public CartResponse getCart(String email) {

		return buildCartResponse(getOrCreateCart(email));
	}

	@Override
	public void clearCart(String email) {

		cartRepository.delete(email);

		log.info("Cart cleared for user={}", email);
	}

	@Override
	public CartResponse incrementQuantity(String email, Integer productId) {

		Cart cart = getOrCreateCart(email);

		CartItem item = findCartItem(cart, productId);

		if (item == null) {
			throw new CartItemNotFoundException(productId);
		}

		ProductDto product = fetchProduct(productId);

		int newQuantity = item.getQuantity() + 1;

		validateStock(product, newQuantity);

		item.setQuantity(newQuantity);

		updateSubTotal(item);

		updateCart(cart);

		return buildCartResponse(cart);
	}

	@Override
	public CartResponse decrementQuantity(String email, Integer productId) {

		Cart cart = getOrCreateCart(email);

		CartItem item = findCartItem(cart, productId);

		if (item == null) {
			throw new CartItemNotFoundException(productId);
		}

		if (item.getQuantity() <= 1) {

			cart.getItems().remove(item);

		} else {

			item.setQuantity(item.getQuantity() - 1);

			updateSubTotal(item);
		}

		updateCart(cart);

		return buildCartResponse(cart);
	}

	// =====================================================
	// PRIVATE METHODS
	// =====================================================

	private Cart getOrCreateCart(String email) {

		return Optional.ofNullable(cartRepository.get(email)).orElseGet(() -> createEmptyCart(email));
	}

	private Cart createEmptyCart(String email) {

		Cart cart = new Cart();

		cart.setEmail(email);
		cart.setItems(new ArrayList<>());
		cart.setTotalItems(0);
		cart.setTotalPrice(BigDecimal.ZERO);
		cart.setCreatedAt(LocalDateTime.now());

		return cart;
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
			dto.setStock(product.getQuantity());

			return dto;

		} catch (Exception ex) {

			log.error("Failed to fetch product {}", productId, ex);

			throw new ProductFetchException(productId);
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
			throw new InvalidQuantityException();
		}
	}

	private void validateStock(ProductDto product, Integer requestedQty) {

		if (product.getStock() < requestedQty) {

			throw new InsufficientStockException(product.getName(), product.getStock(), requestedQty);
		}
	}
}
