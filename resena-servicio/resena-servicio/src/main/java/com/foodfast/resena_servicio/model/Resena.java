package com.foodfast.resena_servicio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resenas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_resena_cliente_producto", columnNames = {"cliente_id", "producto_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "restaurante_id")
    private Long restauranteId;

    @Column(nullable = false)
    private Integer calificacion;

    @Column(nullable = false, length = 500)
    private String comentario;

    @Column(nullable = false)
    private Boolean activa;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
