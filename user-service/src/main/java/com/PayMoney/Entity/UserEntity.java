package com.PayMoney.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.util.Collection;
import java.util.List;

@Entity
@Table(name="users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "user_Id")
    private Long  userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @ToString.Exclude
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role));

    }

}
