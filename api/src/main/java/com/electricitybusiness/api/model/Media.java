package com.electricitybusiness.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité représentant un média.
 * Un média peut être associé à une option, une borne et plusieurs lieux.
 */
@Data
@Entity
@Table(name = "medias")
@NoArgsConstructor
@AllArgsConstructor
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_media")
    @EqualsAndHashCode.Include
    private Long idMedia;

    @Column(name = "name_media", length = 100, nullable = false)
    @NotBlank(message = "Le nom est obligatoire")
    private String nameMedia;

    @Column(name = "url", length = 500, nullable = false)
    @NotBlank(message = "L'URL est obligatoire")
    private String url;

    @Column(name = "type", length = 50, nullable = false)
    @NotBlank(message = "Le type est obligatoire")
    private String type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String descriptionMedia;

    @Column(name = "size")
    private String size;

    @Column(name = "date_creation")
    @NotNull(message = "La date de création est obligatoire")
    private LocalDateTime dateCreation = LocalDateTime.now();

/*    @ManyToOne
    @JoinColumn(name = "id_options")
    private Option options;*/

    @ManyToMany(mappedBy = "medias")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Option> options;

/*
    @ManyToOne
    @JoinColumn(name = "id_terminal")
    private Terminal terminal;
*/

    @ManyToMany(mappedBy = "medias")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Terminal> terminals;

    @ManyToMany(mappedBy = "medias")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Place> places;

/*    @OneToOne
    @JoinColumn(name = "id_user")
    private User user;*/

/*    @OneToOne(mappedBy = "media")
    private User user;*/
    @OneToOne(mappedBy = "media", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User user;

}
