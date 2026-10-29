package pe.juans.pos.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="producto")
public class Producto {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    @Column(unique=true,nullable=false) public String codigo;
    @Column(nullable=false) public String nombre;
    @Column(nullable=false) public String categoria="General";
    public String proveedor="Sin especificar";
    @Column(nullable=false,precision=10,scale=2) public BigDecimal precioCompra=BigDecimal.ZERO;
    @Column(nullable=false,precision=10,scale=2) public BigDecimal precioVenta=BigDecimal.ONE;
    public int stockActual=0;
    public int stockMinimo=0;
    public LocalDate fechaVencimiento;
    public boolean activo=true;
}
