package pe.juans.pos.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="cliente")
public class Cliente {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    @Column(unique=true,nullable=false,length=8) public String dni;
    @Column(nullable=false) public String nombres;
    public String telefono;
    @Column(precision=10,scale=2) public BigDecimal limiteCredito=BigDecimal.ZERO;
    @Column(precision=10,scale=2) public BigDecimal saldoDeuda=BigDecimal.ZERO;
    public boolean habilitadoFiado=false;
    public boolean activo=true;
}
