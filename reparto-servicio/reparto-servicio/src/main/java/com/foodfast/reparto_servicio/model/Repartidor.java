package com.foodfast.reparto_servicio.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "repartidores")
public class Repartidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Column(nullable = false, length = 50)
    private String vehiculo;

    @Column(nullable = false)
    private Boolean activo;

    @Column(nullable = false)
    private Boolean disponible;

    @Builder.Default
    @OneToMany(mappedBy = "repartidor")
    private List<Entrega> entregas = new ArrayList<>();
}
