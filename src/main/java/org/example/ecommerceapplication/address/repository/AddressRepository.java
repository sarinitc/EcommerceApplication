package org.example.ecommerceapplication.address.repository;


import org.example.ecommerceapplication.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository
        extends JpaRepository<Address, Long> {

    @Query("""
            select (count(address) > 0)
            from Address address join address.users user
            where lower(user.email) = lower(:email)
              and lower(address.street) = lower(:street)
              and ((:buildingName is null and address.buildingName is null) or lower(address.buildingName) = lower(:buildingName))
              and lower(address.city) = lower(:city)
              and ((:state is null and address.state is null) or lower(address.state) = lower(:state))
              and lower(address.country) = lower(:country)
              and lower(address.pincode) = lower(:pincode)
            """)
    boolean existsDuplicateForUser(
            @Param("email") String email,
            @Param("street") String street,
            @Param("buildingName") String buildingName,
            @Param("city") String city,
            @Param("state") String state,
            @Param("country") String country,
            @Param("pincode") String pincode
    );

    Optional<Address> findByAddressIdAndUsersEmail(
             Long addressId,
             String email

    );

}
