package com.foodfast.restaurante_servicio.model;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalTime;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "horarios_restaurante")
public class HorarioRestaurante {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(optional = false)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;
 
    @Column(name = "dia_semana", nullable = false, length = 20)
    private String diaSemana;
 
    @Column(name = "hora_apertura", nullable = false)
    private LocalTime horaApertura;
 
    @Column(name = "hora_cierre", nullable = false)
    private LocalTime horaCierre;
}
