package com.foodfast.cliente_servicio.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "direcciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String calle;

    @Column(nullable = false, length = 30)
    private String numero;

    @Column(nullable = false, length = 100)
    private String comuna;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(length = 255)
    private String referencia;

    @Column(nullable = false)
    private Boolean principal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
}
