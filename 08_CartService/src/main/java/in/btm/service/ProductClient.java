package in.btm.service;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import in.btm.config.FeignAuthConfig;
import in.btm.dto.ApiResponse;
import in.btm.dto.ProductDetailsResponse;

@FeignClient(
        name = "ProductService",
        configuration = FeignAuthConfig.class
)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ApiResponse<ProductDetailsResponse> getProduct(
            @PathVariable Integer id
    );
}