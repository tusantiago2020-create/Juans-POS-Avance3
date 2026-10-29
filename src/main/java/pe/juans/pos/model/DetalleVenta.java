package pe.juans.pos.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="detalle_venta")
public class DetalleVenta {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public Long idVenta;
    public Long idProducto;
    public int cantidad;
    @Column(precision=10,scale=2) public BigDecimal precioUnitario=BigDecimal.ZERO;
    @Column(precision=10,scale=2) public BigDecimal costoUnitario=BigDecimal.ZERO;
    @Column(precision=10,scale=2) public BigDecimal subtotal=BigDecimal.ZERO;
}
