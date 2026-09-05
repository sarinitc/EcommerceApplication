package org.example.ecommerceapplication.customer.service;

import lombok.RequiredArgsConstructor;
import org.example.ecommerceapplication.address.dto.request.AddressRequest;
import org.example.ecommerceapplication.address.entity.Address;
import org.example.ecommerceapplication.address.repository.AddressRepository;
import org.example.ecommerceapplication.auth.service.EmailService;
import org.example.ecommerceapplication.customer.dto.response.*;
import org.example.ecommerceapplication.customer.dto.response.request.AdminCreateCustomerRequest;
import org.example.ecommerceapplication.customer.dto.request.AdminCustomerUpdateRequest;
import org.example.ecommerceapplication.customer.exception.CustomerNotFoundException;
import org.example.ecommerceapplication.orders.entity.Order;
import org.example.ecommerceapplication.orders.repository.OrderRepository;
import org.example.ecommerceapplication.user.entity.AccountStatus;
import org.example.ecommerceapplication.user.entity.Role;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.RoleRepository;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminCustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RoleRepository roleRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    @Transactional(readOnly = true)
    public Page<AdminCustomerResponse> getCustomers(
            int page,
            int size,
            String search
    ) {

        Pageable pageable =
                PageRequest.of(page, size);

        Page<User> users =
                userRepository.findCustomers(
                        search,
                        pageable
                );


        return users.map(user -> {

            long orderCount =
                    orderRepository
                            .countByUser_Id(
                                    user.getId()
                            );


            BigDecimal totalSpent =
                    orderRepository
                            .sumTotalSpentByUserId(
                                    user.getId()
                            );


            Order lastOrder =
                    orderRepository
                            .findTopByUser_IdOrderByOrderDateDescOrderIdDesc(
                                    user.getId()
                            )
                            .orElse(null);

            CustomerLocationResponse location = user.getAddresses().stream()
                    .findFirst()
                    .map(this::toLocationResponse)
                    .orElse(null);

            return AdminCustomerResponse.builder()
                    .customerId(
                            user.getId()
                    )
                    .username(
                            user.getUsername()
                    )
                    .email(
                            user.getEmail()
                    )
                    .phoneNumber(
                            user.getPhoneNumber()
                    )
                    .profileImage(
                            user.getProfileImage()
                    )
                    .verified(
                            user.isVerified()
                    )
                    .accountStatus(
                            user.getAccountStatus().name()
                    )
                    .location(
                            location
                    )
                    .orderCount(
                            orderCount
                    )
                    .totalSpent(
                            totalSpent
                    )
                    .lastOrder(
                            toLastOrderResponse(lastOrder)
                    )
                    .build();
        });
    }

    private CustomerLocationResponse toLocationResponse(Address address) {
        return CustomerLocationResponse.builder()
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .build();
    }

    private CustomerLastOrderResponse toLastOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        return CustomerLastOrderResponse.builder()
                .orderId(order.getOrderId())
                .orderDate(order.getOrderDate())
                .total(order.getTotalAmount())
                .status(order.getOrderStatus())
                .build();
    }
    @Transactional
    public void deleteCustomer(Long customerId) {

        User customer = findCustomer(customerId);

        boolean hasOrders =
                orderRepository.existsByUser_Id(customerId);

        if (hasOrders) {
            throw new RuntimeException(
                    "Customer has order history and cannot be deleted. Block the account instead."
            );
        }

        customer.getRoles().clear();

        customer.getAddresses().clear();

        userRepository.save(customer);

        userRepository.delete(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> getCustomerAddresses(Long customerId) {
        User customer = findCustomer(customerId);

        return customer.getAddresses().stream()
                .sorted(Comparator.comparing(Address::getAddressId))
                .map(this::toAddressResponse)
                .toList();
    }

    private User findCustomer(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + customerId
                ));

        boolean isCustomer = customer.getRoles().stream()
                .anyMatch(role -> "CUSTOMER".equals(role.getName()));
        boolean isPrivilegedUser = customer.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName())
                        || "SELLER".equals(role.getName()));

        if (!isCustomer || isPrivilegedUser) {
            throw new CustomerNotFoundException(
                    "Customer not found with id: " + customerId
            );
        }

        return customer;
    }

    private CustomerAddressResponse toAddressResponse(Address address) {
        return CustomerAddressResponse.builder()
                .addressId(address.getAddressId())
                .street(address.getStreet())
                .buildingName(address.getBuildingName())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .pincode(address.getPincode())
                .build();
    }
    @Transactional
    public AdminCustomerDetailResponse createCustomer(
            AdminCreateCustomerRequest request
    ) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String username = request.getUsername().trim();

        // 1. Check duplicate account details
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new RuntimeException("Username already exists");
        }


        // 2. Find CUSTOMER role
        Role customerRole =
                roleRepository.findByName("CUSTOMER")
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "CUSTOMER role not found"
                                )
                        );


        // 3. Create User
        User customer =
                User.builder()
                        .username(username)
                        .email(email)
                        .phoneNumber(request.getPhoneNumber())
                        .profileImage(request.getProfileImage())

                        .password(
                                passwordEncoder.encode(
                                        request.getTemporaryPassword()
                                )
                        )

                        .verified(
                                request.isVerified()
                        )

                        .accountStatus(
                                request.getAccountStatus() != null
                                        ? request.getAccountStatus()
                                        : AccountStatus.ACTIVE
                        )

                        .build();


        // 4. Assign CUSTOMER role
        customer.getRoles().add(customerRole);


        // 5. Save User
        User savedCustomer =
                userRepository.save(customer);


        // 6. Create addresses if provided
        if (request.getAddresses() != null) {

            for (AddressRequest addressRequest
                    : request.getAddresses()) {

                Address address =
                        Address.builder()
                                .street(
                                        addressRequest.getStreet()
                                )
                                .buildingName(
                                        addressRequest.getBuildingName()
                                )
                                .city(
                                        addressRequest.getCity()
                                )
                                .state(
                                        addressRequest.getState()
                                )
                                .country(
                                        addressRequest.getCountry()
                                )
                                .pincode(
                                        addressRequest.getPincode()
                                )
                                .build();


                Address savedAddress =
                        addressRepository.save(address);


                savedCustomer
                        .getAddresses()
                        .add(savedAddress);

                savedAddress
                        .getUsers()
                        .add(savedCustomer);
            }


            userRepository.save(savedCustomer);
        }

        emailService.sendCustomerInvitation(
                savedCustomer.getEmail(),
                savedCustomer.getUsername()
        );

        // 7. Return DTO
        return mapToCustomerDetail(savedCustomer);
    }

    private AdminCustomerDetailResponse mapToCustomerDetail(User customer) {
        return AdminCustomerDetailResponse.builder()
                .customerId(customer.getId())
                .username(customer.getUsername())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .profileImage(customer.getProfileImage())
                .verified(customer.isVerified())
                .accountStatus(customer.getAccountStatus().name())
                .customerType(customer.getCustomerType())
                .taxExempt(customer.isTaxExempt())
                .gender(customer.getGender())
                .preferredLanguage(customer.getPreferredLanguage())
                .preferredCurrency(customer.getPreferredCurrency())
                .dateOfBirth(customer.getDateOfBirth())
                .allowMarketingEmails(customer.isAllowMarketingEmails())
                .internalNotes(customer.getInternalNotes())
                .joinedAt(customer.getCreatedAt())
                .stats(null)
                .build();
    }

    @Transactional
    public AdminCustomerDetailResponse updateCustomer(
            Long customerId,
            AdminCustomerUpdateRequest request
    ) {
        User customer = findCustomer(customerId);

        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            customer.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfileImage() != null) {
            customer.setProfileImage(request.getProfileImage());
        }
        if (request.getAccountStatus() != null) {
            customer.setAccountStatus(request.getAccountStatus());
        }
        if (request.getCustomerType() != null) {
            customer.setCustomerType(request.getCustomerType());
        }
        if (request.getTaxExempt() != null) {
            customer.setTaxExempt(request.getTaxExempt());
        }
        if (request.getVerified() != null) {
            customer.setVerified(request.getVerified());
        }
        if (request.getGender() != null) {
            customer.setGender(request.getGender());
        }
        if (request.getPreferredLanguage() != null) {
            customer.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getPreferredCurrency() != null) {
            customer.setPreferredCurrency(request.getPreferredCurrency());
        }
        if (request.getDateOfBirth() != null) {
            customer.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAllowMarketingEmails() != null) {
            customer.setAllowMarketingEmails(request.getAllowMarketingEmails());
        }
        if (request.getInternalNotes() != null) {
            customer.setInternalNotes(request.getInternalNotes());
        }

        return mapToCustomerDetail(userRepository.save(customer));
    }
}
