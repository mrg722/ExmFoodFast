package com.foodfast.autenticacion_servicio.dto;

import com.foodfast.autenticacion_servicio.model.Rol;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}
