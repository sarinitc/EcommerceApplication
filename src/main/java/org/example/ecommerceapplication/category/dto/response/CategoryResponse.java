package org.example.ecommerceapplication.category.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
private  Long categoryId;
private  String categoryName;


}
