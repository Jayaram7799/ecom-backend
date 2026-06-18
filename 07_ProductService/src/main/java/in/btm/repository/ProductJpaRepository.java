package in.btm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import in.btm.entity.Product;

public interface ProductJpaRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

	List<Product> findByCategoryIdAndIdNot(Integer categoryId, Integer id);
}