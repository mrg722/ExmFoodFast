package com.foodfast.reparto_servicio.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "entregas",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_entregas_pedido", columnNames = "pedido_id")
        }
)
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Repartidor repartidor;

    @Column(name = "direccion_entrega", nullable = false, length = 180)
    private String direccionEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_entrega", nullable = false, length = 30)
    private EstadoEntrega estadoEntrega;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
