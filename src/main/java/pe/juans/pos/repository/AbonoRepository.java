package pe.juans.pos.repository;
import pe.juans.pos.model.Abono;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AbonoRepository extends JpaRepository<Abono,Long> { List<Abono> findByIdClienteOrderByFechaHoraDesc(Long cliente); }
