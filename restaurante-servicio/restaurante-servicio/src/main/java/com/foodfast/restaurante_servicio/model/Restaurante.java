package com.foodfast.restaurante_servicio.model;
 
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
@Table(name = "restaurantes")
public class Restaurante {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, length = 120)
    private String nombre;
 
    @Column(length = 255)
    private String descripcion;
 
    @Column(nullable = false, length = 180)
    private String direccion;
 
    @Column(nullable = false, length = 30)
    private String telefono;
 
    @Column(length = 120)
    private String email;
 
    @Column(nullable = false)
    private Boolean activo;
 
    @Column(nullable = false)
    private Boolean abierto;
 
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<HorarioRestaurante> horarios = new ArrayList<>();
}
