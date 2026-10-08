package in.btm.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import in.btm.config.FeignAuthConfig;
import in.btm.dto.ProductInternalResponse;

@FeignClient(
        name = "ProductService",
        configuration = FeignAuthConfig.class
)
public interface ProductClient {

    @GetMapping("/internal/products/{id}")
    ProductInternalResponse getProduct(@PathVariable Integer id);

}