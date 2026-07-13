package com.foodfast.pago_servicio.repository;
 
import com.foodfast.pago_servicio.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.List;
 
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByPedidoId(Long pedidoId);
    boolean existsByPedidoId(Long pedidoId);
}
