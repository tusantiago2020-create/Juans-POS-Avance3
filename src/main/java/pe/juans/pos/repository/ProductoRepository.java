package pe.juans.pos.repository;
import pe.juans.pos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
public interface ProductoRepository extends JpaRepository<Producto,Long> { List<Producto> findByActivoTrueOrderByNombreAsc(); Optional<Producto> findByCodigo(String codigo); @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Producto p where p.id = :id") Optional<Producto> bloquearPorId(@Param("id") Long id); }
