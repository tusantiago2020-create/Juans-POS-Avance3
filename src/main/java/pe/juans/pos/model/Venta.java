package pe.juans.pos.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="venta")
public class Venta {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    @Column(nullable=false) public Long idUsuario;
    public Long idCliente;
    public LocalDateTime fechaHora=LocalDateTime.now();
    public String tipoPago;
    @Column(precision=10,scale=2) public BigDecimal total=BigDecimal.ZERO;
    @Column(precision=10,scale=2) public BigDecimal montoRecibido=BigDecimal.ZERO;
    public String estado="COMPLETADA";
    public String motivoAnulacion;
}
