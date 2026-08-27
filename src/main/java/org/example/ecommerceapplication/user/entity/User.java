package org.example.ecommerceapplication.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.ecommerceapplication.address.entity.Address;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    // Email verification status
    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns =
            @JoinColumn(name = "user_id"),
            inverseJoinColumns =
            @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_address",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "address_id")
    )
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();
}
