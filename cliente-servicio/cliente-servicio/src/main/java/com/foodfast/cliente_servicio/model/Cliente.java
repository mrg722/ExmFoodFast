package com.foodfast.cliente_servicio.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Column(nullable = false)
    private Boolean activo;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<Direccion> direcciones = new ArrayList<>();

    public void agregarDireccion(Direccion direccion) {
        direcciones.add(direccion);
        direccion.setCliente(this);
    }

    public void limpiarDirecciones() {
        direcciones.forEach(direccion -> direccion.setCliente(null));
        direcciones.clear();
    }
}
