package br.com.aegispatrimonio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rbac_permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String resource;

    @Column(nullable = false, length = 32)
    private String action;

    @Column(length = 255)
    private String description;

    @Column(name = "context_key", length = 32)
    private String contextKey;
}
