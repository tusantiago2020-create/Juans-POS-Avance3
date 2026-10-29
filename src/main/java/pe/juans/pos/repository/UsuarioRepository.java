package pe.juans.pos.repository;
import pe.juans.pos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UsuarioRepository extends JpaRepository<Usuario,Long> { Optional<Usuario> findByUsernameAndActivoTrue(String username); }
