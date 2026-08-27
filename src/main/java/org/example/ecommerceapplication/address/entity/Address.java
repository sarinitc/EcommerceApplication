package org.example.ecommerceapplication.address.entity;


import jakarta.persistence.*;
import lombok.*;
import org.example.ecommerceapplication.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "street")
    private String street;


    @Column(name = "building_name")
    private String buildingName;


    @Column(name = "city")
    private String city;


    @Column(name = "state")
    private String state;


    @Column(name = "country")
    private String country;


    @Column(name = "pincode")
    private String pincode;


    // Address can be connected to users through user_address
    @ManyToMany(mappedBy = "addresses")
    @Builder.Default
    private List<User> users =
            new ArrayList<>();
}