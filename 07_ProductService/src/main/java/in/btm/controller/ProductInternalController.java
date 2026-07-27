package in.btm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.btm.dto.ProductInternalResponse;
import in.btm.service.ProductService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class ProductInternalController {

	private final ProductService productService;

	@GetMapping("/{id}")
	public ProductInternalResponse getProduct(@PathVariable Integer id) {
		return productService.getProductForOrder(id);
	}
}