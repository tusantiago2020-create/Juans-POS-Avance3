package pe.juans.pos.repository;
import pe.juans.pos.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario,Long> { List<MovimientoInventario> findAllByOrderByFechaHoraDesc(); }
