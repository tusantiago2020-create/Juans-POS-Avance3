package pe.juans.pos.repository;
import pe.juans.pos.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta,Long> { List<DetalleVenta> findByIdVenta(Long venta); }
