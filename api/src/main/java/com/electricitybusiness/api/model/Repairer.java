package com.electricitybusiness.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entité représentant un réparateur dans le système.
 * Un réparateur peut être associé à plusieurs bornes.
 */
@Data
@Entity
@Table(name = "repairers")
@NoArgsConstructor
@AllArgsConstructor
public class Repairer implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_repairer")
    private Long idRepairer;

    @NotBlank
    @Column(name = "name_reparateur")
    private String nameRepairer;

    @NotBlank
    @Column(name = "email_repairer")
    private String emailRepairer;

    @NotBlank
    @Column(name = "password_repairer")
    private String passwordRepairer;

    @Enumerated
    @NotNull
    private UserRole role = UserRole.USER;

    @ManyToMany(mappedBy = "repairers")
    private Set<Terminal> terminals = new HashSet<>();


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    };

    @Override
    public String getPassword() {
        return passwordRepairer;
    }

    @Override
    public String getUsername() {
        return emailRepairer;
    }

}
