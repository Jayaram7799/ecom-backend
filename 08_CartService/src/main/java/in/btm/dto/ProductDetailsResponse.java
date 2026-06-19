package in.btm.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsResponse {

    private ProductResponse product;
    private List<ProductResponse> similarProducts;
}
