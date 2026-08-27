package org.example.ecommerceapplication.address.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.address.dto.request.AddressRequest;
import org.example.ecommerceapplication.address.dto.response.AddressResponse;
import org.example.ecommerceapplication.address.entity.Address;
import org.example.ecommerceapplication.address.repository.AddressRepository;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;


    @Transactional
    public AddressResponse createAddress(
            String email,
            AddressRequest request
    ) {

        // 1. Find logged-in user
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );


        // 2. Create Address entity
        Address address = Address.builder()
                .street(request.getStreet())
                .buildingName(request.getBuildingName())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .pincode(request.getPincode())
                .build();


        // 3. Save Address first
        Address savedAddress =
                addressRepository.save(address);


        // 4. Connect User <-> Address
        user.getAddresses().add(savedAddress);

        savedAddress.getUsers().add(user);


        // 5. Save owning side
        userRepository.save(user);


        // 6. Convert Entity -> Response
        return mapToAddressResponse(savedAddress);
    }


    private AddressResponse mapToAddressResponse(
            Address address
    ) {

        return AddressResponse.builder()
                .addressId(address.getAddressId())
                .street(address.getStreet())
                .buildingName(address.getBuildingName())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .pincode(address.getPincode())
                .build();
    }
}