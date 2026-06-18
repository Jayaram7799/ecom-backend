package in.btm.service;


import in.btm.dto.CategoryListResponse;
import in.btm.dto.CategoryRequest;
import in.btm.dto.CategoryResponse;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse getCategoryById(Integer id);

    CategoryListResponse  getAllCategories();

    CategoryResponse updateCategory(Integer id, CategoryRequest request);

    void deleteCategory(Integer id);
}