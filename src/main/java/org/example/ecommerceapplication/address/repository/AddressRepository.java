package org.example.ecommerceapplication.address.repository;


import org.example.ecommerceapplication.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository
        extends JpaRepository<Address, Long> {
}