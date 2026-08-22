package org.example.ecommerceapplication.product.repository;

import org.example.ecommerceapplication.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    boolean existsByProductNameIgnoreCaseAndDeletedFalse(String productName);

    Page<Product> findAllByDeletedFalse(Pageable pageable);

    List<Product> findAllByDeletedFalse();

    Optional<Product> findByProductIdAndDeletedFalse(Long productId);
}
