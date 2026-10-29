package pe.juans.pos.repository;
import pe.juans.pos.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AlertaRepository extends JpaRepository<Alerta,Long> { List<Alerta> findAllByOrderByFechaGeneracionDesc(); boolean existsByIdProductoAndTipoAndEstado(Long producto, String tipo, String estado); }
