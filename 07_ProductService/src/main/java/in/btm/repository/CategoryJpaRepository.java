package in.btm.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import in.btm.entity.Category;


public interface CategoryJpaRepository extends JpaRepository<Category, Integer> {

	boolean existsByNameIgnoreCase(String name);
}