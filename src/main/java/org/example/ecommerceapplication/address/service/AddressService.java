package org.example.ecommerceapplication.address.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.address.dto.request.AddressRequest;
import org.example.ecommerceapplication.address.dto.response.AddressResponse;
import org.example.ecommerceapplication.address.entity.Address;
import org.example.ecommerceapplication.address.exception.AddressAlreadyExistsException;
import org.example.ecommerceapplication.address.repository.AddressRepository;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;


    // =====================================================
    // CREATE ADDRESS
    // =====================================================

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

        // Do not add the same complete address more than once for this user.
        if (addressRepository.existsDuplicateForUser(
                email,
                request.getStreet(),
                request.getBuildingName(),
                request.getCity(),
                request.getState(),
                request.getCountry(),
                request.getPincode()
        )) {
            throw new AddressAlreadyExistsException();
        }

        // 2. Create Address entity
        Address address = Address.builder()
                .street(request.getStreet())
                .buildingName(request.getBuildingName())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .pincode(request.getPincode())
                .build();


        // 3. Save address
        Address savedAddress =
                addressRepository.save(address);


        // 4. Connect User -> Address
        user.getAddresses().add(savedAddress);

        // Keep both sides synchronized in Java
        savedAddress.getUsers().add(user);


        // 5. Save owning side
        userRepository.save(user);


        // 6. Convert Entity -> Response
        return mapToAddressResponse(savedAddress);
    }


    // =====================================================
    // GET ADDRESS BY ID
    // =====================================================

    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long addressId) {

        Address address = addressRepository
                .findById(addressId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Address not found with id: " + addressId
                        )
                );

        return mapToAddressResponse(address);
    }


    // =====================================================
    // ENTITY -> RESPONSE DTO
    // =====================================================

    private AddressResponse mapToAddressResponse(
            Address address
    ) {

        return AddressResponse.builder()
                .addressId(
                        address.getAddressId()
                )
                .street(
                        address.getStreet()
                )
                .buildingName(
                        address.getBuildingName()
                )
                .city(
                        address.getCity()
                )
                .state(
                        address.getState()
                )
                .country(
                        address.getCountry()
                )
                .pincode(
                        address.getPincode()
                )
                .build();
    }
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses() {

        List<Address> addresses =
                addressRepository.findAll();

        return addresses.stream()
                .map(this::mapToAddressResponse)
                .toList();
    }
    @Transactional
    public  AddressResponse updateAddress(
            Long addressId,
            AddressRequest request
    ) {
        // 1 . Find address
        Address address = addressRepository
                .findById(addressId)
                .orElseThrow(() ->
                        new RuntimeException("Address not found with id:" + addressId));
        // 2 . Update fields
        address.setStreet(request.getStreet());
        address.setBuildingName(request.getBuildingName());
        address.setCity(request.getCity());
        address.setCountry(request.getCountry());
        address.setPincode(request.getPincode());
        // 3. Save update address
        Address updateAddress = addressRepository.save(address);
        // 4. Entity-> Response DTO
        return mapToAddressResponse(updateAddress);
    }
    @Transactional
    public void deleteAddressById(Long addressId){
        Address address = addressRepository.findById(addressId)
                .orElseThrow(()->
                        new RuntimeException("Address not found with id :"+ addressId));
        // 2. Remove Address from owning side (User)
        address.getUsers().forEach(user ->
                user.getAddresses().remove(address));
        // 3. Clear inverse relationship
        address.getUsers().clear();
        // 4. Delete address
        addressRepository.delete(address);

    }
}
