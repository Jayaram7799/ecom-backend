package in.btm.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import in.btm.dto.CategoryRequest;
import in.btm.dto.CategoryResponse;
import in.btm.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category category);

    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    void updateCategoryFromDto(
            CategoryRequest request,
            @MappingTarget Category category
    );
}