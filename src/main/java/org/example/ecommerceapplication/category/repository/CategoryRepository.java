package org.example.ecommerceapplication.category.repository;

import org.example.ecommerceapplication.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    boolean existsByCategoryNameIgnoreCase(String categoryName);

}
