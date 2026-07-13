package com.foodfast.restaurante_servicio.dto;
 
import lombok.Builder;
import lombok.Data;
 
import java.util.List;
 
@Data
@Builder
public class RestauranteResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String direccion;
    private String telefono;
    private String email;
    private Boolean activo;
    private Boolean abierto;
    private List<HorarioRestauranteResponse> horarios;
}
