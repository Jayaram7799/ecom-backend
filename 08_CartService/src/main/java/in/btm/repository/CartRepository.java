package in.btm.repository;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import in.btm.entity.Cart;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CartRepository {

	private static final String KEY = "cart:";

	private final RedisTemplate<String, Cart> redisTemplate;

	public Cart get(String email) {
		return redisTemplate.opsForValue().get(KEY + email);
	}

	public void save(Cart cart) {

		redisTemplate.opsForValue().set(KEY + cart.getEmail(), cart, 30, TimeUnit.MINUTES);
	}

	public void delete(String email) {
		redisTemplate.delete(KEY + email);
	}
}