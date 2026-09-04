package org.example.ecommerceapplication.orders.repository;

import org.example.ecommerceapplication.dashboard.projection.TopProductProjection;
import org.example.ecommerceapplication.orders.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            select p.productId as productId,
                   p.productName as productName,
                   p.image as image,
                   sum(oi.quantity) as unitsSold
            from OrderItem oi
            join oi.order o
            join oi.product p
            where o.orderDate between :from and :to
              and p.deleted = false
            group by p.productId, p.productName, p.image
            order by sum(oi.quantity) desc, p.productName asc
            """)
    List<TopProductProjection> findTopSellingProducts(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );
}
