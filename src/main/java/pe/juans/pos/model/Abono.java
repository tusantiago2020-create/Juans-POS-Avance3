package pe.juans.pos.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="abono")
public class Abono {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public Long idCliente;
    public Long idUsuario;
    @Column(precision=10,scale=2) public BigDecimal monto=BigDecimal.ZERO;
    public String medioPago;
    public LocalDateTime fechaHora=LocalDateTime.now();
}
