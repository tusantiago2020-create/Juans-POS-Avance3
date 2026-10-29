package pe.juans.pos.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="alerta")
public class Alerta {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public Long idProducto;
    public String tipo;
    @Column(length=255) public String mensaje;
    public String estado="PENDIENTE";
    public LocalDateTime fechaGeneracion=LocalDateTime.now();
    public LocalDateTime fechaAtencion;
    public Long idUsuarioAtencion;
}
