package org.example.ecommerceapplication.user.repository;

import org.example.ecommerceapplication.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            select count(distinct u)
            from User u
            join u.roles r
            where r.name = :roleName
            """)
    long countUsersByRole(@Param("roleName") String roleName);
    @Query("""
    SELECT DISTINCT u
    FROM User u
    JOIN u.roles r
    WHERE r.name = 'CUSTOMER'
      AND (
          :search IS NULL
          OR :search = ''
          OR LOWER(u.username)
                LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(u.email)
                LIKE LOWER(CONCAT('%', :search, '%'))
      )
""")
    Page<User> findCustomers(
            @Param("search") String search,
            Pageable pageable
    );
}
