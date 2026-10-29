package pe.juans.pos.repository;
import pe.juans.pos.model.Cliente;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface ClienteRepository extends JpaRepository<Cliente,Long> {
 List<Cliente> findByActivoTrueOrderByNombresAsc();
 Optional<Cliente> findByDni(String dni);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from Cliente c where c.id = :id")
 Optional<Cliente> bloquearPorId(@Param("id") Long id);
}
