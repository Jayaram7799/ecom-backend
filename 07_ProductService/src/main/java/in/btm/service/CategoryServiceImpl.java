package in.btm.service;

import java.util.List;

import in.btm.dto.CategoryListResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.btm.dto.CategoryRequest;
import in.btm.dto.CategoryResponse;
import in.btm.entity.Category;
import in.btm.exception.CategoryAlreadyExistsException;
import in.btm.exception.CategoryNotFoundException;
import in.btm.mapper.CategoryMapper;
import in.btm.repository.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

	private static final String CATEGORY_CACHE = "category";
	private static final String CATEGORY_LIST_CACHE = "category-list";

	private final CategoryJpaRepository categoryJpaRepository;
	private final CategoryMapper categoryMapper;

	@Override
	@Transactional
	@CacheEvict(value = CATEGORY_LIST_CACHE, allEntries = true)
	public CategoryResponse createCategory(CategoryRequest request) {

		log.info("Creating category: {}", request.getName());

		if (categoryJpaRepository.existsByNameIgnoreCase(request.getName())) {
			throw new CategoryAlreadyExistsException(
					"Category already exists with name: " + request.getName());
		}

		Category category = categoryMapper.toEntity(request);
		category.setActive(true);

		Category saved = categoryJpaRepository.save(category);

		log.info("Category created successfully. id={}", saved.getId());

		return categoryMapper.toResponse(saved);
	}

	@Override
	@Cacheable(
			value = CATEGORY_CACHE,
			key = "#id",
			unless = "#result == null"
	)
	public CategoryResponse getCategoryById(Integer id) {

		log.info("FETCHING CATEGORY FROM DATABASE id={}", id);

		Category category = categoryJpaRepository.findById(id)
				.orElseThrow(() ->
						new CategoryNotFoundException(
								"Category not found with id: " + id));

		if (!category.isActive()) {
			throw new CategoryNotFoundException(
					"Category is inactive with id: " + id);
		}

		return categoryMapper.toResponse(category);
	}

	@Cacheable(value = CATEGORY_LIST_CACHE, key = "'all'")
	public CategoryListResponse getAllCategories() {

		List<CategoryResponse> categories =
				categoryJpaRepository.findAll()
						.stream()
						.filter(Category::isActive)
						.map(categoryMapper::toResponse)
						.toList();

		return CategoryListResponse.builder()
				.categories(categories)
				.build();
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = CATEGORY_CACHE, key = "#id"),
			@CacheEvict(value = CATEGORY_LIST_CACHE, allEntries = true)
	})
	public CategoryResponse updateCategory(
			Integer id,
			CategoryRequest request) {

		log.info("Updating category id={}", id);

		Category category = categoryJpaRepository.findById(id)
				.orElseThrow(() ->
						new CategoryNotFoundException(
								"Category not found with id: " + id));

		if (categoryJpaRepository.existsByNameIgnoreCase(request.getName())
				&& !category.getName().equalsIgnoreCase(request.getName())) {

			throw new CategoryAlreadyExistsException(
					"Category already exists with name: " + request.getName());
		}

		categoryMapper.updateCategoryFromDto(request, category);

		Category updated = categoryJpaRepository.save(category);

		log.info("Category updated successfully id={}", id);

		return categoryMapper.toResponse(updated);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = CATEGORY_CACHE, key = "#id"),
			@CacheEvict(value = CATEGORY_LIST_CACHE, allEntries = true)
	})
	public void deleteCategory(Integer id) {

		log.info("Deleting category id={}", id);

		Category category = categoryJpaRepository.findById(id)
				.orElseThrow(() ->
						new CategoryNotFoundException(
								"Category not found with id: " + id));

		category.setActive(false);

		categoryJpaRepository.save(category);

		log.info("Category deleted successfully id={}", id);
	}
}