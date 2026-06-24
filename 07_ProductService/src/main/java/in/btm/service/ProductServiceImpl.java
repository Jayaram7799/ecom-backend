package in.btm.service;

import java.util.List;

import in.btm.dto.ProductPageResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import in.btm.dto.ProductDetailsResponse;
import in.btm.dto.ProductRequest;
import in.btm.dto.ProductResponse;
import in.btm.entity.Category;
import in.btm.entity.Product;
import in.btm.exception.CategoryNotFoundException;
import in.btm.exception.ProductNotFoundException;
import in.btm.mapper.ProductMapper;
import in.btm.repository.CategoryJpaRepository;
import in.btm.repository.ProductJpaRepository;
import in.btm.spec.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

	private static final String PRODUCT_CACHE = "product";
	private static final String PRODUCT_PAGE_CACHE = "product-page";

	private final ProductJpaRepository productRepository;
	private final CategoryJpaRepository categoryRepository;
	private final ProductMapper productMapper;

	@Override
	@Transactional
	@CacheEvict(value = PRODUCT_PAGE_CACHE, allEntries = true)
	public ProductResponse createProduct(ProductRequest request) {

		log.info("Creating product: {}", request.getName());

		Category category = categoryRepository.findById(request.getCategoryId())
				.orElseThrow(() ->
						new CategoryNotFoundException(
								"Category not found: " + request.getCategoryId()));

		Product product = productMapper.toEntity(request);
		product.setCategory(category);
		product.setActive(true);

		Product saved = productRepository.save(product);

		log.info("Product created successfully. id={}", saved.getId());

		return productMapper.toResponse(saved);
	}

	@Override
	@Cacheable(
			value = PRODUCT_CACHE,
			key = "#id",
			unless = "#result == null"
	)
	public ProductDetailsResponse getProductById(Integer id) {

		log.info("FETCHING PRODUCT FROM DATABASE id={}", id);

		Product product = productRepository.findById(id)
				.orElseThrow(() ->
						new ProductNotFoundException(
								"Product not found: " + id));

		if (!product.isActive()) {
			throw new ProductNotFoundException(
					"Inactive product: " + id);
		}

		ProductResponse productResponse =
				productMapper.toResponse(product);

		List<ProductResponse> similarProducts =
				productRepository
						.findByCategoryIdAndIdNot(
								product.getCategory().getId(),
								id)
						.stream()
						.filter(Product::isActive)
						.limit(4)
						.map(productMapper::toResponse)
						.toList();

		return ProductDetailsResponse.builder()
				.product(productResponse)
				.similarProducts(similarProducts)
				.build();
	}

	@Override
	@Cacheable(
	        value = PRODUCT_PAGE_CACHE,
	        key = "T(java.lang.String).format('%s-%s-%s-%s-%s-%s-%s-%s',"
	                + "#page,#size,#sortBy,#categoryId,"
	                + "#search,#rating,#minPrice,#maxPrice)"
	)
	public ProductPageResponse getAllProducts(
	        Integer page,
	        Integer size,
	        String sortBy,
	        Integer categoryId,
	        String search,
	        Double rating,
	        Double minPrice,
	        Double maxPrice) {

	    log.info(
	            "FETCHING PRODUCTS FROM DATABASE page={}, size={}, categoryId={}, search={}",
	            page, size, categoryId, search);

	    Pageable pageable = PageRequest.of(page, size, buildSort(sortBy));

	    // Base specification
	    Specification<Product> spec = ProductSpecification.isActive();

	    if (categoryId != null) {
	        spec = spec.and(ProductSpecification.hasCategory(categoryId));
	    }

	    if (search != null && !search.isBlank()) {
	        spec = spec.and(ProductSpecification.titleContains(search));
	    }

	    if (rating != null) {
	        spec = spec.and(ProductSpecification.hasRating(rating));
	    }

	    if (minPrice != null) {
	        spec = spec.and(ProductSpecification.minPrice(minPrice));
	    }

	    if (maxPrice != null) {
	        spec = spec.and(ProductSpecification.maxPrice(maxPrice));
	    }

	    Page<Product> result = productRepository.findAll(spec, pageable);

	    List<ProductResponse> content = result.getContent()
	            .stream()
	            .map(productMapper::toResponse)
	            .toList();

	    return ProductPageResponse.builder()
	            .content(content)
	            .page(result.getNumber())
	            .size(result.getSize())
	            .totalPages(result.getTotalPages())
	            .totalElements(result.getTotalElements())
	            .first(result.isFirst())
	            .last(result.isLast())
	            .numberOfElements(result.getNumberOfElements())
	            .empty(result.isEmpty())
	            .build();
	}

	@Override
	@Transactional
	@org.springframework.cache.annotation.Caching(evict = {
			@CacheEvict(value = PRODUCT_CACHE, key = "#id"),
			@CacheEvict(value = PRODUCT_PAGE_CACHE, allEntries = true)
	})
	public ProductResponse updateProduct(
			Integer id,
			ProductRequest request) {

		log.info("Updating product id={}", id);

		Product product = productRepository.findById(id)
				.orElseThrow(() ->
						new ProductNotFoundException(
								"Product not found: " + id));

		Category category = categoryRepository.findById(
						request.getCategoryId())
				.orElseThrow(() ->
						new CategoryNotFoundException(
								"Category not found: "
										+ request.getCategoryId()));

		productMapper.updateProductFromDto(request, product);

		product.setCategory(category);

		Product updated = productRepository.save(product);

		log.info("Product updated successfully. id={}", id);

		return productMapper.toResponse(updated);
	}

	@Override
	@Transactional
	@org.springframework.cache.annotation.Caching(evict = {
			@CacheEvict(value = PRODUCT_CACHE, key = "#id"),
			@CacheEvict(value = PRODUCT_PAGE_CACHE, allEntries = true)
	})
	public void deleteProduct(Integer id) {

		log.info("Deleting product id={}", id);

		Product product = productRepository.findById(id)
				.orElseThrow(() ->
						new ProductNotFoundException(
								"Product not found: " + id));

		product.setActive(false);

		productRepository.save(product);

		log.info("Product deleted successfully. id={}", id);
	}

	private Sort buildSort(String sortBy) {

		if (sortBy == null || sortBy.isBlank()) {
			return Sort.by(Sort.Direction.DESC, "id");
		}

		return switch (sortBy.toLowerCase()) {
			case "priceasc" -> Sort.by(Sort.Direction.ASC, "price");
			case "pricedesc" -> Sort.by(Sort.Direction.DESC, "price");
			case "nameasc" -> Sort.by(Sort.Direction.ASC, "name");
			case "namedesc" -> Sort.by(Sort.Direction.DESC, "name");
			case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
			default -> Sort.by(Sort.Direction.DESC, "id");
		};
	}
}