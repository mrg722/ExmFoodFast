package com.foodfast.pago_servicio.model;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pagos")
public class Pago {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;
 
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private MetodoPago metodoPago;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 30)
    private EstadoPago estadoPago;
 
    @Column(name = "codigo_transaccion", length = 120)
    private String codigoTransaccion;
 
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
 
    @Column(name = "fecha_procesamiento")
    private LocalDateTime fechaProcesamiento;
}
