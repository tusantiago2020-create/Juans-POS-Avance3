package pe.juans.pos.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="usuario")
public class Usuario {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    @Column(unique=true,nullable=false) public String username;
    @Column(nullable=false,name="password_hash") public String passwordHash;
    @Column(nullable=false) public String nombres;
    @Column(nullable=false) public String rol="VENDEDOR";
    public boolean activo=true;
}
