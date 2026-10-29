package pe.juans.pos.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="auditoria")
public class Auditoria {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public Long idUsuario;
    public String accion;
    public String tablaAfectada;
    public String idRegistro;
    public LocalDateTime fechaHora=LocalDateTime.now();
}
