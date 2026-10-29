package pe.juans.pos.repository;
import pe.juans.pos.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface VentaRepository extends JpaRepository<Venta,Long> { List<Venta> findAllByOrderByFechaHoraDesc(); }
