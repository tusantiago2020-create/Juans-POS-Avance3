package pe.juans.pos.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="movimiento_inventario")
public class MovimientoInventario {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public Long idProducto;
    public Long idUsuario;
    public Long idVenta;
    public String tipo;
    public int cantidad;
    public String motivo;
    public LocalDateTime fechaHora=LocalDateTime.now();
}
