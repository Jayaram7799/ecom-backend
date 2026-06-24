package in.btm.spec;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import in.btm.entity.Product;

public final class ProductSpecification {

	private ProductSpecification() {
	}

	/**
	 * Active products only
	 */
	public static Specification<Product> isActive() {

		return (root, query, cb) -> cb.isTrue(root.get("active"));
	}

	/**
	 * Category filter
	 */
	public static Specification<Product> hasCategory(Integer categoryId) {

		return (root, query, cb) -> {

			if (categoryId == null) {
				return cb.conjunction();
			}

			return cb.equal(root.get("category").get("id"), categoryId);
		};
	}

	/**
	 * Search by product name
	 */
	public static Specification<Product> titleContains(String keyword) {

		return (root, query, cb) -> {

			if (keyword == null || keyword.isBlank()) {

				return cb.conjunction();
			}

			return cb.like(cb.lower(root.get("name")), "%" + keyword.trim().toLowerCase() + "%");
		};
	}

	/**
	 * Rating >= given rating
	 */
	public static Specification<Product> hasRating(Double rating) {

		return (root, query, cb) -> {

			if (rating == null) {
				return cb.conjunction();
			}

			return cb.greaterThanOrEqualTo(root.get("rating"), rating);
		};
	}

	/**
	 * Price >= minPrice
	 */
	public static Specification<Product> minPrice(Double minPrice) {

		return (root, query, cb) -> {

			if (minPrice == null) {
				return cb.conjunction();
			}

			return cb.greaterThanOrEqualTo(root.get("price"), BigDecimal.valueOf(minPrice));
		};
	}

	/**
	 * Price <= maxPrice
	 */
	public static Specification<Product> maxPrice(Double maxPrice) {

		return (root, query, cb) -> {

			if (maxPrice == null) {
				return cb.conjunction();
			}

			return cb.lessThanOrEqualTo(root.get("price"), BigDecimal.valueOf(maxPrice));
		};
	}

	/**
	 * Price between range
	 */
	public static Specification<Product> priceBetween(Double minPrice, Double maxPrice) {

		return (root, query, cb) -> {

			if (minPrice == null && maxPrice == null) {

				return cb.conjunction();
			}

			if (minPrice != null && maxPrice != null) {

				return cb.between(root.get("price"), BigDecimal.valueOf(minPrice), BigDecimal.valueOf(maxPrice));
			}

			if (minPrice != null) {

				return cb.greaterThanOrEqualTo(root.get("price"), BigDecimal.valueOf(minPrice));
			}

			return cb.lessThanOrEqualTo(root.get("price"), BigDecimal.valueOf(maxPrice));
		};
	}

	/**
	 * Exact rating match (optional)
	 */
	public static Specification<Product> exactRating(Double rating) {

		return (root, query, cb) -> {

			if (rating == null) {
				return cb.conjunction();
			}

			return cb.equal(root.get("rating"), rating);
		};
	}

	/**
	 * Category + Active
	 */
	public static Specification<Product> activeCategory(Integer categoryId) {
	    return isActive()
	            .and(hasCategory(categoryId));
	}
}